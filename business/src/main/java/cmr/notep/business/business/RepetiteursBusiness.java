package cmr.notep.business.business;

import cmr.notep.interfaces.modeles.Repetiteurs;
import cmr.notep.ressourcesjpa.commun.DaoAccessorService;
import cmr.notep.ressourcesjpa.dao.RepetiteursEntity;
import cmr.notep.ressourcesjpa.repository.RepetiteursRepository;
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
        log.info("Fetching Répétiteur with ID: {}", idRepetiteur);
        return dozerMapperBean.map(
                daoAccessorService.getRepository(RepetiteursRepository.class)
                        .findById(idRepetiteur)
                        .orElseThrow(() -> new RuntimeException("Répétiteur introuvable")),
                Repetiteurs.class
        );
    }

    public Repetiteurs posterRepetiteur(Repetiteurs repetiteur) {
        log.info("Posting a new Répétiteur: {}", repetiteur);
        return dozerMapperBean.map(
                this.daoAccessorService.getRepository(RepetiteursRepository.class)
                        .save(dozerMapperBean.map(repetiteur, RepetiteursEntity.class)),
                Repetiteurs.class
        );
    }

    public List<Repetiteurs> avoirToutRepetiteurs() {
        log.info("Fetching all Répétiteurs");
        return daoAccessorService.getRepository(RepetiteursRepository.class).findAll()
                .stream()
                .map(rep -> dozerMapperBean.map(rep, Repetiteurs.class))
                .collect(Collectors.toList());
    }
}
