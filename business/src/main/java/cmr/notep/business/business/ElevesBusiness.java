package cmr.notep.business.business;

import cmr.notep.interfaces.modeles.Eleves;
import cmr.notep.ressourcesjpa.commun.DaoAccessorService;
import cmr.notep.ressourcesjpa.dao.ElevesEntity;
import cmr.notep.ressourcesjpa.repository.ElevesRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import static cmr.notep.business.config.BusinessConfig.dozerMapperBean;

@Component
@Slf4j
public class ElevesBusiness {
    private final DaoAccessorService daoAccessorService;

    public ElevesBusiness(DaoAccessorService daoAccessorService) {
        this.daoAccessorService = daoAccessorService;
    }

    public Eleves avoirEleve(String idEleve) {
        log.info("Fetching eleve with ID: {}", idEleve);
        return dozerMapperBean.map(
                daoAccessorService.getRepository(ElevesRepository.class)
                        .findById(idEleve)
                        .orElseThrow(() -> new RuntimeException("eleve introuvable")),
                Eleves.class
        );
    }

    public List<Eleves> avoirToutEleves() {
        return daoAccessorService.getRepository(ElevesRepository.class).findAll()
                .stream()
                .map(entity -> dozerMapperBean.map(entity, Eleves.class))
                .collect(Collectors.toList());
    }

    public Eleves posterEleve(Eleves eleve) {
        return dozerMapperBean.map(
                daoAccessorService.getRepository(ElevesRepository.class)
                        .save(dozerMapperBean.map(eleve, ElevesEntity.class)),
                Eleves.class
        );
    }
}
