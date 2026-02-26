package cmr.notep.business.business;

import cmr.notep.business.exceptions.SchoolException;
import cmr.notep.business.exceptions.enums.SchoolErrorCode;
import cmr.notep.business.services.NotificationService;
import cmr.notep.interfaces.modeles.Classes;
import cmr.notep.interfaces.modeles.ExerciseProgrammer;
import cmr.notep.modele.EtatExercise;
import cmr.notep.ressourcesjpa.commun.DaoAccessorService;
import cmr.notep.ressourcesjpa.dao.*;
import cmr.notep.ressourcesjpa.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static cmr.notep.business.config.BusinessConfig.dozerMapperBean;

@Component
@Slf4j
@RequiredArgsConstructor
public class ExerciseProgrammerBusiness {

    private final DaoAccessorService daoAccessorService;
    private final NotificationService notificationService;

    public ExerciseProgrammer programmerExercise(ExerciseProgrammer exerciseProgrammer) {
        log.info("Programmation d'un nouvel exercice à partir de l'exercice ID: {}", exerciseProgrammer.getExerciseId());

        // Récupérer l'exercice existant
        ExerciseEntity exerciseExistante = daoAccessorService.getRepository(ExerciseRepository.class)
                .findById(exerciseProgrammer.getExerciseId())
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Exercice source introuvable"));

        // Validate and fetch required entities
        ProfesseursEntity professeur = daoAccessorService.getRepository(ProfesseursRepository.class)
                .findById(exerciseProgrammer.getProgrammeParId())
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Professeur programmeur introuvable"));

        // Créer l'entité ExerciseProgrammer
        ExerciseProgrammerEntity entity = new ExerciseProgrammerEntity();
        entity.setId(exerciseProgrammer.getExerciseId()); // Use exercise_id as PK
        entity.setExercise(exerciseExistante);
        entity.setProgrammePar(professeur);
        entity.setDateExoPrevue(exerciseProgrammer.getDateExoPrevue());
        entity.setDateDebutExoEffectif(exerciseProgrammer.getDateDebutExoEffectif());
        entity.setDateFinExoEffectif(exerciseProgrammer.getDateFinExoEffectif());
        entity.setEtat(EtatExercise.PUBLIE); // Default state

        // Save the entity
        ExerciseProgrammerEntity savedEntity = daoAccessorService.getRepository(ExerciseProgrammerRepository.class).save(entity);
        log.info("Exercice programmé avec ID: {} à partir de l'exercice source: {}", savedEntity.getId(), exerciseProgrammer.getExerciseId());

        return dozerMapperBean.map(savedEntity, ExerciseProgrammer.class);
    }

    // Les autres méthodes restent inchangées...
    public ExerciseProgrammer programmerEtDiffuserExercise(ExerciseProgrammer exerciseProgrammer) {
        // Programmer l'exercice d'abord
        ExerciseProgrammer exerciseProgramme = programmerExercise(exerciseProgrammer);

        // Diffuser dans les classes spécifiées (si des IDs de classes sont fournis)
        if (exerciseProgrammer.getClassesDiffusees() != null && !exerciseProgrammer.getClassesDiffusees().isEmpty()) {
            List<String> classeIds = new ArrayList<>();
            for (Classes classe : exerciseProgrammer.getClassesDiffusees()) {
                diffuserExerciseDansClasse(exerciseProgramme.getId(), classe.getId());
                classeIds.add(classe.getId());
            }

            // Send notifications to students in the classes
            try {
                ProfesseursEntity prof = daoAccessorService.getRepository(ProfesseursRepository.class)
                        .findById(exerciseProgrammer.getProgrammeParId()).orElse(null);
                if (prof != null) {
                    String profName = prof.getPrenom() + " " + prof.getNom();
                    notificationService.createExerciseAssignedNotification(
                            exerciseProgramme.getNom(), prof.getId(), profName, classeIds);
                }
            } catch (Exception e) {
                log.error("Error sending exercise notifications: {}", e.getMessage());
            }
        }

        return exerciseProgramme;
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

    public List<ExerciseProgrammer> obtenirExercisesProgrammesParExercise(String exerciseId) {
        return daoAccessorService.getRepository(ExerciseProgrammerRepository.class)
                .findByExerciseId(exerciseId)
                .stream()
                .map(e -> dozerMapperBean.map(e, ExerciseProgrammer.class))
                .collect(Collectors.toList());
    }

    public ExerciseProgrammer mettreAJourEtatExerciseProgramme(String exerciseProgrammerId, EtatExercise nouvelEtat) {
        ExerciseProgrammerEntity exerciseProgrammer = daoAccessorService.getRepository(ExerciseProgrammerRepository.class)
                .findById(exerciseProgrammerId)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Exercice programmé introuvable"));

        exerciseProgrammer.getExercise().setEtat(nouvelEtat);
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