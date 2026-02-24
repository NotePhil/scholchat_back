package cmr.notep.business.business;

import cmr.notep.business.exceptions.SchoolException;
import cmr.notep.business.exceptions.enums.SchoolErrorCode;
import cmr.notep.interfaces.modeles.Gestionnaires;
import cmr.notep.ressourcesjpa.commun.DaoAccessorService;
import cmr.notep.ressourcesjpa.dao.GestionnairesEntity;
import cmr.notep.ressourcesjpa.repository.GestionnairesRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static cmr.notep.business.config.BusinessConfig.dozerMapperBean;

@Component
@Slf4j
@Transactional(noRollbackFor = SchoolException.class)
public class GestionnairesBusiness {
    private final DaoAccessorService daoAccessorService;

    public GestionnairesBusiness(DaoAccessorService daoAccessorService) {
        this.daoAccessorService = daoAccessorService;
    }

    public Gestionnaires avoirGestionnaire(String idGestionnaire) {
        log.info("Récupération du gestionnaire avec ID: {}", idGestionnaire);
        GestionnairesEntity gestionnaireEntity = daoAccessorService.getRepository(GestionnairesRepository.class)
                .findById(idGestionnaire)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, 
                        "Gestionnaire introuvable avec l'ID: " + idGestionnaire));
        return dozerMapperBean.map(gestionnaireEntity, Gestionnaires.class);
    }

    public List<Gestionnaires> avoirTousGestionnaires() {
        log.info("Récupération de tous les gestionnaires");
        return daoAccessorService.getRepository(GestionnairesRepository.class).findAll()
                .stream()
                .map(entity -> dozerMapperBean.map(entity, Gestionnaires.class))
                .collect(Collectors.toList());
    }
}
