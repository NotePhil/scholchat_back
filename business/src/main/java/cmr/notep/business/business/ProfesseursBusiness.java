package cmr.notep.business.business;

import cmr.notep.interfaces.modeles.Professeurs;
import cmr.notep.ressourcesjpa.dao.ProfesseursEntity;
import cmr.notep.ressourcesjpa.repository.ProfesseursRepository;
import cmr.notep.ressourcesjpa.commun.DaoAccessorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import static cmr.notep.business.config.BusinessConfig.dozerMapperBean;

@Component
@Slf4j
public class ProfesseursBusiness {

    private final DaoAccessorService daoAccessorService;

    public ProfesseursBusiness(DaoAccessorService daoAccessorService) {
        this.daoAccessorService = daoAccessorService;
    }

    public Professeurs avoirProfesseur(Long idProfesseur) {
        log.info("Fetching professor by ID: {}", idProfesseur);
        return dozerMapperBean.map(
                daoAccessorService.getRepository(ProfesseursRepository.class)
                        .findById(idProfesseur)
                        .orElseThrow(() -> new RuntimeException("Professor not found")),
                Professeurs.class
        );
    }

    public List<Professeurs> avoirTousProfesseur() {
        log.info("Fetching all professors...");
        return daoAccessorService.getRepository(ProfesseursRepository.class)
                .findAll()
                .stream()
                .map(professeurEntity -> dozerMapperBean.map(professeurEntity, Professeurs.class))
                .collect(Collectors.toList());
    }
    public Professeurs creerProfesseur(Professeurs professeur) {
        log.info("Creating professor: {}", professeur);

        // Vérifie que le champ obligatoire `urlCni` est présent
        if (professeur.getUrlCni() == null) {
            throw new RuntimeException("urlCni cannot be null");
        }

        ProfesseursEntity savedEntity = daoAccessorService.getRepository(ProfesseursRepository.class)
                .save(dozerMapperBean.map(professeur, ProfesseursEntity.class));
        return dozerMapperBean.map(savedEntity, Professeurs.class);
    }
}
