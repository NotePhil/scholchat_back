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
    @org.springframework.beans.factory.annotation.Autowired
    private cmr.notep.business.security.CurrentUserService currentUser;
    @org.springframework.beans.factory.annotation.Autowired
    private cmr.notep.business.security.AccessControlService accessControl;

    private final GestionnairesBusiness gestionnairesBusiness;

    public GestionnairesService(GestionnairesBusiness gestionnairesBusiness) {
        this.gestionnairesBusiness = gestionnairesBusiness;
    }

    @Override
    public Gestionnaires avoirGestionnaire(String idGestionnaire) {
        currentUser.requireAuthenticated();
        log.info("Récupération du gestionnaire avec ID: {}", idGestionnaire);
        return gestionnairesBusiness.avoirGestionnaire(idGestionnaire);
    }

    @Override
    public List<Gestionnaires> avoirTousGestionnaires() {
        currentUser.requireAdmin();
        log.info("Récupération de tous les gestionnaires");
        return gestionnairesBusiness.avoirTousGestionnaires();
    }
}
