package cmr.notep.business.business;

import cmr.notep.interfaces.modeles.CoursProgrammer;
import cmr.notep.modele.EtatCours;
import cmr.notep.modele.EtatCoursProgramme;
import cmr.notep.ressourcesjpa.commun.DaoAccessorService;
import cmr.notep.ressourcesjpa.dao.*;
import cmr.notep.ressourcesjpa.repository.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class CoursProgrammerBusiness {

    private final DaoAccessorService daoAccessorService;

    public CoursProgrammerBusiness(DaoAccessorService daoAccessorService) {
        this.daoAccessorService = daoAccessorService;
    }

    @Transactional
    public CoursProgrammer programmerCours(CoursProgrammer coursProgrammer) {
        // Validate input dates
        validateCourseScheduleDates(coursProgrammer);
        validateEffectiveDates(coursProgrammer);
        // Get the course and validate its status
        CoursEntity cours = daoAccessorService.getRepository(CoursRepository.class)
                .findById(coursProgrammer.getCoursId())
                .orElseThrow(() -> new RuntimeException("Course not found with ID: " + coursProgrammer.getCoursId()));

        validateCourseStatusForScheduling(cours);

        // Get the professor
        ProfesseursEntity professeur = daoAccessorService.getRepository(ProfesseursRepository.class)
                .findById(coursProgrammer.getProfesseurId())
                .orElseThrow(() -> new RuntimeException("Professor not found with ID: " + coursProgrammer.getProfesseurId()));

        // Validate that the professor is the author of the course
        if (!cours.getRedacteur().getId().equals(professeur.getId())) {
            throw new RuntimeException("Only the course author can schedule the course");
        }

        // Create and populate the scheduled course entity
        CoursProgrammerEntity entity = createScheduledCourseEntity(coursProgrammer, cours, professeur);

        // Save the scheduled course
        CoursProgrammerEntity savedEntity = daoAccessorService.getRepository(CoursProgrammerRepository.class).save(entity);

        // Update course status if needed
        updateCourseStatusAfterScheduling(cours);

        return mapToDto(savedEntity);
    }

    @Transactional
    public CoursProgrammer mettreAJourCoursProgramme(String id, CoursProgrammer coursProgrammer) {
        // Find existing scheduled course
        CoursProgrammerEntity existingEntity = daoAccessorService.getRepository(CoursProgrammerRepository.class)
                .findById(id)
                .orElseThrow(() -> new RuntimeException("Scheduled course not found with ID: " + id));

        // Only validate dates if they are being updated
        if (coursProgrammer.getDateCoursPrevue() != null) {
            validateCourseScheduleDates(coursProgrammer);
        }
        if (coursProgrammer.getDateDebutEffectif() != null || coursProgrammer.getDateFinEffectif() != null) {
            validateEffectiveDates(coursProgrammer);
        }
        // Update entity fields
        updateEntityFromDto(existingEntity, coursProgrammer);

        // Save updated entity
        CoursProgrammerEntity updatedEntity = daoAccessorService.getRepository(CoursProgrammerRepository.class).save(existingEntity);

        return mapToDto(updatedEntity);
    }
    private void validateCourseSchedule(CoursProgrammer coursProgrammer) {
        if (coursProgrammer.getDateCoursPrevue() == null) {
            throw new IllegalArgumentException("Date prévue du cours est requise");
        }

        if (coursProgrammer.getDateCoursPrevue().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Impossible de programmer un cours dans le passé");
        }

        if (coursProgrammer.getProfesseurId() == null) {
            throw new IllegalArgumentException("ID du professeur est requis");
        }

        if (coursProgrammer.getCoursId() == null) {
            throw new IllegalArgumentException("ID du cours est requis");
        }
    }

    @Transactional
    public void supprimerCoursProgramme(String id) {
        if (!daoAccessorService.getRepository(CoursProgrammerRepository.class).existsById(id)) {
            throw new RuntimeException("Scheduled course not found with ID: " + id);
        }
        daoAccessorService.getRepository(CoursProgrammerRepository.class).deleteById(id);
    }

    public CoursProgrammer obtenirCoursProgrammeParId(String id) {
        CoursProgrammerEntity entity = daoAccessorService.getRepository(CoursProgrammerRepository.class)
                .findById(id)
                .orElseThrow(() -> new RuntimeException("Scheduled course not found with ID: " + id));
        return mapToDto(entity);
    }

    public List<CoursProgrammer> obtenirTousLesCoursProgrammes() {
        return daoAccessorService.getRepository(CoursProgrammerRepository.class)
                .findAll()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private void updateEntityFromDto(CoursProgrammerEntity entity, CoursProgrammer dto) {
        // Update basic fields only if provided
        if (dto.getDateCoursPrevue() != null) {
            entity.setDateCoursPrevue(dto.getDateCoursPrevue());
        }
        if (dto.getDateDebutEffectif() != null) {
            entity.setDateDebutEffectif(dto.getDateDebutEffectif());
        }
        if (dto.getDateFinEffectif() != null) {
            entity.setDateFinEffectif(dto.getDateFinEffectif());
        }
        if (dto.getEtatCoursProgramme() != null) {
            entity.setEtatCoursProgramme(dto.getEtatCoursProgramme());
        }
        if (dto.getLieu() != null) {
            entity.setLieu(dto.getLieu());
        }
        if (dto.getDescription() != null) {
            entity.setDescription(dto.getDescription());
        }

        // Update classes if provided
        if (dto.getClassesIds() != null) {
            List<ClassesEntity> classes = dto.getClassesIds().stream()
                    .map(classId -> daoAccessorService.getRepository(ClassesRepository.class)
                            .findById(classId)
                            .orElseThrow(() -> new RuntimeException("Class not found with ID: " + classId)))
                    .collect(Collectors.toList());
            entity.setClasses(classes);
        }

        // Update participants if provided - VALIDATE ACCESS
        if (dto.getParticipantsIds() != null) {
            List<UtilisateursEntity> participants = validateAndGetParticipants(
                    dto.getParticipantsIds(),
                    dto.getClassesIds()
            );
            entity.setParticipants(participants);
        }
    }

    public List<CoursProgrammer> obtenirProgrammationParProfesseur(String professeurId) {
        return daoAccessorService.getRepository(CoursProgrammerRepository.class)
                .findByProfesseurId(professeurId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }


    private void validateCourseScheduleDates(CoursProgrammer coursProgrammer) {
        if (coursProgrammer.getDateCoursPrevue() == null) {
            throw new IllegalArgumentException("Scheduled date cannot be null");
        }

        if (coursProgrammer.getDateCoursPrevue().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Cannot schedule a course in the past");
        }

        if (coursProgrammer.getDateDebutEffectif() != null && coursProgrammer.getDateFinEffectif() != null
                && coursProgrammer.getDateDebutEffectif().isAfter(coursProgrammer.getDateFinEffectif())) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }
    }

    private void validateCourseStatusForScheduling(CoursEntity cours) {
        if (cours.getEtat() != EtatCours.BROUILLON && cours.getEtat() != EtatCours.PUBLIE) {
            throw new IllegalStateException("Course must be in BROUILLON or PUBLIE state to be scheduled. Current state: " + cours.getEtat());
        }
    }
    private void validateCourseStatus(CoursEntity cours) {
        if (cours.getEtat() != EtatCours.BROUILLON && cours.getEtat() != EtatCours.PUBLIE) {
            throw new IllegalStateException("Le cours doit être en état BROUILLON ou PUBLIE pour être programmé");
        }
    }
    private CoursProgrammerEntity createScheduledCourseEntity(CoursProgrammer coursProgrammer, CoursEntity cours, ProfesseursEntity professeur) {
        CoursProgrammerEntity entity = new CoursProgrammerEntity();
        entity.setId(UUID.randomUUID().toString());

        // Set basic fields
        entity.setDateCoursPrevue(coursProgrammer.getDateCoursPrevue());
        entity.setDateDebutEffectif(coursProgrammer.getDateDebutEffectif());
        entity.setDateFinEffectif(coursProgrammer.getDateFinEffectif());
        entity.setEtatCoursProgramme(coursProgrammer.getEtatCoursProgramme() != null ?
                coursProgrammer.getEtatCoursProgramme() : EtatCoursProgramme.PLANIFIE);
        entity.setLieu(coursProgrammer.getLieu());
        entity.setDescription(coursProgrammer.getDescription());
        entity.setCours(cours);
        entity.setProfesseur(professeur);

        // Set classes if provided
        if (coursProgrammer.getClassesIds() != null && !coursProgrammer.getClassesIds().isEmpty()) {
            List<ClassesEntity> classes = coursProgrammer.getClassesIds().stream()
                    .map(classId -> daoAccessorService.getRepository(ClassesRepository.class)
                            .findById(classId)
                            .orElseThrow(() -> new RuntimeException("Class not found with ID: " + classId)))
                    .collect(Collectors.toList());
            entity.setClasses(classes);
        }

        // Set participants if provided - VALIDATE ACCESS
        if (coursProgrammer.getParticipantsIds() != null && !coursProgrammer.getParticipantsIds().isEmpty()) {
            List<UtilisateursEntity> participants = validateAndGetParticipants(
                    coursProgrammer.getParticipantsIds(),
                    coursProgrammer.getClassesIds()
            );
            entity.setParticipants(participants);
        }

        return entity;
    }
    private List<UtilisateursEntity> validateAndGetParticipants(List<String> participantIds, List<String> classIds) {
        return participantIds.stream()
                .map(participantId -> {
                    UtilisateursEntity participant = daoAccessorService.getRepository(UtilisateursRepository.class)
                            .findById(participantId)
                            .orElseThrow(() -> new RuntimeException("User not found: " + participantId));

                    // Vérifier que l'utilisateur a accès aux classes spécifiées
                    if (classIds != null && !classIds.isEmpty()) {
                        boolean hasAccess = classIds.stream()
                                .allMatch(classId -> daoAccessorService.getRepository(AccederRepository.class)
                                        .existsByUtilisateurIdAndClasseId(participantId, classId));

                        if (!hasAccess) {
                            throw new RuntimeException("User " + participantId + " doesn't have access to all specified classes");
                        }
                    }

                    return participant;
                })
                .collect(Collectors.toList());
    }
    private void updateCourseStatusAfterScheduling(CoursEntity cours) {
        if (cours.getEtat() == EtatCours.BROUILLON) {
            cours.setEtat(EtatCours.PUBLIE);
            daoAccessorService.getRepository(CoursRepository.class).save(cours);
        }
    }
    private void updateCourseStatus(CoursEntity cours) {
        if (cours.getEtat() == EtatCours.BROUILLON) {
            cours.setEtat(EtatCours.PUBLIE);
            daoAccessorService.getRepository(CoursRepository.class).save(cours);
        }
    }
    public List<CoursProgrammer> obtenirProgrammationParCours(String coursId) {
        return daoAccessorService.getRepository(CoursProgrammerRepository.class)
                .findByCoursId(coursId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    private CoursProgrammer mapToDto(CoursProgrammerEntity entity) {
        CoursProgrammer dto = new CoursProgrammer();
        dto.setId(entity.getId());
        dto.setCoursId(entity.getCours().getId());
        dto.setProfesseurId(entity.getProfesseur().getId());
        dto.setDateCoursPrevue(entity.getDateCoursPrevue());
        dto.setDateDebutEffectif(entity.getDateDebutEffectif());
        dto.setDateFinEffectif(entity.getDateFinEffectif());
        dto.setEtatCoursProgramme(entity.getEtatCoursProgramme());
        dto.setLieu(entity.getLieu());
        dto.setDescription(entity.getDescription());


        // Map classes IDs
        if (entity.getClasses() != null) {
            dto.setClassesIds(entity.getClasses().stream()
                    .map(ClassesEntity::getId)
                    .collect(Collectors.toList()));
        }

        // Map participants IDs
        if (entity.getParticipants() != null) {
            dto.setParticipantsIds(entity.getParticipants().stream()
                    .map(UtilisateursEntity::getId)
                    .collect(Collectors.toList()));
        }

        return dto;
    }
    public List<CoursProgrammer> obtenirProgrammationParClasse(String classeId) {
        return daoAccessorService.getRepository(CoursProgrammerRepository.class)
                .findByClasseId(classeId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<CoursProgrammer> obtenirProgrammationParParticipant(String participantId) {
        return daoAccessorService.getRepository(CoursProgrammerRepository.class)
                .findByParticipantId(participantId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<CoursProgrammer> obtenirProgrammationAccessible(String userId) {
        return daoAccessorService.getRepository(CoursProgrammerRepository.class)
                .findByUserAccess(userId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    private void validateEffectiveDates(CoursProgrammer coursProgrammer) {
        // Only validate if both dates are provided
        if (coursProgrammer.getDateDebutEffectif() != null && coursProgrammer.getDateFinEffectif() != null) {
            if (coursProgrammer.getDateDebutEffectif().isBefore(coursProgrammer.getDateCoursPrevue())) {
                throw new IllegalArgumentException("La date de début effective ne peut pas être avant la date prévue du cours.");
            }
            if (coursProgrammer.getDateFinEffectif().isBefore(coursProgrammer.getDateDebutEffectif())) {
                throw new IllegalArgumentException("La date de fin effective doit être après la date de début effective.");
            }
            if (coursProgrammer.getDateFinEffectif().isEqual(coursProgrammer.getDateDebutEffectif()) &&
                    coursProgrammer.getDateFinEffectif().toLocalTime().isBefore(coursProgrammer.getDateDebutEffectif().toLocalTime())) {
                throw new IllegalArgumentException("Si la date de fin est le même jour que la date de début, l'heure de fin doit être après l'heure de début.");
            }
        }
        // Validate if only one date is provided
        if ((coursProgrammer.getDateDebutEffectif() != null && coursProgrammer.getDateFinEffectif() == null) ||
            (coursProgrammer.getDateDebutEffectif() == null && coursProgrammer.getDateFinEffectif() != null)) {
            throw new IllegalArgumentException("Si une date effective est fournie, les deux dates (début et fin) doivent être fournies.");
        }
    }


}