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

    private final AccederBusiness accederBusiness;

    @Override
    public void demanderAcces(String utilisateurId, String classeId, String codeActivation, boolean estParent, String eleveAssocieId) {
        log.info("API - Demande d'accès de l'utilisateur {} à la classe {} (Parent: {}, Enfant: {})", 
                utilisateurId, classeId, estParent, eleveAssocieId);
        accederBusiness.demanderAcces(utilisateurId, classeId, codeActivation, estParent, eleveAssocieId);
    }

    @Override
    public void validerDemandeAcces(String demandeId) {
        log.info("API - Validation de la demande d'accès {}", demandeId);
        accederBusiness.validerDemandeAcces(demandeId);
    }
    // Dans AccederService.java
    @Override
    public List<DemandeAccesDto> obtenirDemandesAccesPourModerateur(String moderatorId) {
        log.info("API - Obtenir les demandes d'accès pour le modérateur {}", moderatorId);
        return accederBusiness.obtenirDemandesAccesPourModerateur(moderatorId);
    }

    @Override
    public void rejeterDemandeAcces(String demandeId, String motifRejet) {
        log.info("API - Rejet de la demande d'accès {}", demandeId);
        accederBusiness.rejeterDemandeAcces(demandeId, motifRejet);
    }

    @Override
    public void retirerAcces(String utilisateurId, String classeId) {
        log.info("API - Retirer l'accès de l'utilisateur {} à la classe {}", utilisateurId, classeId);
        accederBusiness.retirerAcces(utilisateurId, classeId);
    }

    @Override
    public List<UtilisateurSimpleDto> obtenirUtilisateursAvecAcces(String classeId) {
        log.info("API - Obtenir les utilisateurs ayant accès à la classe {}", classeId);
        return accederBusiness.obtenirUtilisateursAvecAccesSimple(classeId);
    }

    @Override
    public List<Classes> obtenirClassesAccessibles(String utilisateurId) {
        log.info("API - Obtenir les classes accessibles par l'utilisateur {}", utilisateurId);
        return accederBusiness.obtenirClassesAccessibles(utilisateurId);
    }

    @Override
    public List<DemandeAccesDto> obtenirDemandesAccesPourClasse(String classeId) {
        log.info("API - Obtenir les demandes d'accès pour la classe {}", classeId);
        return accederBusiness.obtenirDemandesAccesPourClasse(classeId);
    }

    @Override
    public List<UtilisateurSimpleDto> obtenirUtilisateursAvecAcces(List<String> classeIds) {
        log.info("API - Obtenir les utilisateurs ayant accès aux classes {}", classeIds);
        return accederBusiness.obtenirUtilisateursAvecAccesSimple(classeIds);
    }
}