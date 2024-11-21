package cmr.notep.business.business;

import cmr.notep.interfaces.modeles.Professeur;
import cmr.notep.ressourcesjpa.dao.ProfesseurEntity;
import cmr.notep.ressourcesjpa.repository.ProfesseurRepository;
import cmr.notep.ressourcesjpa.commun.DaoAccessorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import static cmr.notep.business.config.BusinessConfig.dozerMapperBean;

@Component
@Slf4j
public class ProfesseurBusiness {

    private final DaoAccessorService daoAccessorService;

    public ProfesseurBusiness(DaoAccessorService daoAccessorService) {
        this.daoAccessorService = daoAccessorService;
    }

    public Professeur obtenirProfesseurParId(Long idProfesseur) {
        log.info("obtenirProfesseurParId called with ID: {}", idProfesseur);
        return dozerMapperBean.map(
                daoAccessorService.getRepository(ProfesseurRepository.class)
                        .findById(idProfesseur)
                        .orElseThrow(() -> new RuntimeException("Professeur introuvable")),
                Professeur.class
        );
    }

    public List<Professeur> obtenirTousLesProfesseurs() {
        log.info("obtenirTousLesProfesseurs called");
        return daoAccessorService.getRepository(ProfesseurRepository.class)
                .findAll()
                .stream()
                .map(professeurEntity -> dozerMapperBean.map(professeurEntity, Professeur.class))
                .collect(Collectors.toList());
    }

    public Professeur creerProfesseur(Professeur professeur) {
        log.info("creerProfesseur called with data: {}", professeur);
        if (professeur.getUrlCni() == null) {
            throw new RuntimeException("urlCni cannot be null");
        }
        ProfesseurEntity savedEntity = daoAccessorService.getRepository(ProfesseurRepository.class)
                .save(dozerMapperBean.map(professeur, ProfesseurEntity.class));
        return dozerMapperBean.map(savedEntity, Professeur.class);
    }
}
