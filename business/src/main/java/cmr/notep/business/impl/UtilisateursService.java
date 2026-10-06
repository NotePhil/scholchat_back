package cmr.notep.business.impl;

import cmr.notep.business.business.UtilisateursBusiness;
import cmr.notep.business.security.AccessControlService;
import cmr.notep.business.security.CurrentUserService;
import cmr.notep.business.services.ActivationService;
import cmr.notep.interfaces.modeles.Eleves;
import cmr.notep.interfaces.modeles.Parents;
import cmr.notep.interfaces.modeles.Professeurs;
import cmr.notep.interfaces.api.UtilisateursApi;
import cmr.notep.interfaces.modeles.IUtilisateurs;
import cmr.notep.interfaces.modeles.Utilisateurs;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
@RestController
@Slf4j
public class UtilisateursService implements UtilisateursApi {
    private final UtilisateursBusiness utilisateursBusiness;
    private final ActivationService activationService;
    private final CurrentUserService currentUser;
    private final AccessControlService accessControl;
    private final cmr.notep.business.utils.JwtUtil jwtUtil;

    public UtilisateursService(UtilisateursBusiness utilisateursBusiness, ActivationService activationService,
                               CurrentUserService currentUser, AccessControlService accessControl,
                               cmr.notep.business.utils.JwtUtil jwtUtil) {
        this.utilisateursBusiness = utilisateursBusiness;
        this.activationService = activationService;
        this.currentUser = currentUser;
        this.accessControl = accessControl;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Utilisateurs avoirUtilisateur( String idUtilisateur) {
        log.info("Récupération de l'utilisateur avec ID: {}", idUtilisateur);
        return redigerPiecesIdentite(utilisateursBusiness.avoirUtilisateur(idUtilisateur));
    }

    /**
     * PATCH /utilisateurs/{id}
     * - anonyme : uniquement pour joindre matricule / CNI / selfie au compte professeur tout juste créé
     *   par l'inscription, avec le jeton de dépôt renvoyé par POST /utilisateurs (en-tête X-Upload-Token,
     *   voir AccessControlService#hasSignupUploadAccess) ;
     * - connecté : soi-même, un parent pour son enfant, ou un administrateur ;
     *   seul un administrateur peut changer l'état (activation) ou l'email (sujet du JWT).
     */
    @Override
    public Utilisateurs patcherUtilisateur(@NonNull String idUtilisateur, @NonNull Utilisateurs partialUpdate) {
        log.info("Patching user with ID: {}", idUtilisateur);
        if (!currentUser.isAuthenticated()) {
            // Jeton de dépôt renvoyé par l'inscription (en-tête X-Upload-Token), émis pour CE compte,
            // et compte encore en cours d'inscription : connaître l'id ne suffit pas.
            if (!(partialUpdate instanceof Professeurs docs) || !accessControl.hasSignupUploadAccess(idUtilisateur)) {
                throw new cmr.notep.business.exceptions.SchoolException(
                        cmr.notep.business.exceptions.enums.SchoolErrorCode.UNAUTHORIZED,
                        "Authentification requise. Veuillez vous connecter.");
            }
            // On ne transmet QUE les pièces de l'inscription : nom, email, état… sont ignorés.
            Professeurs pieces = new Professeurs();
            pieces.setMatriculeProfesseur(docs.getMatriculeProfesseur());
            pieces.setCniUrlRecto(docs.getCniUrlRecto());
            pieces.setCniUrlVerso(docs.getCniUrlVerso());
            pieces.setSelfieUrl(docs.getSelfieUrl());
            return vueMinimale(utilisateursBusiness.patcherUtilisateur(idUtilisateur, pieces));
        }

        accessControl.requireSelfOrParentOrAdmin(idUtilisateur);
        if (!currentUser.isAdmin()) {
            Utilisateurs existant = utilisateursBusiness.avoirUtilisateur(idUtilisateur);
            if (partialUpdate.getEtat() != null && partialUpdate.getEtat() != existant.getEtat()) {
                throw CurrentUserService.forbidden("Seul un administrateur peut modifier l'état d'un compte.");
            }
            if (partialUpdate.getEmail() != null && !partialUpdate.getEmail().isBlank()
                    && !partialUpdate.getEmail().trim().equalsIgnoreCase(existant.getEmail())) {
                throw CurrentUserService.forbidden("L'adresse email d'un compte ne peut être modifiée que par un administrateur.");
            }
            partialUpdate.setEtat(null);
            partialUpdate.setEmail(null);
        }
        return redigerPiecesIdentite(utilisateursBusiness.patcherUtilisateur(idUtilisateur, partialUpdate));
    }
    @Override
    public List<Utilisateurs> avoirToutAdmins() {
        log.info("Récupération de tous les administrateurs");
        return utilisateursBusiness.avoirToutAdmins();
    }

    @Override
    public List<Utilisateurs> avoirToutUtilisateurs() {
        log.info("Récupération de tous les utilisateurs");
        currentUser.requireAdmin();
        return utilisateursBusiness.avoirToutUtilisateurs();
    }

    /**
     * POST /utilisateurs — inscription publique.
     * Sans être administrateur, seuls les types proposés par les pages d'inscription
     * (élève, parent, professeur) sont acceptés, sans privilège admin. Les autres types
     * (gestionnaire, répétiteur, utilisateur générique) sont créés par un administrateur.
     */
    @Override
    public Utilisateurs posterUtilisateur(@NonNull Utilisateurs utilisateur) {
        log.info("Création d'un nouvel utilisateur");
        boolean admin = currentUser.isAdmin();
        if (!admin) {
            boolean typePublic = utilisateur.getClass() == Eleves.class
                    || utilisateur.getClass() == Parents.class
                    || utilisateur.getClass() == Professeurs.class;
            if (!typePublic) {
                throw CurrentUserService.forbidden("Ce type de compte ne peut être créé que par un administrateur.");
            }
            utilisateur.setAdmin(false);
            utilisateur.setPasseAccess(null);
            utilisateur.setActivationToken(null);
            utilisateur.setResetPasswordToken(null);
            if (utilisateur instanceof Professeurs p) {
                p.setHasUploaded(false);
            }
        }
        Utilisateurs cree = utilisateursBusiness.posterUtilisateur(utilisateur);
        if (admin) return cree;
        Utilisateurs vue = vueMinimale(cree);
        // Inscription professeur : jeton de courte durée pour déposer les pièces sans être connecté
        // (étape "pièces justificatives" des clients web et mobile).
        if (utilisateur instanceof Professeurs && cree.getId() != null
                && accessControl.isSignupPendingProfessor(cree.getId())) {
            vue.setUploadToken(jwtUtil.generateProfessorDocumentsUploadToken(cree.getId()));
        }
        return vue;
    }

    /** Réponse réduite pour les appels non-admin d'inscription : ne divulgue pas le profil d'un compte existant. */
    private static Utilisateurs vueMinimale(Utilisateurs u) {
        Utilisateurs vue;
        if (u instanceof Professeurs) vue = new Professeurs();
        else if (u instanceof Eleves) vue = new Eleves();
        else if (u instanceof Parents) vue = new Parents();
        else vue = new Utilisateurs();
        vue.setId(u.getId());
        vue.setEmail(u.getEmail());
        vue.setNom(u.getNom());
        vue.setPrenom(u.getPrenom());
        vue.setEtat(u.getEtat());
        vue.setCreationDate(u.getCreationDate());
        vue.setInscriptionStatut(u.getInscriptionStatut());
        return vue;
    }

    /** Les scans de CNI d'un professeur ne sont visibles que de lui-même et des administrateurs. */
    private Utilisateurs redigerPiecesIdentite(Utilisateurs u) {
        if (u instanceof Professeurs p && !currentUser.isAdmin() && !currentUser.isSelf(p.getId())) {
            p.setCniUrlRecto(null);
            p.setCniUrlVerso(null);
            p.setMatriculeProfesseur(null);
        }
        return u;
    }


    @Override
    public Utilisateurs regenererActivationEmail(@RequestParam String email) {
        log.info("Regeneration de l'email d'activation pour: {}", email);
        Utilisateurs u = utilisateursBusiness.regenererActivationEmail(email);
        return currentUser.isAdmin() ? u : vueMinimale(u);
    }

    @Override
    public Utilisateurs validerProfesseur(String professorId) {
        log.info("Validating professor with ID: {}", professorId);
        currentUser.requireAdmin();
        return utilisateursBusiness.validerProfesseur(professorId);
    }

    @Override
    public List<Utilisateurs> avoirProfesseursEnAttente() {
        log.info("Fetching all pending professors");
        currentUser.requireAdmin();
        return utilisateursBusiness.avoirProfesseursEnAttente();
    }

    @Override
    public Utilisateurs rejeterProfesseur(
            @PathVariable String professorId,
            @RequestParam String codeErreur,
            @RequestParam(required = false) String motifSupplementaire) {
        currentUser.requireAdmin();
        return utilisateursBusiness.rejeterProfesseur(professorId, codeErreur, motifSupplementaire);
    }
}
