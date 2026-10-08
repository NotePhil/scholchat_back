package cmr.notep.business.business;

import cmr.notep.business.security.UserSubtypeService;

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

    private final UserSubtypeService userSubtypeService;
    private final DaoAccessorService daoAccessorService;
    private final NotificationService notificationService;

    /** Programme sans diffuser (POST /exercises-programmer) ; renvoie la première programmation créée. */
    public ExerciseProgrammer programmerExercise(ExerciseProgrammer exerciseProgrammer) {
        return programmer(exerciseProgrammer, false).get(0);
    }

    /** Programme et diffuse (POST /programmer-et-diffuser) ; renvoie la première programmation créée. */
    public ExerciseProgrammer programmerEtDiffuserExercise(ExerciseProgrammer exerciseProgrammer) {
        return programmer(exerciseProgrammer, true).get(0);
    }

    /**
     * Programme un exercice pour une ou plusieurs classes. Chaque classe reçoit un cours (coursParClasse[classe],
     * sinon coursId, sinon « Exercice général ») qui doit être programmé dans cette classe (400
     * COURS_NON_PROGRAMME_DANS_CLASSE sinon ; tout est validé avant toute écriture). Les classes sont regroupées par
     * cours : une programmation (exercises_programmer) par cours distinct, mêmes dates/type/état, chacune diffusée
     * (si {@code diffuser}) dans ses classes et notifiée à leurs élèves (un élève n'est notifié qu'une fois).
     * Un seul groupe (cas habituel) = une seule programmation, comme avant.
     *
     * @return les programmations créées, dans l'ordre des classes demandées (jamais vide)
     */
    public List<ExerciseProgrammer> programmer(ExerciseProgrammer exerciseProgrammer, boolean diffuser) {
        log.info("Programmation d'un nouvel exercice à partir de l'exercice ID: {}", exerciseProgrammer.getExerciseId());

        ExerciseEntity exerciseSource = daoAccessorService.getRepository(ExerciseRepository.class)
                .findById(exerciseProgrammer.getExerciseId())
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Exercice source introuvable"));

        UtilisateursEntity professeur = userSubtypeService.findProfesseur(exerciseProgrammer.getProgrammeParId())
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Professeur programmeur introuvable"));

        // 1. Validation : un cours (ou null = général) par classe, regroupement par cours
        List<String> classeIds = classeIdsDemandees(exerciseProgrammer);
        java.util.Map<String, CoursEntity> coursParGroupe = new java.util.LinkedHashMap<>();
        java.util.Map<String, List<String>> classesParGroupe = new java.util.LinkedHashMap<>();
        if (classeIds.isEmpty()) {
            CoursEntity cours = resoudreCours(exerciseProgrammer.getCoursId(), List.of());
            String cle = cours == null ? "" : cours.getId();
            coursParGroupe.put(cle, cours);
            classesParGroupe.put(cle, new ArrayList<>());
        } else {
            java.util.Map<String, String> parClasse = exerciseProgrammer.getCoursParClasse() != null
                    ? exerciseProgrammer.getCoursParClasse() : java.util.Map.of();
            for (String classeId : classeIds) {
                String coursId = parClasse.containsKey(classeId) ? parClasse.get(classeId) : exerciseProgrammer.getCoursId();
                CoursEntity cours = resoudreCoursPourClasse(coursId, classeId);
                String cle = cours == null ? "" : cours.getId();
                coursParGroupe.putIfAbsent(cle, cours);
                classesParGroupe.computeIfAbsent(cle, k -> new ArrayList<>()).add(classeId);
            }
        }

        // 2. Création d'une programmation par cours distinct
        exerciseSource.setEtat(diffuser && !classeIds.isEmpty() ? EtatExercise.PUBLIE : EtatExercise.ACTIF);
        daoAccessorService.getRepository(ExerciseRepository.class).save(exerciseSource);
        if (diffuser) {
            lierCoursLegacy(exerciseSource, exerciseProgrammer.getCoursIds());
        }

        String exerciseName = exerciseSource.getNom() != null ? exerciseSource.getNom() : "Exercice";
        String profName = professeur.getPrenom() + " " + professeur.getNom();
        java.util.Set<String> elevesNotifies = new java.util.HashSet<>();
        List<ExerciseProgrammer> crees = new ArrayList<>();
        for (java.util.Map.Entry<String, List<String>> groupe : classesParGroupe.entrySet()) {
            ExerciseProgrammerEntity entity = new ExerciseProgrammerEntity();
            entity.setId(UUID.randomUUID().toString());
            entity.setExercise(exerciseSource);
            entity.setProgrammePar(professeur);
            entity.setTypeAssignation(exerciseProgrammer.getTypeAssignation() != null
                    ? exerciseProgrammer.getTypeAssignation() : TypeAssignation.EXERCICE);
            entity.setDateExoPrevue(exerciseProgrammer.getDateExoPrevue());
            entity.setDateDebutExoEffectif(exerciseProgrammer.getDateDebutExoEffectif());
            entity.setDateFinExoEffectif(exerciseProgrammer.getDateFinExoEffectif());
            entity.setEtat(EtatExercise.ACTIF);
            entity.setCours(coursParGroupe.get(groupe.getKey()));
            if (diffuser) {
                for (String classeId : groupe.getValue()) {
                    ClassesEntity classe = daoAccessorService.getRepository(ClassesRepository.class).findById(classeId)
                            .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Classe introuvable"));
                    if (!entity.getClassesDiffusees().contains(classe)) {
                        entity.getClassesDiffusees().add(classe);
                    }
                }
            }
            ExerciseProgrammerEntity saved = daoAccessorService.getRepository(ExerciseProgrammerRepository.class).save(entity);
            log.info("Exercice programmé {} (source {}, type {}, cours {}) {} classes {}", saved.getId(),
                    exerciseSource.getId(), saved.getTypeAssignation(),
                    saved.getCours() != null ? saved.getCours().getId() : "(général)",
                    diffuser ? "diffusé dans les" : "pour les", groupe.getValue());

            if (diffuser && !groupe.getValue().isEmpty()) {
                try {
                    notificationService.createExerciseAssignedNotification(saved.getId(), exerciseName,
                            professeur.getId(), profName, groupe.getValue(), elevesNotifies);
                } catch (Exception e) {
                    log.error("Error sending exercise notifications: {}", e.getMessage());
                }
            }
            crees.add(dozerMapperBean.map(saved, ExerciseProgrammer.class));
        }
        return crees;
    }

    /** Ancien champ coursIds : lie l'exercice source aux cours (exercises.coursLies), sans effet sur le rattachement. */
    private void lierCoursLegacy(ExerciseEntity exercise, List<String> coursIds) {
        if (coursIds == null || coursIds.isEmpty()) {
            return;
        }
        for (String coursId : coursIds) {
            try {
                CoursEntity cours = daoAccessorService.getRepository(CoursRepository.class).findById(coursId).orElse(null);
                if (cours != null && !exercise.getCoursLies().contains(cours)) {
                    exercise.getCoursLies().add(cours);
                    daoAccessorService.getRepository(ExerciseRepository.class).save(exercise);
                }
            } catch (Exception e) {
                log.warn("Could not link exercise to cours {}: {}", coursId, e.getMessage());
            }
        }
    }

    /** Valide le cours d'une classe (null/vide = général) ; message d'erreur nommant la classe. */
    private CoursEntity resoudreCoursPourClasse(String coursId, String classeId) {
        if (coursId == null || coursId.isBlank()) {
            return null;
        }
        CoursEntity cours = daoAccessorService.getRepository(CoursRepository.class).findById(coursId.trim())
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.COURS_NON_PROGRAMME_DANS_CLASSE,
                        "Cours introuvable : " + coursId));
        if (daoAccessorService.getRepository(CoursProgrammerRepository.class)
                .countProgrammationsDansClasse(cours.getId(), classeId) == 0) {
            String nomClasse = daoAccessorService.getRepository(ClassesRepository.class).findById(classeId)
                    .map(ClassesEntity::getNom).orElse(classeId);
            throw new SchoolException(SchoolErrorCode.COURS_NON_PROGRAMME_DANS_CLASSE,
                    "Le cours « " + cours.getTitre() + " » n'est pas programmé dans la classe « " + nomClasse + " ».");
        }
        return cours;
    }

    private static List<String> classeIdsDemandees(ExerciseProgrammer exerciseProgrammer) {
        java.util.LinkedHashSet<String> ids = new java.util.LinkedHashSet<>();
        if (exerciseProgrammer.getClasseIds() != null) {
            exerciseProgrammer.getClasseIds().stream().filter(id -> id != null && !id.isBlank()).forEach(ids::add);
        }
        if (ids.isEmpty() && exerciseProgrammer.getClassesDiffusees() != null) {
            exerciseProgrammer.getClassesDiffusees().stream()
                    .filter(c -> c != null && c.getId() != null).forEach(c -> ids.add(c.getId()));
        }
        if (exerciseProgrammer.getCoursParClasse() != null) {
            exerciseProgrammer.getCoursParClasse().keySet().stream().filter(id -> id != null && !id.isBlank())
                    .forEach(ids::add);
        }
        return new ArrayList<>(ids);
    }

    /**
     * Cours de rattachement d'un exercice programmé : null/vide = « Exercices généraux ». Sinon le cours doit
     * exister et être programmé (cours_programmer) dans chacune des classes indiquées ; à défaut, 400
     * COURS_NON_PROGRAMME_DANS_CLASSE.
     */
    public CoursEntity resoudreCours(String coursId, java.util.Collection<String> classeIds) {
        if (coursId == null || coursId.isBlank()) {
            return null;
        }
        CoursEntity cours = daoAccessorService.getRepository(CoursRepository.class).findById(coursId.trim())
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.COURS_NON_PROGRAMME_DANS_CLASSE,
                        "Cours introuvable : " + coursId));
        if (classeIds != null) {
            CoursProgrammerRepository cpRepo = daoAccessorService.getRepository(CoursProgrammerRepository.class);
            for (String classeId : classeIds) {
                if (cpRepo.countProgrammationsDansClasse(cours.getId(), classeId) == 0) {
                    throw new SchoolException(SchoolErrorCode.COURS_NON_PROGRAMME_DANS_CLASSE,
                            "Le cours « " + cours.getTitre() + " » n'est pas programmé dans cette classe.");
                }
            }
        }
        return cours;
    }

    /**
     * Change (ou retire, coursId null/vide) le cours de rattachement d'un exercice programmé existant. Le cours
     * doit être programmé dans toutes les classes où l'exercice est diffusé.
     */
    public ExerciseProgrammerEntity changerCours(String exerciseProgrammerId, String coursId) {
        ExerciseProgrammerEntity ep = obtenirExerciseProgrammeEntityParId(exerciseProgrammerId);
        List<String> classeIds = ep.getClassesDiffusees() == null ? List.of()
                : ep.getClassesDiffusees().stream().map(ClassesEntity::getId).collect(Collectors.toList());
        ep.setCours(resoudreCours(coursId, classeIds));
        ExerciseProgrammerEntity saved = daoAccessorService.getRepository(ExerciseProgrammerRepository.class).save(ep);
        log.info("Exercice programmé {} rattaché au cours {}", exerciseProgrammerId,
                saved.getCours() != null ? saved.getCours().getId() : "(exercices généraux)");
        return saved;
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