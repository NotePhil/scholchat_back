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

    public Professeurs avoirProfesseur(String idProfesseur) {
        log.info("Fetching professor by ID: {}", idProfesseur);
        return daoAccessorService.getRepository(ProfesseursRepository.class)
                .findById(idProfesseur)
                .map(entity -> dozerMapperBean.map(entity, Professeurs.class))
                .orElseThrow(() -> new RuntimeException("Professor not found"));
    }

    public List<Professeurs> avoirTousProfesseur() {
        log.info("Fetching all professors...");
        return daoAccessorService.getRepository(ProfesseursRepository.class)
                .findAll()
                .stream()
                .map(entity -> dozerMapperBean.map(entity, Professeurs.class))
                .collect(Collectors.toList());
    }

    public Professeurs creerProfesseur(Professeurs professeur) {
        log.info("Creating professor: {}", professeur);

        if (professeur.getCniUrlFront() == null || professeur.getCniUrlBack() == null) {
            throw new RuntimeException("CNI URLs (front and back) cannot be null");
        }


        ProfesseursEntity entity = dozerMapperBean.map(professeur, ProfesseursEntity.class);
        ProfesseursEntity savedEntity = daoAccessorService.getRepository(ProfesseursRepository.class)
                .save(entity);

        return dozerMapperBean.map(savedEntity, Professeurs.class);
    }
}
