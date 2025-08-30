package cmr.notep.business.impl;

import cmr.notep.business.business.DroitPublicationBusiness;
import cmr.notep.interfaces.api.DroitPublicationApi;
import cmr.notep.interfaces.modeles.Classes;
import cmr.notep.interfaces.modeles.Utilisateurs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
public class DroitPublicationService implements DroitPublicationApi {

    private final DroitPublicationBusiness droitPublicationBusiness;

    @Override
    public void attribuerDroitPublication(String utilisateurId, String classeId, boolean peutPublier, boolean peutModerer) {
        log.info("API - Assigning publication rights to user {} for class {}", utilisateurId, classeId);
        droitPublicationBusiness.attribuerDroitPublication(utilisateurId, classeId, peutPublier, peutModerer);
    }

    @Override
    public void modifierDroitPublication(String utilisateurId, String classeId, boolean peutPublier, boolean peutModerer) {
        log.info("API - Updating publication rights for user {} in class {}", utilisateurId, classeId);
        droitPublicationBusiness.modifierDroitPublication(utilisateurId, classeId, peutPublier, peutModerer);
    }

    @Override
    public void retirerDroitPublication(String utilisateurId, String classeId) {
        log.info("API - Removing publication rights from user {} for class {}", utilisateurId, classeId);
        droitPublicationBusiness.retirerDroitPublication(utilisateurId, classeId);
    }

    @Override
    public List<Utilisateurs> obtenirUtilisateursAvecDroitPublication(String classeId) {
        log.info("API - Getting users with publication rights for class {}", classeId);
        return droitPublicationBusiness.obtenirUtilisateursAvecDroitPublication(classeId);
    }

    @Override
    public List<Classes> obtenirClassesAvecDroitPublication(String utilisateurId) {
        log.info("API - Getting classes with publication rights for user {}", utilisateurId);
        return droitPublicationBusiness.obtenirClassesAvecDroitPublication(utilisateurId);
    }
}