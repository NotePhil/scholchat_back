package cmr.notep.business.business;

import cmr.notep.business.exceptions.SchoolException;
import cmr.notep.business.exceptions.enums.SchoolErrorCode;
import cmr.notep.interfaces.modeles.Exercise;
import cmr.notep.modele.EtatCours;
import cmr.notep.modele.EtatExercise;
import cmr.notep.modele.EtatX;
import cmr.notep.modele.ListeNiveau;
import cmr.notep.ressourcesjpa.commun.DaoAccessorService;
import cmr.notep.ressourcesjpa.dao.CoursEntity;
import cmr.notep.ressourcesjpa.dao.ExerciseEntity;
import cmr.notep.ressourcesjpa.dao.MatiereEntity;
import cmr.notep.ressourcesjpa.dao.ProfesseursEntity;
import cmr.notep.ressourcesjpa.repository.CoursRepository;
import cmr.notep.ressourcesjpa.repository.ExerciseRepository;
import cmr.notep.ressourcesjpa.repository.MatiereRepository;
import cmr.notep.ressourcesjpa.repository.ProfesseursRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static cmr.notep.business.config.BusinessConfig.dozerMapperBean;

@Component
@Slf4j
@RequiredArgsConstructor
@Transactional
public class ExerciseBusiness {

    private final DaoAccessorService daoAccessorService;

    public Exercise creerExercise(Exercise exercise) {
        log.info("Création d'un nouvel exercice: {}", exercise.getNom());
        ProfesseursEntity professeur = daoAccessorService.getRepository(ProfesseursRepository.class)
                .findById(exercise.getRedacteurId())
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Professeur introuvable"));
        ExerciseEntity entity = dozerMapperBean.map(exercise, ExerciseEntity.class);
        entity.setRedacteur(professeur);
        entity.setDateCreation(new Date());
        if (entity.getCoursLies() == null) {
            entity.setCoursLies(new ArrayList<>());
        }
        if (entity.getEtat() == null) {
            entity.setEtat(EtatExercise.ACTIF);  // Par défaut, ACTIF
        }
        if (entity.getRestriction() == null) {
            entity.setRestriction("PRIVE");
        }
        ExerciseEntity savedEntity = daoAccessorService.getRepository(ExerciseRepository.class).save(entity);
        log.info("Exercise créé avec ID: {}", savedEntity.getId());
        return dozerMapperBean.map(savedEntity, Exercise.class);
    }



    public ExerciseEntity obtenirExerciseEntityParId(String exerciseId) {
        return daoAccessorService.getRepository(ExerciseRepository.class)
                .findById(exerciseId)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Exercice introuvable"));
    }

    public List<ExerciseEntity> obtenirExercisesEntityParProfesseur(String professeurId) {
        return daoAccessorService.getRepository(ExerciseRepository.class)
                .findByRedacteurId(professeurId);
    }

    public List<ExerciseEntity> obtenirExercisesEntityParNiveau(ListeNiveau niveau) {
        return daoAccessorService.getRepository(ExerciseRepository.class)
                .findByNiveau(niveau);
    }

    public List<ExerciseEntity> obtenirExercisesEntityAccessibles(String userId) {
        return daoAccessorService.getRepository(ExerciseRepository.class)
                .findAccessibleExercises(userId);
    }

    public List<ExerciseEntity> obtenirExercisesEntityParCours(String coursId) {
        return daoAccessorService.getRepository(ExerciseRepository.class)
                .findByCoursId(coursId);
    }
    public ExerciseEntity lierExerciseAMatiere(String exerciseId, String matiereId) {
        ExerciseEntity exerciseEntity = daoAccessorService.getRepository(ExerciseRepository.class)
                .findById(exerciseId)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Exercice introuvable"));
        MatiereEntity matiereEntity = daoAccessorService.getRepository(MatiereRepository.class)
                .findById(matiereId)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Matière introuvable"));
        if (!exerciseEntity.getMatieres().contains(matiereEntity)) {
            exerciseEntity.getMatieres().add(matiereEntity);
            exerciseEntity = daoAccessorService.getRepository(ExerciseRepository.class).save(exerciseEntity);
        }
        return exerciseEntity;
    }
    public ExerciseEntity delierExerciseDeMatiere(String exerciseId, String matiereId) {
        ExerciseEntity exerciseEntity = daoAccessorService.getRepository(ExerciseRepository.class)
                .findById(exerciseId)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Exercice introuvable"));

        MatiereEntity matiereEntity = daoAccessorService.getRepository(MatiereRepository.class)
                .findById(matiereId)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Matière introuvable"));

        if (exerciseEntity.getMatieres().contains(matiereEntity)) {
            exerciseEntity.getMatieres().remove(matiereEntity);
            exerciseEntity = daoAccessorService.getRepository(ExerciseRepository.class).save(exerciseEntity);
        }

        return exerciseEntity;
    }

    public Exercise mettreAJourExercise(String exerciseId, Exercise exercise) {
        log.info("Mise à jour de l'exercice: {}", exerciseId);
        ExerciseEntity existingEntity = daoAccessorService.getRepository(ExerciseRepository.class)
                .findById(exerciseId)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Exercice introuvable"));

        if (exercise.getNom() != null) {
            existingEntity.setNom(exercise.getNom());
        }
        if (exercise.getDescription() != null) {
            existingEntity.setDescription(exercise.getDescription());
        }
        if (exercise.getEtat() != null) {
            existingEntity.setEtat(exercise.getEtat());
        }
        if (exercise.getRestriction() != null) {
            existingEntity.setRestriction(exercise.getRestriction());
        }
        if (exercise.getNiveau() != null) {
            existingEntity.setNiveau(exercise.getNiveau());
        }

        ExerciseEntity updatedEntity = daoAccessorService.getRepository(ExerciseRepository.class).save(existingEntity);
        return dozerMapperBean.map(updatedEntity, Exercise.class);
    }

    public void supprimerExercise(String exerciseId) {
        log.info("Suppression de l'exercice: {}", exerciseId);
        ExerciseEntity exerciseEntity = daoAccessorService.getRepository(ExerciseRepository.class)
                .findById(exerciseId)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Exercice introuvable"));
        daoAccessorService.getRepository(ExerciseRepository.class).delete(exerciseEntity);
    }

    public Exercise obtenirExerciseParId(String exerciseId) {
        return dozerMapperBean.map(
                daoAccessorService.getRepository(ExerciseRepository.class)
                        .findById(exerciseId)
                        .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Exercice introuvable")),
                Exercise.class
        );
    }

    public List<Exercise> obtenirExercisesParProfesseur(String professeurId) {
        return daoAccessorService.getRepository(ExerciseRepository.class)
                .findByRedacteurId(professeurId)
                .stream()
                .map(e -> dozerMapperBean.map(e, Exercise.class))
                .collect(Collectors.toList());
    }

    public List<Exercise> obtenirExercisesParNiveau(ListeNiveau niveau) {
        return daoAccessorService.getRepository(ExerciseRepository.class)
                .findByNiveau(niveau)
                .stream()
                .map(e -> dozerMapperBean.map(e, Exercise.class))
                .collect(Collectors.toList());
    }

    public List<Exercise> obtenirExercisesAccessibles(String userId) {
        return daoAccessorService.getRepository(ExerciseRepository.class)
                .findAccessibleExercises(userId)
                .stream()
                .map(e -> dozerMapperBean.map(e, Exercise.class))
                .collect(Collectors.toList());
    }

    public List<Exercise> obtenirExercisesParCours(String coursId) {
        return daoAccessorService.getRepository(ExerciseRepository.class)
                .findByCoursId(coursId)
                .stream()
                .map(e -> dozerMapperBean.map(e, Exercise.class))
                .collect(Collectors.toList());
    }

    public ExerciseEntity lierExerciseACours(String exerciseId, String coursId) {
        ExerciseEntity exerciseEntity = daoAccessorService.getRepository(ExerciseRepository.class)
                .findById(exerciseId)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Exercice introuvable"));
        CoursEntity coursEntity = daoAccessorService.getRepository(CoursRepository.class)
                .findById(coursId)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Cours introuvable"));

        if (!exerciseEntity.getCoursLies().contains(coursEntity)) {
            exerciseEntity.getCoursLies().add(coursEntity);
            exerciseEntity = daoAccessorService.getRepository(ExerciseRepository.class).save(exerciseEntity);
        }

        return exerciseEntity;
    }

    public ExerciseEntity delierExerciseDeCours(String exerciseId, String coursId) {
        ExerciseEntity exerciseEntity = daoAccessorService.getRepository(ExerciseRepository.class)
                .findById(exerciseId)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Exercice introuvable"));
        CoursEntity coursEntity = daoAccessorService.getRepository(CoursRepository.class)
                .findById(coursId)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Cours introuvable"));

        exerciseEntity.getCoursLies().remove(coursEntity);
        exerciseEntity = daoAccessorService.getRepository(ExerciseRepository.class).save(exerciseEntity);

        return exerciseEntity;
    }
}
