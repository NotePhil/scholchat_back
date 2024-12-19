package cmr.notep.business.business;

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
        log.info("avoirUtilisateur called");
        return dozerMapperBean.map(daoAccessorService.getRepository(UtilisateursRepository.class)
                .findById(idUtilisateur)
                .orElseThrow(()-> new RuntimeException("Utilisateur introuvable")),Utilisateurs.class);
    }

    public Utilisateurs posterUtilisateur(Utilisateurs utilisateur) {
        return dozerMapperBean.map(this.daoAccessorService.getRepository(UtilisateursRepository.class)
                .save(dozerMapperBean.map(utilisateur, UtilisateursEntity.class)), Utilisateurs.class);

    }

    public List<Utilisateurs> avoirToutUtilisateurs() {
        return daoAccessorService.getRepository(UtilisateursRepository.class).findAll()
                .stream().map(msg -> dozerMapperBean.map(msg,Utilisateurs.class))
                .collect(Collectors.toList());
    }
}