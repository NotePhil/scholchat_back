package cmr.notep.business.business;

import cmr.notep.business.exceptions.SchoolException;
import cmr.notep.business.exceptions.enums.SchoolErrorCode;
import cmr.notep.interfaces.modeles.Parents;
import cmr.notep.ressourcesjpa.commun.DaoAccessorService;
import cmr.notep.ressourcesjpa.dao.ParentsEntity;
import cmr.notep.ressourcesjpa.repository.ParentsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import static cmr.notep.business.config.BusinessConfig.dozerMapperBean;

@Component
@Slf4j
public class ParentsBusiness {
    private final DaoAccessorService daoAccessorService;

    public ParentsBusiness(DaoAccessorService daoAccessorService) {
        this.daoAccessorService = daoAccessorService;
    }

    public Parents avoirParent(String idParent) {
        log.info("avoirParent called");
        return dozerMapperBean.map(
                daoAccessorService.getRepository(ParentsRepository.class)
                        .findById(idParent)
                        .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND,"Parent introuvable avec l'ID: " + idParent)),
                Parents.class
        );
    }

    public Parents posterParent(Parents Parent) {
        return dozerMapperBean.map(
                this.daoAccessorService.getRepository(ParentsRepository.class)
                        .save(dozerMapperBean.map(Parent, ParentsEntity.class)),
                Parents.class
        );
    }

    public List<Parents> avoirToutParents() {
        return daoAccessorService.getRepository(ParentsRepository.class).findAll()
                .stream()
                .map(parent -> dozerMapperBean.map(parent, Parents.class))
                .collect(Collectors.toList());
    }
}