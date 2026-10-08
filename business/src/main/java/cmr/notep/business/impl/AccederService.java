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
        return accederBusiness.obtenirClassesAccessibles(utilisateurId);
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
        return accederBusiness.obtenirDemandesAccesDeUtilisateur(utilisateurId);
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