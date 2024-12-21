package cmr.notep.business.business;

import cmr.notep.interfaces.modeles.Professeurs;
import cmr.notep.ressourcesjpa.commun.DaoAccessorService;
import cmr.notep.ressourcesjpa.dao.ProfesseursEntity;
import cmr.notep.ressourcesjpa.repository.ProfesseursRepository;
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
        log.info("avoirProfesseur called");
        return dozerMapperBean.map(
                daoAccessorService.getRepository(ProfesseursRepository.class)
                        .findById(idProfesseur)
                        .orElseThrow(() -> new RuntimeException("Professeur introuvable")),
                Professeurs.class
        );
    }

    public Professeurs posterProfesseur(Professeurs professeur) {
        return dozerMapperBean.map(
                this.daoAccessorService.getRepository(ProfesseursRepository.class)
                        .save(dozerMapperBean.map(professeur, ProfesseursEntity.class)),
                Professeurs.class
        );
    }

    public List<Professeurs> avoirToutProfesseurs() {
        return daoAccessorService.getRepository(ProfesseursRepository.class).findAll()
                .stream()
                .map(prof -> dozerMapperBean.map(prof, Professeurs.class))
                .collect(Collectors.toList());
    }

    public Professeurs avoirProfesseurParMatricule(String matriculeProfesseur) {
        return dozerMapperBean.map(
                daoAccessorService.getRepository(ProfesseursRepository.class)
                        .findByMatriculeProfesseur(matriculeProfesseur),
                Professeurs.class
        );
    }
}