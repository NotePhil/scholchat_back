package cmr.notep.business.business;

import cmr.notep.interfaces.modeles.ProfilEleves;
import cmr.notep.ressourcesjpa.commun.DaoAccessorService;
import cmr.notep.ressourcesjpa.dao.ProfilElevesEntity;
import cmr.notep.ressourcesjpa.repository.ProfilElevesRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import static cmr.notep.business.config.BusinessConfig.dozerMapperBean;

@Component
@Slf4j
public class ProfilElevesBusiness {
    private final DaoAccessorService daoAccessorService;

    public ProfilElevesBusiness(DaoAccessorService daoAccessorService) {
        this.daoAccessorService = daoAccessorService;
    }

    public ProfilEleves avoirProfilEleve(String idProfilEleve) {
        log.info("Fetching ProfilEleve with ID: {}", idProfilEleve);
        return dozerMapperBean.map(
                daoAccessorService.getRepository(ProfilElevesRepository.class)
                        .findById(idProfilEleve)
                        .orElseThrow(() -> new RuntimeException("ProfilEleve introuvable")),
                ProfilEleves.class
        );
    }

    public List<ProfilEleves> avoirToutProfilEleves() {
        return daoAccessorService.getRepository(ProfilElevesRepository.class).findAll()
                .stream()
                .map(entity -> dozerMapperBean.map(entity, ProfilEleves.class))
                .collect(Collectors.toList());
    }

    public ProfilEleves posterProfilEleve(ProfilEleves profilEleve) {
        return dozerMapperBean.map(
                daoAccessorService.getRepository(ProfilElevesRepository.class)
                        .save(dozerMapperBean.map(profilEleve, ProfilElevesEntity.class)),
                ProfilEleves.class
        );
    }
}
