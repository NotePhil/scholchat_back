package cmr.notep.business.impl;

import cmr.notep.business.business.GestionnairesBusiness;
import cmr.notep.interfaces.api.GestionnairesApi;
import cmr.notep.interfaces.modeles.Gestionnaires;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
public class GestionnairesService implements GestionnairesApi {
    private final GestionnairesBusiness gestionnairesBusiness;

    public GestionnairesService(GestionnairesBusiness gestionnairesBusiness) {
        this.gestionnairesBusiness = gestionnairesBusiness;
    }

    @Override
    public Gestionnaires avoirGestionnaire(String idGestionnaire) {
        log.info("Récupération du gestionnaire avec ID: {}", idGestionnaire);
        return gestionnairesBusiness.avoirGestionnaire(idGestionnaire);
    }

    @Override
    public List<Gestionnaires> avoirTousGestionnaires() {
        log.info("Récupération de tous les gestionnaires");
        return gestionnairesBusiness.avoirTousGestionnaires();
    }
}
