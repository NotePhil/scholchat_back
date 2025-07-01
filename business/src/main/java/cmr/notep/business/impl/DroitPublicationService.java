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
        log.info("API - Attribuer droit de publication à {} pour la classe {}", utilisateurId, classeId);
        droitPublicationBusiness.attribuerDroitPublication(utilisateurId, classeId, peutPublier, peutModerer);
    }

    @Override
    public void modifierDroitPublication(String utilisateurId, String classeId, boolean peutPublier, boolean peutModerer) {
        log.info("API - Modifier droit de publication pour {} dans la classe {}", utilisateurId, classeId);
        droitPublicationBusiness.modifierDroitPublication(utilisateurId, classeId, peutPublier, peutModerer);
    }

    @Override
    public void retirerDroitPublication(String utilisateurId, String classeId) {
        log.info("API - Retirer droit de publication de {} pour la classe {}", utilisateurId, classeId);
        droitPublicationBusiness.retirerDroitPublication(utilisateurId, classeId);
    }

    @Override
    public List<Utilisateurs> obtenirUtilisateursAvecDroitPublication(String classeId) {
        log.info("API - Obtenir utilisateurs avec droits de publication pour la classe {}", classeId);
        return droitPublicationBusiness.obtenirUtilisateursAvecDroitPublication(classeId);
    }

    @Override
    public List<Classes> obtenirClassesAvecDroitPublication(String utilisateurId) {
        log.info("API - Obtenir classes avec droits de publication pour l'utilisateur {}", utilisateurId);
        return droitPublicationBusiness.obtenirClassesAvecDroitPublication(utilisateurId);
    }
}