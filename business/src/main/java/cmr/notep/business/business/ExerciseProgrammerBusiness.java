package cmr.notep.business.business;

import cmr.notep.business.exceptions.SchoolException;
import cmr.notep.business.exceptions.enums.SchoolErrorCode;
import cmr.notep.interfaces.modeles.ExerciseProgrammer;
import cmr.notep.modele.EtatExercise;
import cmr.notep.ressourcesjpa.commun.DaoAccessorService;
import cmr.notep.ressourcesjpa.dao.*;
import cmr.notep.ressourcesjpa.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static cmr.notep.business.config.BusinessConfig.dozerMapperBean;

@Component
@Slf4j
@RequiredArgsConstructor
public class ExerciseProgrammerBusiness {

    private final DaoAccessorService daoAccessorService;

    public ExerciseProgrammer programmerExercise(ExerciseProgrammer exerciseProgrammer) {
        log.info("Programmation d'un nouvel exercice: {}", exerciseProgrammer.getNom());
        ProfesseursEntity professeur = daoAccessorService.getRepository(ProfesseursRepository.class)
                .findById(exerciseProgrammer.getProgrammeParId())
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Professeur introuvable"));
        ProfesseursEntity redacteur = daoAccessorService.getRepository(ProfesseursRepository.class)
                .findById(exerciseProgrammer.getRedacteurId())
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Rédacteur introuvable"));
        ExerciseProgrammerEntity entity = dozerMapperBean.map(exerciseProgrammer, ExerciseProgrammerEntity.class);
        entity.setProgrammePar(professeur);
        entity.setRedacteur(redacteur);
        entity.setDateCreation(new Date());
        if (entity.getEtat() == null) {
            entity.setEtat(EtatExercise.BROUILLON);  // Par défaut, BROUILLON
        }
        if (entity.getRestriction() == null) {
            entity.setRestriction("PRIVE");
        }
        ExerciseProgrammerEntity savedEntity = daoAccessorService.getRepository(ExerciseProgrammerRepository.class).save(entity);
        log.info("Exercice programmé avec ID: {}", savedEntity.getId());
        return dozerMapperBean.map(savedEntity, ExerciseProgrammer.class);
    }

    public ExerciseProgrammerEntity diffuserExerciseDansClasse(String exerciseProgrammerId, String classeId) {
        ExerciseProgrammerEntity exerciseProgrammer = daoAccessorService.getRepository(ExerciseProgrammerRepository.class)
                .findById(exerciseProgrammerId)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Exercice programmé introuvable"));

        ClassesEntity classe = daoAccessorService.getRepository(ClassesRepository.class)
                .findById(classeId)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Classe introuvable"));

        if (!exerciseProgrammer.getClassesDiffusees().contains(classe)) {
            exerciseProgrammer.getClassesDiffusees().add(classe);
            exerciseProgrammer = daoAccessorService.getRepository(ExerciseProgrammerRepository.class).save(exerciseProgrammer);
            log.info("Exercice {} diffusé dans la classe {}", exerciseProgrammerId, classeId);
        }

        return exerciseProgrammer;
    }

    public ExerciseProgrammerEntity retirerExerciseDeClasse(String exerciseProgrammerId, String classeId) {
        ExerciseProgrammerEntity exerciseProgrammer = daoAccessorService.getRepository(ExerciseProgrammerRepository.class)
                .findById(exerciseProgrammerId)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Exercice programmé introuvable"));

        ClassesEntity classe = daoAccessorService.getRepository(ClassesRepository.class)
                .findById(classeId)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Classe introuvable"));

        if (exerciseProgrammer.getClassesDiffusees().contains(classe)) {
            exerciseProgrammer.getClassesDiffusees().remove(classe);
            exerciseProgrammer = daoAccessorService.getRepository(ExerciseProgrammerRepository.class).save(exerciseProgrammer);
            log.info("Exercice {} retiré de la classe {}", exerciseProgrammerId, classeId);
        }

        return exerciseProgrammer;
    }

    public List<ExerciseProgrammer> obtenirExercisesProgrammesParProfesseur(String professeurId) {
        return daoAccessorService.getRepository(ExerciseProgrammerRepository.class)
                .findByProgrammeParId(professeurId)
                .stream()
                .map(e -> dozerMapperBean.map(e, ExerciseProgrammer.class))
                .collect(Collectors.toList());
    }

    public List<ExerciseProgrammer> obtenirExercisesProgrammesParClasse(String classeId) {
        return daoAccessorService.getRepository(ExerciseProgrammerRepository.class)
                .findByClasseId(classeId)
                .stream()
                .map(e -> dozerMapperBean.map(e, ExerciseProgrammer.class))
                .collect(Collectors.toList());
    }

    public ExerciseProgrammer mettreAJourEtatExerciseProgramme(String exerciseProgrammerId, EtatExercise nouvelEtat) {
        ExerciseProgrammerEntity exerciseProgrammer = daoAccessorService.getRepository(ExerciseProgrammerRepository.class)
                .findById(exerciseProgrammerId)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Exercice programmé introuvable"));

        exerciseProgrammer.setEtat(nouvelEtat);
        ExerciseProgrammerEntity updatedEntity = daoAccessorService.getRepository(ExerciseProgrammerRepository.class).save(exerciseProgrammer);

        log.info("État de l'exercice programmé {} mis à jour vers {}", exerciseProgrammerId, nouvelEtat);
        return dozerMapperBean.map(updatedEntity, ExerciseProgrammer.class);
    }

    public ExerciseProgrammer obtenirExerciseProgrammeParId(String exerciseProgrammerId) {
        ExerciseProgrammerEntity entity = daoAccessorService.getRepository(ExerciseProgrammerRepository.class)
                .findById(exerciseProgrammerId)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Exercice programmé introuvable"));

        return dozerMapperBean.map(entity, ExerciseProgrammer.class);
    }

    public void supprimerExerciseProgramme(String exerciseProgrammerId) {
        ExerciseProgrammerEntity exerciseProgrammer = daoAccessorService.getRepository(ExerciseProgrammerRepository.class)
                .findById(exerciseProgrammerId)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Exercice programmé introuvable"));

        daoAccessorService.getRepository(ExerciseProgrammerRepository.class).delete(exerciseProgrammer);
        log.info("Exercice programmé {} supprimé", exerciseProgrammerId);
    }

    public ExerciseProgrammerEntity obtenirExerciseProgrammeEntityParId(String exerciseProgrammerId) {
        return daoAccessorService.getRepository(ExerciseProgrammerRepository.class)
                .findById(exerciseProgrammerId)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Exercice programmé introuvable"));
    }
}