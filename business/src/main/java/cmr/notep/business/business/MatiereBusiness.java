package cmr.notep.business.business;

import cmr.notep.business.exceptions.SchoolException;
import cmr.notep.business.exceptions.enums.SchoolErrorCode;
import cmr.notep.interfaces.modeles.Matiere;
import cmr.notep.modele.NomMatiere;
import cmr.notep.ressourcesjpa.commun.DaoAccessorService;
import cmr.notep.ressourcesjpa.dao.MatiereEntity;
import cmr.notep.ressourcesjpa.repository.MatiereRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import static cmr.notep.business.config.BusinessConfig.dozerMapperBean;

@Component
@Slf4j
public class MatiereBusiness {

    private final DaoAccessorService daoAccessorService;

    public MatiereBusiness(DaoAccessorService daoAccessorService) {
        this.daoAccessorService = daoAccessorService;
    }

    public Matiere creerMatiere(Matiere matiere) {
        MatiereEntity entity = dozerMapperBean.map(matiere, MatiereEntity.class);
        MatiereEntity savedEntity = daoAccessorService.getRepository(MatiereRepository.class).save(entity);
        return dozerMapperBean.map(savedEntity, Matiere.class);
    }

    public List<Matiere> obtenirToutesMatieres() {
        return daoAccessorService.getRepository(MatiereRepository.class).findAll()
                .stream()
                .map(m -> dozerMapperBean.map(m, Matiere.class))
                .collect(Collectors.toList());
    }

    public Matiere obtenirMatiereParNom(String nomMatiere) {
        MatiereEntity entity = daoAccessorService.getRepository(MatiereRepository.class)
                .findByNom(NomMatiere.valueOf(nomMatiere))
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Matière non trouvée: " + nomMatiere));
        return dozerMapperBean.map(entity, Matiere.class);
    }

}