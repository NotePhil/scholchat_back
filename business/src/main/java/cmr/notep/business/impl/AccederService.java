package cmr.notep.business.impl;

import cmr.notep.business.business.AccederBusiness;
import cmr.notep.interfaces.api.AccederApi;
import cmr.notep.interfaces.modeles.Classes;
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
    public void donnerAcces(String utilisateurId, String classeId) {
        log.info("API - Donner accès à l'utilisateur {} à la classe {}", utilisateurId, classeId);
        accederBusiness.donnerAcces(utilisateurId, classeId);
    }

    @Override
    public void retirerAcces(String utilisateurId, String classeId) {
        log.info("API - Retirer l'accès de l'utilisateur {} à la classe {}", utilisateurId, classeId);
        accederBusiness.retirerAcces(utilisateurId, classeId);
    }

    @Override
    public List<Utilisateurs> obtenirUtilisateursAvecAcces(String classeId) {
        log.info("API - Obtenir les utilisateurs ayant accès à la classe {}", classeId);
        return accederBusiness.obtenirUtilisateursAvecAcces(classeId);
    }

    @Override
    public List<Classes> obtenirClassesAccessibles(String utilisateurId) {
        log.info("API - Obtenir les classes accessibles par l'utilisateur {}", utilisateurId);
        return accederBusiness.obtenirClassesAccessibles(utilisateurId);
    }
}
