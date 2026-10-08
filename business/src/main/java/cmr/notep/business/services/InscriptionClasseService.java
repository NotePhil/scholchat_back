package cmr.notep.business.services;

import cmr.notep.business.exceptions.SchoolException;
import cmr.notep.business.exceptions.enums.SchoolErrorCode;
import cmr.notep.modele.EtatClasse;
import cmr.notep.modele.EtatDemandeAcces;
import cmr.notep.modele.EtatUtilisateur;
import cmr.notep.ressourcesjpa.commun.DaoAccessorService;
import cmr.notep.ressourcesjpa.dao.ClassesEntity;
import cmr.notep.ressourcesjpa.dao.UserRoleEntity;
import cmr.notep.ressourcesjpa.dao.UtilisateursEntity;
import cmr.notep.ressourcesjpa.repository.ClassesRepository;
import cmr.notep.ressourcesjpa.repository.DemandeAccesRepository;
import cmr.notep.ressourcesjpa.repository.UserRoleRepository;
import cmr.notep.ressourcesjpa.repository.UtilisateursRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Inscription d'un parent ou d'un élève majeur avec le code d'une classe.
 *
 * <ol>
 *   <li>POST /utilisateurs (type parent / eleve + codeClasse) : le compte est créé SANS mot de passe, à
 *       l'état {@link EtatUtilisateur#AWAITING_VALIDATION} (non purgé, connexion et renvoi du lien
 *       d'activation impossibles), sans e-mail d'activation, et une demande d'accès à la classe est créée.</li>
 *   <li>Approbation de la demande (endpoint existant, mêmes permissions) : mot de passe temporaire généré,
 *       compte activé, must_change_password = true, e-mail avec les identifiants.</li>
 *   <li>Refus : e-mail avec le motif, le compte reste en attente (une nouvelle inscription avec le même
 *       e-mail et un code de classe ajoute simplement une nouvelle demande).</li>
 * </ol>
 *
 * Un compte "en attente d'inscription par classe" se reconnaît à : état AWAITING_VALIDATION et aucun profil
 * professeur (AWAITING_VALIDATION n'est sinon utilisé que pour les professeurs en attente de validation).
 */
@Service
@Slf4j
public class InscriptionClasseService {

    public static final String STATUT_EN_ATTENTE_APPROBATION_CLASSE = "EN_ATTENTE_APPROBATION_CLASSE";

    private static final String MAJUSCULES = "ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final String MINUSCULES = "abcdefghijkmnpqrstuvwxyz";
    private static final String CHIFFRES = "23456789";
    // Pas de + / = : le mot de passe ne doit jamais ressembler à du base64 (voir PasswordDecryptionService)
    private static final String SPECIAUX = "!@#$%*?-_";
    private static final int LONGUEUR_MOT_DE_PASSE = 12;

    private final SecureRandom random = new SecureRandom();
    private final DaoAccessorService daoAccessorService;
    private final PasswordEncoder passwordEncoder;
    private final InscriptionClasseEmailService emailService;

    @org.springframework.beans.factory.annotation.Autowired
    @org.springframework.context.annotation.Lazy
    private NotificationService notificationService;

    public InscriptionClasseService(DaoAccessorService daoAccessorService, PasswordEncoder passwordEncoder,
                                    InscriptionClasseEmailService emailService) {
        this.daoAccessorService = daoAccessorService;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    /**
     * Classe correspondant au code saisi à l'inscription, et qui accepte l'inscription demandée.
     *
     * @param pourEleve vrai pour un élève (la classe doit être ouverte aux majeurs)
     */
    public ClassesEntity resoudreClasse(String codeClasse, boolean pourEleve) {
        if (codeClasse == null || codeClasse.isBlank()) {
            throw new SchoolException(SchoolErrorCode.CODE_CLASSE_REQUIS,
                    "Le code de la classe est obligatoire pour créer un compte parent ou élève.");
        }
        String code = codeClasse.trim();
        List<ClassesEntity> classes = daoAccessorService.getRepository(ClassesRepository.class).findByCodeActivation(code);
        if (classes.isEmpty()) {
            throw new SchoolException(SchoolErrorCode.CODE_CLASSE_INVALIDE,
                    "Aucune classe ne correspond à ce code. Vérifiez le code transmis par votre enseignant.");
        }
        ClassesEntity classe = classes.stream().filter(c -> c.getEtat() == EtatClasse.ACTIF).findFirst()
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.CLASSE_NON_ACTIVE,
                        "Cette classe n'est pas active : elle n'accepte pas d'inscription pour le moment."));
        if (classe.isExpireParOffre()) {
            throw new SchoolException(SchoolErrorCode.CLASSE_NON_ACTIVE,
                    "Cette classe n'accepte pas d'inscription pour le moment : son offre a expiré.");
        }
        if (pourEleve && !classe.isAccesMajeur()) {
            throw new SchoolException(SchoolErrorCode.CLASSE_RESERVEE_MINEURS,
                    "Cette classe est réservée aux élèves mineurs : l'inscription se fait par un parent, "
                            + "qui crée un compte parent avec ce code puis ajoute son enfant.");
        }
        return classe;
    }

    /**
     * Aperçu public d'une classe à partir de son code (GET /public/classes/apercu) : mêmes validations et mêmes
     * erreurs que l'inscription ({@link #resoudreClasse}). Ne renvoie que des informations d'affichage.
     */
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public java.util.Map<String, Object> apercu(String codeClasse, boolean pourEleve) {
        ClassesEntity classe = resoudreClasse(codeClasse, pourEleve);
        java.util.Map<String, Object> vue = new java.util.LinkedHashMap<>();
        vue.put("classeId", classe.getId());
        vue.put("nom", classe.getNom());
        vue.put("niveau", classe.getNiveau());
        vue.put("etablissementNom", classe.getEtablissement() != null ? classe.getEtablissement().getNom() : null);
        vue.put("accesMajeur", classe.isAccesMajeur());
        String prof = classe.getModerator() != null ? nomComplet(classe.getModerator()) : null;
        vue.put("professeurNom", prof == null || prof.isBlank() ? null : prof);
        return vue;
    }

    /** Compte créé par l'inscription avec code de classe et jamais encore approuvé. */
    public boolean estEnAttenteInscriptionClasse(UtilisateursEntity u) {
        return u != null && estEnAttenteInscriptionClasse(u.getId(), u.getEtat());
    }

    public boolean estEnAttenteInscriptionClasse(String userId, EtatUtilisateur etat) {
        return userId != null && etat == EtatUtilisateur.AWAITING_VALIDATION
                && !daoAccessorService.getRepository(UtilisateursRepository.class).hasProfesseurRow(userId);
    }

    /** Le compte a-t-il au moins une demande d'accès encore en attente (sinon : toutes refusées) ? */
    public boolean aUneDemandeEnAttente(String userId) {
        return daoAccessorService.getRepository(DemandeAccesRepository.class).findByUtilisateurId(userId).stream()
                .anyMatch(d -> d.getEtat() == EtatDemandeAcces.EN_ATTENTE);
    }

    /**
     * Approbation d'une demande d'accès dont le demandeur est un compte en attente d'inscription par classe :
     * mot de passe temporaire, activation du compte et de ses rôles, e-mail des identifiants (après commit).
     * Sans effet pour tout autre compte.
     *
     * @return vrai si le compte vient d'être activé
     */
    public boolean activerSiInscriptionClasse(UtilisateursEntity utilisateur, ClassesEntity classe) {
        if (!estEnAttenteInscriptionClasse(utilisateur)) {
            return false;
        }
        String motDePasse = genererMotDePasseTemporaire();
        utilisateur.setPasseAccess(passwordEncoder.encode(motDePasse));
        utilisateur.setMustChangePassword(true);
        utilisateur.setEtat(EtatUtilisateur.ACTIVE);
        utilisateur.setActivationToken(null);
        utilisateur.setResetPasswordToken(null);
        daoAccessorService.getRepository(UtilisateursRepository.class).save(utilisateur);

        UserRoleRepository roleRepo = daoAccessorService.getRepository(UserRoleRepository.class);
        for (UserRoleEntity role : roleRepo.findByUtilisateurId(utilisateur.getId())) {
            if (!"PROFESSOR".equals(role.getRoleType()) && !Boolean.TRUE.equals(role.getIsActive())) {
                role.setIsActive(true);
                roleRepo.save(role);
            }
        }
        log.info("Class sign-up account {} activated on approval for class {} (temporary password e-mailed)",
                utilisateur.getId(), classe.getId());

        String email = utilisateur.getEmail();
        String nomComplet = nomComplet(utilisateur);
        String classeNom = classe.getNom();
        apresCommit(() -> emailService.envoyerIdentifiants(email, nomComplet, classeNom, motDePasse));
        return true;
    }

    /**
     * Approbation d'une demande d'accès (non parent) d'un compte actif dont le profil élève a été demandé
     * (ajout de profil avec un code de classe, rôle STUDENT inactif) : le rôle est activé, notification
     * STUDENT_ROLE_VALIDATED et e-mail (après commit). Le compte a déjà un mot de passe : rien d'autre ne change.
     *
     * @return vrai si le profil élève vient d'être activé (la demande est alors une demande ÉLÈVE : pas de droits
     *         de publication même si le compte est aussi professeur)
     */
    public boolean activerRoleEleveSiDemande(UtilisateursEntity utilisateur, ClassesEntity classe) {
        if (utilisateur == null || estEnAttenteInscriptionClasse(utilisateur)) {
            return false;
        }
        UserRoleRepository roleRepo = daoAccessorService.getRepository(UserRoleRepository.class);
        UserRoleEntity role = roleRepo.findByUtilisateurIdAndRoleType(utilisateur.getId(), "STUDENT").orElse(null);
        if (role == null || Boolean.TRUE.equals(role.getIsActive())) {
            return false;
        }
        role.setIsActive(true);
        roleRepo.save(role);
        log.info("STUDENT role of account {} activated on approval of its access request to class {}",
                utilisateur.getId(), classe.getId());
        if (notificationService != null) {
            notificationService.createRoleRequestDecisionNotification(utilisateur.getId(), "élève", true, null);
        }
        String email = utilisateur.getEmail();
        String nomComplet = nomComplet(utilisateur);
        String classeNom = classe.getNom();
        apresCommit(() -> emailService.envoyerProfilEleveValide(email, nomComplet, classeNom));
        return true;
    }

    /** Refus de la demande d'accès liée à une demande de profil élève (rôle STUDENT inactif) : notification. */
    public void notifierRefusRoleEleveSiDemande(UtilisateursEntity utilisateur, String motif) {
        if (utilisateur == null || estEnAttenteInscriptionClasse(utilisateur) || notificationService == null) {
            return;
        }
        boolean roleEnAttente = daoAccessorService.getRepository(UserRoleRepository.class)
                .findByUtilisateurIdAndRoleType(utilisateur.getId(), "STUDENT")
                .map(r -> !Boolean.TRUE.equals(r.getIsActive())).orElse(false);
        if (roleEnAttente) {
            notificationService.createRoleRequestDecisionNotification(utilisateur.getId(), "élève", false, motif);
        }
    }

    /** Refus de la demande d'un compte en attente d'inscription par classe : e-mail dédié (après commit). */
    public void notifierRefus(UtilisateursEntity utilisateur, ClassesEntity classe, String motif) {
        String email = utilisateur.getEmail();
        String nomComplet = nomComplet(utilisateur);
        String classeNom = classe.getNom();
        apresCommit(() -> emailService.envoyerRefus(email, nomComplet, classeNom, motif));
    }

    /**
     * Inscription publique d'un élève majeur par code de classe (nouvelle demande ou nouveau compte) : accusé de
     * réception SANS identifiants (envoyés à l'approbation), après commit.
     */
    public void envoyerAccuseInscriptionEleve(String email, String prenom, String nom, ClassesEntity classe) {
        if (email == null || email.isBlank()) return;
        String nomComplet = ((prenom == null ? "" : prenom) + " " + (nom == null ? "" : nom)).trim();
        String classeNom = classe.getNom();
        apresCommit(() -> emailService.envoyerInscriptionEleveRecue(email, nomComplet, classeNom));
    }

    String genererMotDePasseTemporaire() {
        List<Character> chars = new ArrayList<>();
        chars.add(pick(MAJUSCULES));
        chars.add(pick(MINUSCULES));
        chars.add(pick(CHIFFRES));
        chars.add(pick(SPECIAUX));
        String tous = MAJUSCULES + MINUSCULES + CHIFFRES + SPECIAUX;
        while (chars.size() < LONGUEUR_MOT_DE_PASSE) {
            chars.add(pick(tous));
        }
        Collections.shuffle(chars, random);
        StringBuilder sb = new StringBuilder();
        chars.forEach(sb::append);
        String mdp = sb.toString();
        cmr.notep.business.utils.PasswordPolicy.valider(mdp);
        return mdp;
    }

    private char pick(String alphabet) {
        return alphabet.charAt(random.nextInt(alphabet.length()));
    }

    private static String nomComplet(UtilisateursEntity u) {
        String p = u.getPrenom() == null ? "" : u.getPrenom();
        String n = u.getNom() == null ? "" : u.getNom();
        return (p + " " + n).trim();
    }

    /** Exécute l'action après le commit de la transaction courante (immédiatement s'il n'y en a pas). */
    static void apresCommit(Runnable action) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    action.run();
                }
            });
        } else {
            action.run();
        }
    }
}
