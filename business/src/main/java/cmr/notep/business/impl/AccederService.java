package cmr.notep.business.impl;

import cmr.notep.business.business.AccederBusiness;
import cmr.notep.interfaces.api.AccederApi;
import cmr.notep.interfaces.modeles.Classes;
import cmr.notep.interfaces.modeles.DemandeAccesDto;
import cmr.notep.interfaces.modeles.UtilisateurSimpleDto;
import cmr.notep.interfaces.modeles.Utilisateurs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
public class AccederService implements AccederApi {
    @org.springframework.beans.factory.annotation.Autowired
    private cmr.notep.business.security.CurrentUserService currentUser;
    @org.springframework.beans.factory.annotation.Autowired
    private cmr.notep.business.security.AccessControlService accessControl;


    private final AccederBusiness accederBusiness;

    @Override
    public void demanderAcces(String utilisateurId, String classeId, String codeActivation, boolean estParent, String eleveAssocieId) {
        currentUser.requireSelfOrAdmin(utilisateurId);
        // Demande pour un enfant : seulement un enfant rattaché ET mineur (sans compte) ; un enfant majeur
        // demande lui-même l'accès depuis son compte.
        if (estParent && eleveAssocieId != null && !eleveAssocieId.isBlank() && !currentUser.isAdmin()) {
            if (!accessControl.isParentOf(utilisateurId, eleveAssocieId)) {
                throw cmr.notep.business.security.CurrentUserService.forbidden("Cet élève n'est pas rattaché à votre compte.");
            }
            if (!accessControl.isMinorChildAccount(eleveAssocieId)) {
                throw cmr.notep.business.security.CurrentUserService.forbidden(
                        cmr.notep.business.security.AccessControlService.MSG_ENFANT_MAJEUR_LECTURE_SEULE);
            }
        }
        log.info("API - Demande d'accès de l'utilisateur {} à la classe {} (Parent: {}, Enfant: {})", 
                utilisateurId, classeId, estParent, eleveAssocieId);
        accederBusiness.demanderAcces(utilisateurId, classeId, codeActivation, estParent, eleveAssocieId);
    }

    @Override
    public void validerDemandeAcces(String demandeId) {
        accessControl.requireDemandeManager(demandeId);
        log.info("API - Validation de la demande d'accès {}", demandeId);
        accederBusiness.validerDemandeAcces(demandeId);
    }
    // Dans AccederService.java
    @Override
    public List<DemandeAccesDto> obtenirDemandesAccesPourModerateur(String moderatorId) {
        currentUser.requireSelfOrAdmin(moderatorId);
        log.info("API - Obtenir les demandes d'accès pour le modérateur {}", moderatorId);
        return accederBusiness.obtenirDemandesAccesPourModerateur(moderatorId);
    }

    @Override
    public void rejeterDemandeAcces(String demandeId, String motifRejet) {
        accessControl.requireDemandeManager(demandeId);
        log.info("API - Rejet de la demande d'accès {}", demandeId);
        accederBusiness.rejeterDemandeAcces(demandeId, motifRejet);
    }

    @Override
    public void retirerAcces(String utilisateurId, String classeId) {
        accessControl.requireCanRemoveAccess(utilisateurId, classeId);
        log.info("API - Retirer l'accès de l'utilisateur {} à la classe {}", utilisateurId, classeId);
        accederBusiness.retirerAcces(utilisateurId, classeId);
    }

    @Override
    public List<UtilisateurSimpleDto> obtenirUtilisateursAvecAcces(String classeId) {
        accessControl.requireClassMember(classeId);
        log.info("API - Obtenir les utilisateurs ayant accès à la classe {}", classeId);
        // Détails (état, coordonnées, date de création) réservés aux gestionnaires de la classe
        return accederBusiness.obtenirUtilisateursAvecAccesSimple(classeId, canSeeMemberDetails(classeId));
    }

    @Override
    public List<Classes> obtenirClassesAccessibles(String utilisateurId) {
        accessControl.requireSelfOrParentOrAdmin(utilisateurId);
        log.info("API - Obtenir les classes accessibles par l'utilisateur {}", utilisateurId);
        java.util.Set<String> lisibles = accessControl.classesLisiblesParParent(utilisateurId);
        List<Classes> classes = accederBusiness.obtenirClassesAccessibles(utilisateurId);
        // Parent d'un enfant majeur : seulement les classes dont le suivi est accepté
        return lisibles == null ? classes : classes.stream().filter(c -> lisibles.contains(c.getId())).toList();
    }

    @Override
    public List<DemandeAccesDto> obtenirDemandesAccesPourClasse(String classeId) {
        accessControl.requireClassManager(classeId);
        log.info("API - Obtenir les demandes d'accès pour la classe {}", classeId);
        return accederBusiness.obtenirDemandesAccesPourClasse(classeId);
    }

    @Override
    public List<DemandeAccesDto> obtenirDemandesAccesDeUtilisateur(String utilisateurId) {
        accessControl.requireSelfOrParentOrAdmin(utilisateurId);
        log.info("API - Obtenir les demandes d'accès de l'utilisateur {}", utilisateurId);
        java.util.Set<String> lisibles = accessControl.classesLisiblesParParent(utilisateurId);
        List<DemandeAccesDto> demandes = accederBusiness.obtenirDemandesAccesDeUtilisateur(utilisateurId);
        return lisibles == null ? demandes : demandes.stream().filter(d -> lisibles.contains(d.getClasseId())).toList();
    }

    @Override
    public List<UtilisateurSimpleDto> obtenirUtilisateursAvecAcces(List<String> classeIds) {
        if (classeIds != null) classeIds.forEach(accessControl::requireClassMember);
        log.info("API - Obtenir les utilisateurs ayant accès aux classes {}", classeIds);
        boolean details = classeIds != null && !classeIds.isEmpty()
                && classeIds.stream().allMatch(this::canSeeMemberDetails);
        return accederBusiness.obtenirUtilisateursAvecAccesSimple(classeIds, details);
    }

    private boolean canSeeMemberDetails(String classeId) {
        return currentUser.isAdmin()
                || currentUser.currentUserIdOpt().map(me -> accessControl.isClassManager(classeId, me)).orElse(false);
    }
}