package cmr.notep.business.business;

import cmr.notep.business.exceptions.SchoolException;
import cmr.notep.business.exceptions.enums.SchoolErrorCode;
import cmr.notep.interfaces.modeles.Chapitre;
import cmr.notep.interfaces.modeles.Cours;
import cmr.notep.modele.EtatCours;
import cmr.notep.ressourcesjpa.commun.DaoAccessorService;
import cmr.notep.ressourcesjpa.dao.ChapitreEntity;
import cmr.notep.ressourcesjpa.dao.CoursEntity;
import cmr.notep.ressourcesjpa.dao.ProfesseursEntity;
import cmr.notep.ressourcesjpa.repository.ChapitreRepository;
import cmr.notep.ressourcesjpa.repository.CoursRepository;
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
        // Validate professor
        ProfesseursEntity professeur = daoAccessorService.getRepository(ProfesseursRepository.class)
                .findById(cours.getRedacteurId())
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Professeur introuvable"));

        // Map and save course
        CoursEntity entity = dozerMapperBean.map(cours, CoursEntity.class);
        entity.setRedacteur(professeur);

        // Set default state if not provided
        if (entity.getEtat() == null) {
            entity.setEtat(EtatCours.BROUILLON);
        }

        // Set default restriction if not provided
        if (entity.getRestriction() == null) {
            entity.setRestriction("PRIVE");
        }

        // Save course first to get ID
        CoursEntity savedEntity = daoAccessorService.getRepository(CoursRepository.class).save(entity);

        // Save chapters if provided
        if (cours.getChapitres() != null && !cours.getChapitres().isEmpty()) {
            saveChapitres(cours.getChapitres(), savedEntity);
        }

        // Map back to DTO with all fields
        Cours result = dozerMapperBean.map(savedEntity, Cours.class);
        result.setRedacteurId(savedEntity.getRedacteur().getId());
        result.setChapitres(mapChapitresToDto(savedEntity.getChapitres()));

        return result;
    }

    private void saveChapitres(List<Chapitre> chapitres, CoursEntity coursEntity) {
        ChapitreRepository chapitreRepository = daoAccessorService.getRepository(ChapitreRepository.class);

        // Delete existing chapters
        chapitreRepository.deleteByCoursId(coursEntity.getId());

        // Save new chapters
        for (int i = 0; i < chapitres.size(); i++) {
            Chapitre chapitre = chapitres.get(i);
            ChapitreEntity chapitreEntity = dozerMapperBean.map(chapitre, ChapitreEntity.class);
            chapitreEntity.setCours(coursEntity);
            chapitreEntity.setOrdre(i + 1); // Set order based on position in list
            chapitreRepository.save(chapitreEntity);
        }
    }

    private List<Chapitre> mapChapitresToDto(List<ChapitreEntity> chapitreEntities) {
        return chapitreEntities.stream()
                .map(c -> dozerMapperBean.map(c, Chapitre.class))
                .collect(Collectors.toList());
    }

    public List<Cours> obtenirCoursParProfesseur(String professeurId) {
        return daoAccessorService.getRepository(CoursRepository.class)
                .findByRedacteurId(professeurId)
                .stream()
                .map(c -> {
                    Cours cours = dozerMapperBean.map(c, Cours.class);
                    cours.setRedacteurId(c.getRedacteur().getId());
                    cours.setChapitres(mapChapitresToDto(c.getChapitres()));
                    return cours;
                })
                .collect(Collectors.toList());
    }

    public List<Cours> obtenirCoursParEtat(EtatCours etat) {
        return daoAccessorService.getRepository(CoursRepository.class)
                .findByEtat(etat)
                .stream()
                .map(c -> {
                    Cours cours = dozerMapperBean.map(c, Cours.class);
                    cours.setRedacteurId(c.getRedacteur().getId());
                    cours.setChapitres(mapChapitresToDto(c.getChapitres()));
                    return cours;
                })
                .collect(Collectors.toList());
    }

    public List<Cours> obtenirCoursAccessibles(String userId) {
        return daoAccessorService.getRepository(CoursRepository.class)
                .findAccessibleCours(userId)
                .stream()
                .map(c -> {
                    Cours cours = dozerMapperBean.map(c, Cours.class);
                    cours.setRedacteurId(c.getRedacteur().getId());
                    cours.setChapitres(mapChapitresToDto(c.getChapitres()));
                    return cours;
                })
                .collect(Collectors.toList());
    }

    public Cours obtenirCoursAvecChapitres(String coursId) {
        CoursEntity coursEntity = daoAccessorService.getRepository(CoursRepository.class)
                .findById(coursId)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Cours introuvable"));

        // Load chapters
        List<ChapitreEntity> chapitres = daoAccessorService.getRepository(ChapitreRepository.class)
                .findByCoursIdOrderByOrdre(coursId);
        coursEntity.setChapitres(chapitres);

        Cours cours = dozerMapperBean.map(coursEntity, Cours.class);
        cours.setRedacteurId(coursEntity.getRedacteur().getId());
        cours.setChapitres(mapChapitresToDto(chapitres));

        return cours;
    }
}