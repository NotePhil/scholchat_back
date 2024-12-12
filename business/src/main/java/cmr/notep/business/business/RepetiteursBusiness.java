package cmr.notep.business.business;

import cmr.notep.interfaces.modeles.Repetiteurs;
import cmr.notep.ressourcesjpa.dao.RepetiteursEntity;
import cmr.notep.ressourcesjpa.repository.RepetiteursRepository;
import cmr.notep.ressourcesjpa.commun.DaoAccessorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import static cmr.notep.business.config.BusinessConfig.dozerMapperBean;

@Component
@Slf4j
public class RepetiteursBusiness {

    private final DaoAccessorService daoAccessorService;

    public RepetiteursBusiness(DaoAccessorService daoAccessorService) {
        this.daoAccessorService = daoAccessorService;
    }

    public Repetiteurs avoirRepetiteur(String idRepetiteur) {
        log.info("Fetching repetiteur by ID: {}", idRepetiteur);
        return daoAccessorService.getRepository(RepetiteursRepository.class)
                .findById(idRepetiteur)
                .map(entity -> dozerMapperBean.map(entity, Repetiteurs.class))
                .orElseThrow(() -> new RuntimeException("Repetiteur not found"));
    }

    public List<Repetiteurs> avoirTousRepetiteurs() {
        log.info("Fetching all repetiteurs...");
        return daoAccessorService.getRepository(RepetiteursRepository.class)
                .findAll()
                .stream()
                .map(entity -> dozerMapperBean.map(entity, Repetiteurs.class))
                .collect(Collectors.toList());
    }

    public Repetiteurs creerRepetiteur(Repetiteurs repetiteur) {
        log.info("Creating repetiteur: {}", repetiteur);

        if (repetiteur.getCniUrlFront() == null || repetiteur.getCniUrlBack() == null) {
            throw new RuntimeException("CNI URLs (front and back) cannot be null");
        }


        RepetiteursEntity entity = dozerMapperBean.map(repetiteur, RepetiteursEntity.class);
        RepetiteursEntity savedEntity = daoAccessorService.getRepository(RepetiteursRepository.class)
                .save(entity);

        return dozerMapperBean.map(savedEntity, Repetiteurs.class);
    }
}
