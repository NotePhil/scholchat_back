package cmr.notep.business.business;

import cmr.notep.business.exceptions.SchoolException;
import cmr.notep.business.exceptions.enums.SchoolErrorCode;
import cmr.notep.interfaces.modeles.Cours;
import cmr.notep.modele.EtatCours;
import cmr.notep.ressourcesjpa.commun.DaoAccessorService;
import cmr.notep.ressourcesjpa.dao.CoursEntity;
import cmr.notep.ressourcesjpa.dao.MatiereEntity;
import cmr.notep.ressourcesjpa.dao.ProfesseursEntity;
import cmr.notep.ressourcesjpa.repository.CoursRepository;
import cmr.notep.ressourcesjpa.repository.MatiereRepository;
import cmr.notep.ressourcesjpa.repository.ProfesseursRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import static cmr.notep.business.config.BusinessConfig.dozerMapperBean;

@Component
@Slf4j
public class CoursBusiness {

    private final DaoAccessorService daoAccessorService;

    public CoursBusiness(DaoAccessorService daoAccessorService) {
        this.daoAccessorService = daoAccessorService;
    }

    public Cours creerCours(Cours cours) {
        ProfesseursEntity professeur = daoAccessorService.getRepository(ProfesseursRepository.class)
                .findById(cours.getRedacteurId())
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Professeur introuvable"));

        List<MatiereEntity> matieres = cours.getMatiereIds().stream()
                .map(id -> daoAccessorService.getRepository(MatiereRepository.class)
                        .findById(id)
                        .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Matière introuvable: " + id)))
                .collect(Collectors.toList());

        // 3. Map and save
        CoursEntity entity = dozerMapperBean.map(cours, CoursEntity.class);
        entity.setRedacteur(professeur);
        entity.setMatieres(matieres);

        CoursEntity savedEntity = daoAccessorService.getRepository(CoursRepository.class).save(entity);

        // 4. Map back to DTO with all fields
        Cours result = dozerMapperBean.map(savedEntity, Cours.class);

        // Manually set the IDs that weren't mapped automatically
        result.setRedacteurId(savedEntity.getRedacteur().getId());
        result.setMatiereIds(savedEntity.getMatieres().stream()
                .map(MatiereEntity::getId)
                .collect(Collectors.toList()));

        return result;
    }
    public List<Cours> obtenirCoursParProfesseur(String professeurId) {
        return daoAccessorService.getRepository(CoursRepository.class)
                .findByRedacteurId(professeurId)
                .stream()
                .map(c -> {
                    Cours cours = dozerMapperBean.map(c, Cours.class);
                    cours.setRedacteurId(c.getRedacteur().getId());
                    cours.setMatiereIds(c.getMatieres().stream()
                            .map(MatiereEntity::getId)
                            .collect(Collectors.toList()));
                    return cours;
                })
                .collect(Collectors.toList());
    }

    public List<Cours> obtenirCoursParMatiere(String matiereId) {
        return daoAccessorService.getRepository(CoursRepository.class)
                .findByMatieresId(matiereId)
                .stream()
                .map(c -> {
                    Cours cours = dozerMapperBean.map(c, Cours.class);
                    cours.setRedacteurId(c.getRedacteur().getId());
                    cours.setMatiereIds(c.getMatieres().stream()
                            .map(MatiereEntity::getId)
                            .collect(Collectors.toList()));
                    return cours;
                })
                .collect(Collectors.toList());
    }
    public List<Cours> obtenirCoursParEtat(EtatCours etat) {
        return daoAccessorService.getRepository(CoursRepository.class)
                .findByEtat(etat)
                .stream()
                .map(c -> dozerMapperBean.map(c, Cours.class))
                .collect(Collectors.toList());
    }
}