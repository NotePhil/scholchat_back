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
import cmr.notep.modele.TypeAssignation;
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

        ExerciseEntity exerciseSource = daoAccessorService.getRepository(ExerciseRepository.class)
                .findById(exerciseProgrammer.getExerciseId())
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Exercice source introuvable"));

        ProfesseursEntity professeur = daoAccessorService.getRepository(ProfesseursRepository.class)
                .findById(exerciseProgrammer.getProgrammeParId())
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Professeur programmeur introuvable"));

        ExerciseProgrammerEntity entity = new ExerciseProgrammerEntity();
        entity.setId(UUID.randomUUID().toString());
        entity.setExercise(exerciseSource);
        entity.setProgrammePar(professeur);
        entity.setTypeAssignation(exerciseProgrammer.getTypeAssignation() != null 
            ? exerciseProgrammer.getTypeAssignation() 
            : cmr.notep.modele.TypeAssignation.EXERCICE); // Default to EXERCICE
        entity.setDateExoPrevue(exerciseProgrammer.getDateExoPrevue());
        entity.setDateDebutExoEffectif(exerciseProgrammer.getDateDebutExoEffectif());
        entity.setDateFinExoEffectif(exerciseProgrammer.getDateFinExoEffectif());
        entity.setEtat(EtatExercise.ACTIF);

        exerciseSource.setEtat(EtatExercise.ACTIF);
        daoAccessorService.getRepository(ExerciseRepository.class).save(exerciseSource);

        ExerciseProgrammerEntity savedEntity = daoAccessorService.getRepository(ExerciseProgrammerRepository.class).save(entity);
        log.info("Exercice programmé avec ID: {} à partir de l'exercice source: {} (Type: {})", 
            savedEntity.getId(), exerciseProgrammer.getExerciseId(), entity.getTypeAssignation());

        return dozerMapperBean.map(savedEntity, ExerciseProgrammer.class);
    }

    // Les autres méthodes restent inchangées...
    public ExerciseProgrammer programmerEtDiffuserExercise(ExerciseProgrammer exerciseProgrammer) {
        ExerciseProgrammer exerciseProgramme = programmerExercise(exerciseProgrammer);

        // Link to specific courses if provided
        if (exerciseProgrammer.getCoursIds() != null && !exerciseProgrammer.getCoursIds().isEmpty()) {
            for (String coursId : exerciseProgrammer.getCoursIds()) {
                try {
                    daoAccessorService.getRepository(ExerciseRepository.class)
                        .findById(exerciseProgrammer.getExerciseId()).ifPresent(ex -> {
                            cmr.notep.ressourcesjpa.dao.CoursEntity cours = daoAccessorService
                                .getRepository(cmr.notep.ressourcesjpa.repository.CoursRepository.class)
                                .findById(coursId).orElse(null);
                            if (cours != null && !ex.getCoursLies().contains(cours)) {
                                ex.getCoursLies().add(cours);
                                daoAccessorService.getRepository(ExerciseRepository.class).save(ex);
                            }
                        });
                } catch (Exception e) {
                    log.warn("Could not link exercise to cours {}: {}", coursId, e.getMessage());
                }
            }
        }

        // Diffuse to classes
        List<String> classeIdsToDistribute = new ArrayList<>();
        if (exerciseProgrammer.getClasseIds() != null && !exerciseProgrammer.getClasseIds().isEmpty()) {
            classeIdsToDistribute.addAll(exerciseProgrammer.getClasseIds());
        } else if (exerciseProgrammer.getClassesDiffusees() != null && !exerciseProgrammer.getClassesDiffusees().isEmpty()) {
            for (Classes classe : exerciseProgrammer.getClassesDiffusees()) {
                classeIdsToDistribute.add(classe.getId());
            }
        }

        if (!classeIdsToDistribute.isEmpty()) {
            List<String> classeIds = new ArrayList<>();
            for (String classId : classeIdsToDistribute) {
                diffuserExerciseDansClasse(exerciseProgramme.getId(), classId);
                classeIds.add(classId);
            }

            String sourceExerciseId = exerciseProgrammer.getExerciseId() != null
                    ? exerciseProgrammer.getExerciseId()
                    : exerciseProgramme.getExerciseId();
            ExerciseEntity source = sourceExerciseId != null
                    ? daoAccessorService.getRepository(ExerciseRepository.class).findById(sourceExerciseId).orElse(null)
                    : null;
            if (source != null) {
                source.setEtat(EtatExercise.PUBLIE);
                daoAccessorService.getRepository(ExerciseRepository.class).save(source);
            }

            try {
                ProfesseursEntity prof = daoAccessorService.getRepository(ProfesseursRepository.class)
                        .findById(exerciseProgrammer.getProgrammeParId()).orElse(null);
                if (prof != null) {
                    String profName = prof.getPrenom() + " " + prof.getNom();
                    // Use the exercise NAME (not ID) for the notification message
                    String exerciseName = (source != null && source.getNom() != null)
                            ? source.getNom()
                            : "Exercice";
                    notificationService.createExerciseAssignedNotification(
                            exerciseName, prof.getId(), profName, classeIds);
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

    public List<ExerciseProgrammer> obtenirExercisesProgrammesParExerciseId(String exerciseId) {
        return daoAccessorService.getRepository(ExerciseProgrammerRepository.class)
                .findByExerciseId(exerciseId)
                .stream()
                .map(e -> dozerMapperBean.map(e, ExerciseProgrammer.class))
                .collect(Collectors.toList());
    }

    public void supprimerExerciseProgramme(String exerciseProgrammerId) {
        ExerciseProgrammerEntity exerciseProgrammer = daoAccessorService.getRepository(ExerciseProgrammerRepository.class)
                .findById(exerciseProgrammerId)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Exercice programmé introuvable"));

        String sourceExerciseId = exerciseProgrammer.getExercise().getId();
        daoAccessorService.getRepository(ExerciseProgrammerRepository.class).delete(exerciseProgrammer);

        boolean hasOtherProgrammations = !daoAccessorService.getRepository(ExerciseProgrammerRepository.class)
                .findByExerciseId(sourceExerciseId).isEmpty();
        if (!hasOtherProgrammations) {
            ExerciseEntity source = daoAccessorService.getRepository(ExerciseRepository.class)
                    .findById(sourceExerciseId).orElse(null);
            if (source != null) {
                source.setEtat(EtatExercise.INACTIF);
                daoAccessorService.getRepository(ExerciseRepository.class).save(source);
            }
        }
        log.info("Exercice programmé {} supprimé", exerciseProgrammerId);
    }

    public ExerciseProgrammerEntity obtenirExerciseProgrammeEntityParId(String exerciseProgrammerId) {
        return daoAccessorService.getRepository(ExerciseProgrammerRepository.class)
                .findById(exerciseProgrammerId)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Exercice programmé introuvable"));
    }
}