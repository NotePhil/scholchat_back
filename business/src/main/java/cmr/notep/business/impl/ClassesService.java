package cmr.notep.business.impl;

import cmr.notep.business.business.ClassesBusiness;
import cmr.notep.business.business.HistoActivationBusiness;
import cmr.notep.interfaces.api.ClassesApi;
import cmr.notep.interfaces.modeles.Classes;
import cmr.notep.interfaces.modeles.HistoActivation;
import cmr.notep.interfaces.modeles.Utilisateurs;
import cmr.notep.interfaces.dto.ClasseCreationDto;
import cmr.notep.interfaces.dto.ClasseCreationResponseDto;
import cmr.notep.modele.DroitPublication;
import cmr.notep.modele.EtatClasse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
public class ClassesService implements ClassesApi {
    @org.springframework.beans.factory.annotation.Autowired
    private cmr.notep.business.security.CurrentUserService currentUser;
    @org.springframework.beans.factory.annotation.Autowired
    private cmr.notep.business.security.AccessControlService accessControl;


    private final ClassesBusiness classesBusiness;
    private final HistoActivationBusiness histoActivationBusiness;

    // Deprecated - Use creerNouvelleClasse instead
    @Override
    @Deprecated
    public Classes creerClasse(@NonNull Classes classes) {
        currentUser.requireAuthenticated();
        if (!currentUser.isAdmin() && !currentUser.hasRole("PROFESSOR") && !currentUser.hasRole("GESTIONNAIRE")) {
            throw cmr.notep.business.security.CurrentUserService.forbidden("Seuls les professeurs, gestionnaires d'établissement et administrateurs peuvent créer une classe.");
        }
        log.warn("DEPRECATED: Using old endpoint POST /classes. Use POST /classes/nouvelle instead");
        log.info("Tentative de création d'une nouvelle classe: {}", classes);
        Classes nouvelleClasse = classesBusiness.creerClasse(classes);
        log.info("Classe créée avec succès: {}", nouvelleClasse.getId());
        return nouvelleClasse;
    }

    @Override
    public ClasseCreationResponseDto creerNouvelleClasse(@NonNull ClasseCreationDto classeDto) {
        currentUser.requireAuthenticated();
        if (!currentUser.isAdmin() && !currentUser.hasRole("PROFESSOR") && !currentUser.hasRole("GESTIONNAIRE")) {
            throw cmr.notep.business.security.CurrentUserService.forbidden("Seuls les professeurs, gestionnaires d'établissement et administrateurs peuvent créer une classe.");
        }
        log.info("Tentative de création d'une nouvelle classe avec DTO: {}", classeDto);
        
        // Get connected user from security context
        // (le principal porte l'email : on résout l'id applicatif)
        String connectedUserId = currentUser.requireUserId();
        
        // Check if connected user is a professor
        boolean isProfessor = classesBusiness.isProfessor(connectedUserId);
        
        // If no moderatorId provided and user is professor, use connected user
        if (isProfessor && (classeDto.getModeratorId() == null || classeDto.getModeratorId().trim().isEmpty())) {
            classeDto.setModeratorId(connectedUserId);
            log.info("Professor creating class, set as moderator: {}", connectedUserId);
        }
        
        ClasseCreationResponseDto response = classesBusiness.creerNouvelleClasse(classeDto);
        log.info("Classe créée avec succès: {}", response.getClasse().getId());
        return response;
    }

//    @Override
//    public List<Utilisateurs> obtenirUtilisateursParClasse(String idClasse) {
//        log.info("Récupération des utilisateurs pour la classe avec l'ID: {}", idClasse);
//        List<Utilisateurs> utilisateurs = classesBusiness.obtenirUtilisateursParClasse(idClasse);
//        log.info("Récupération de {} utilisateurs pour la classe avec l'ID: {}", utilisateurs.size(), idClasse);
//        return utilisateurs;
//    }

    @Override
    public Classes modifierClasse(@NonNull String idClasse, @NonNull Classes classeModifiee) {
        accessControl.requireClassManager(idClasse);
        log.info("Tentative de modification de la classe avec l'ID: {}", idClasse);
        classeModifiee.setId(idClasse);
        Classes classeMAJ = classesBusiness.modifierClasse(idClasse, classeModifiee);
        log.info("Classe modifiée avec succès: {}", classeMAJ.getId());
        return classeMAJ;
    }

    @Override
    public Classes approuverClasse(@NonNull String idClasse) {
        accessControl.requireClassApprover(idClasse);
        log.info("Approbation de la classe avec l'ID: {}", idClasse);
        Classes classeApprouvee = classesBusiness.approuverClasse(idClasse);
        log.info("Classe approuvée avec succès: {}", idClasse);
        return classeApprouvee;
    }

    @Override
    public Classes rejeterClasse(@NonNull String idClasse, @NonNull String motif) {
        accessControl.requireClassApprover(idClasse);
        log.info("Rejet de la classe avec l'ID: {}", idClasse);
        Classes classeRejetee = classesBusiness.rejeterClasse(idClasse, motif);
        log.info("Classe rejetée avec succès: {}", idClasse);
        return classeRejetee;
    }

    @Override
    public List<Classes> obtenirClassesParEtat(@NonNull EtatClasse etat) {
        currentUser.requireAdmin();
        log.info("Récupération des classes avec l'état: {}", etat);
        List<Classes> classes = classesBusiness.obtenirClassesParEtat(etat);
        log.info("Récupération de {} classes avec l'état {}", classes.size(), etat);
        return classes;
    }

    @Override
    public void supprimerClasse(@NonNull String idClasse) {
        accessControl.requireClassManager(idClasse);
        log.info("Tentative de suppression de la classe avec l'ID: {}", idClasse);
        classesBusiness.supprimerClasse(idClasse);
        log.info("Classe supprimée avec succès: {}", idClasse);
    }

    @Override
    public Classes obtenirClasseParId(@NonNull String idClasse) {
        log.info("Récupération de la classe avec l'ID: {}", idClasse);
        return classesBusiness.obtenirClasseParId(idClasse);
    }

    @Override
    public List<Classes> obtenirToutesLesClasses() {
        log.info("Récupération de toutes les classes");
        boolean isAdmin = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication() != null
                && org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                        .stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        List<Classes> classes = classesBusiness.obtenirToutesLesClasses(isAdmin);
        log.info("Récupération de {} classes", classes.size());
        return classes;
    }

    @Override
    public Classes obtenirClasseParCode(@NonNull String code) {
        log.info("Récupération de la classe avec le code d'activation: {}", code);
        return classesBusiness.obtenirClasseParCodeActivation(code);
    }

    @Override
    public Classes modifierDroitPublication(String idClasse, DroitPublication droitPublication) {
        accessControl.requireClassManager(idClasse);
        log.info("Modification du droit de publication pour la classe: {}", idClasse);
        return classesBusiness.modifierDroitPublication(idClasse, droitPublication);
    }

    @Override
    public List<HistoActivation> obtenirHistoriqueActivation(String idClasse) {
        accessControl.requireClassMember(idClasse);
        log.info("Récupération de l'historique d'activation pour la classe: {}", idClasse);
        return histoActivationBusiness.obtenirHistoriqueParClasse(idClasse);
    }

    @Override
    public Classes assignerModerator(@NonNull String idClasse, @NonNull String idModerator) {
        accessControl.requireClassManager(idClasse);
        log.info("Attribution du modérateur {} à la classe {}", idModerator, idClasse);
        Classes classeMAJ = classesBusiness.assignerModerator(idClasse, idModerator);
        log.info("Modérateur assigné avec succès à la classe: {}", idClasse);
        return classeMAJ;
    }

    @Override
    public Classes retirerModerator(@NonNull String idClasse) {
        accessControl.requireClassManager(idClasse);
        log.info("Retrait du modérateur de la classe: {}", idClasse);
        Classes classeMAJ = classesBusiness.retirerModerator(idClasse);
        log.info("Modérateur retiré avec succès de la classe: {}", idClasse);
        return classeMAJ;
    }

    @Override
    public List<Utilisateurs> obtenirModerateursDeLaClasse(@NonNull String idClasse) {
        log.info("Récupération des modérateurs de la classe: {}", idClasse);
        List<Utilisateurs> moderateurs = classesBusiness.obtenirModerateursDeLaClasse(idClasse);
        log.info("Récupération de {} modérateurs pour la classe: {}", moderateurs.size(), idClasse);
        return moderateurs;
    }

    @Override
    public void approuverClasseParEtablissement(@NonNull String classeId, @NonNull String etablissementId) {
        accessControl.requireClassDecisionByEtablissement(classeId, etablissementId);
        log.info("Approbation de la classe {} par l'établissement {}", classeId, etablissementId);
        classesBusiness.approuverClasseParEtablissement(classeId, etablissementId);
        log.info("Classe {} approuvée avec succès", classeId);
    }

    @Override
    public void rejeterClasseParEtablissement(@NonNull String classeId, @NonNull String etablissementId) {
        accessControl.requireClassDecisionByEtablissement(classeId, etablissementId);
        log.info("Rejet de la classe {} par l'établissement {}", classeId, etablissementId);
        classesBusiness.rejeterClasseParEtablissement(classeId, etablissementId);
        log.info("Classe {} rejetée avec succès", classeId);
    }
}