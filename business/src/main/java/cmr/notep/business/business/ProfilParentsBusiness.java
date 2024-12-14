package cmr.notep.business.business;

import cmr.notep.interfaces.modeles.ProfilParents;
import cmr.notep.ressourcesjpa.commun.DaoAccessorService;
import cmr.notep.ressourcesjpa.dao.ProfilParentsEntity;
import cmr.notep.ressourcesjpa.repository.ProfilParentsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import static cmr.notep.business.config.BusinessConfig.dozerMapperBean;

@Component
@Slf4j
public class ProfilParentsBusiness {
    private final DaoAccessorService daoAccessorService;

    public ProfilParentsBusiness(DaoAccessorService daoAccessorService) {
        this.daoAccessorService = daoAccessorService;
    }

    public ProfilParents avoirProfilParent(String idProfilParent) {
        log.info("avoirProfilParent called");
        return dozerMapperBean.map(
                daoAccessorService.getRepository(ProfilParentsRepository.class)
                        .findById(idProfilParent)
                        .orElseThrow(() -> new RuntimeException("ProfilParent introuvable")),
                ProfilParents.class
        );
    }

    public ProfilParents posterProfilParent(ProfilParents profilParent) {
        return dozerMapperBean.map(
                this.daoAccessorService.getRepository(ProfilParentsRepository.class)
                        .save(dozerMapperBean.map(profilParent, ProfilParentsEntity.class)),
                ProfilParents.class
        );
    }

    public List<ProfilParents> avoirToutProfilParents() {
        return daoAccessorService.getRepository(ProfilParentsRepository.class).findAll()
                .stream()
                .map(parent -> dozerMapperBean.map(parent, ProfilParents.class))
                .collect(Collectors.toList());
    }
}