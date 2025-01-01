package cmr.notep.business.business;

import cmr.notep.business.exceptions.SchoolException;
import cmr.notep.business.exceptions.enums.SchoolErrorCode;
import cmr.notep.interfaces.modeles.Utilisateurs;
import cmr.notep.ressourcesjpa.commun.DaoAccessorService;
import cmr.notep.ressourcesjpa.dao.UtilisateursEntity;
import cmr.notep.ressourcesjpa.repository.UtilisateursRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import static cmr.notep.business.config.BusinessConfig.dozerMapperBean;

@Component
@Slf4j
public class UtilisateursBusiness {
    private final DaoAccessorService daoAccessorService ;

    public UtilisateursBusiness(DaoAccessorService daoAccessorService) {
        this.daoAccessorService = daoAccessorService;
    }

    public Utilisateurs avoirUtilisateur(String idUtilisateur) {
        log.info("Récupération de l'utilisateur avec ID: {}", idUtilisateur);
        return dozerMapperBean.map(daoAccessorService.getRepository(UtilisateursRepository.class)
                .findById(idUtilisateur)
                .orElseThrow(()-> new SchoolException(SchoolErrorCode.NOT_FOUND, "Utilisateur introuvable avec l'ID: " + idUtilisateur)),Utilisateurs.class);
    }
    
    public Utilisateurs posterUtilisateur(Utilisateurs utilisateur) {
        log.info("Création d'un nouvel utilisateur");
        UtilisateursEntity savedEntity = daoAccessorService.getRepository(UtilisateursRepository.class)
                .save(dozerMapperBean.map(utilisateur, UtilisateursEntity.class));
        return dozerMapperBean.map(savedEntity, Utilisateurs.class);

    }
    
    public List<Utilisateurs> avoirToutUtilisateurs() {
        log.info("Récupération de tous les utilisateurs");
        return daoAccessorService.getRepository(UtilisateursRepository.class).findAll()
                .stream().map(msg -> dozerMapperBean.map(msg,Utilisateurs.class))
                .collect(Collectors.toList());
    }
}
