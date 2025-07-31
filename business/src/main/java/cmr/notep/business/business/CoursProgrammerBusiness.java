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

        // Get the course and validate its status
        CoursEntity cours = daoAccessorService.getRepository(CoursRepository.class)
                .findById(coursProgrammer.getCoursId())
                .orElseThrow(() -> new RuntimeException("Course not found with ID: " + coursProgrammer.getCoursId()));

        validateCourseStatusForScheduling(cours);

        // Create and populate the scheduled course entity
        CoursProgrammerEntity entity = createScheduledCourseEntity(coursProgrammer, cours);

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

        // Validate input dates
        validateCourseScheduleDates(coursProgrammer);

        // Update entity fields
        updateEntityFromDto(existingEntity, coursProgrammer);

        // Save updated entity
        CoursProgrammerEntity updatedEntity = daoAccessorService.getRepository(CoursProgrammerRepository.class).save(existingEntity);

        return mapToDto(updatedEntity);
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
        // Update basic fields
        entity.setDateCoursPrevue(dto.getDateCoursPrevue());
        entity.setDateDebutEffectif(dto.getDateDebutEffectif());
        entity.setDateFinEffectif(dto.getDateFinEffectif());
        entity.setEtatCoursProgramme(dto.getEtatCoursProgramme() != null ?
                dto.getEtatCoursProgramme() : EtatCoursProgramme.PLANIFIE);
        entity.setLieu(dto.getLieu());
        entity.setDescription(dto.getDescription());
        entity.setCapaciteMax(dto.getCapaciteMax());

        // Update class if provided
        if (dto.getClasseId() != null) {
            ClassesEntity classe = daoAccessorService.getRepository(ClassesRepository.class)
                    .findById(dto.getClasseId())
                    .orElseThrow(() -> new RuntimeException("Class not found with ID: " + dto.getClasseId()));
            entity.setClasse(classe);
        } else {
            entity.setClasse(null);
        }

        // Update participants if provided
        if (dto.getParticipantsIds() != null) {
            List<UtilisateursEntity> participants = dto.getParticipantsIds().stream()
                    .map(participantId -> daoAccessorService.getRepository(UtilisateursRepository.class)
                            .findById(participantId)
                            .orElseThrow(() -> new RuntimeException("User not found with ID: " + participantId)))
                    .collect(Collectors.toList());
            entity.setParticipants(participants);
        } else {
            entity.setParticipants(null);
        }
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

    private CoursProgrammerEntity createScheduledCourseEntity(CoursProgrammer coursProgrammer, CoursEntity cours) {
        CoursProgrammerEntity entity = new CoursProgrammerEntity();

        // Set basic fields
        entity.setDateCoursPrevue(coursProgrammer.getDateCoursPrevue());
        entity.setDateDebutEffectif(coursProgrammer.getDateDebutEffectif());
        entity.setDateFinEffectif(coursProgrammer.getDateFinEffectif());
        entity.setEtatCoursProgramme(coursProgrammer.getEtatCoursProgramme() != null ?
                coursProgrammer.getEtatCoursProgramme() : EtatCoursProgramme.PLANIFIE);
        entity.setLieu(coursProgrammer.getLieu());
        entity.setDescription(coursProgrammer.getDescription());
        entity.setCapaciteMax(coursProgrammer.getCapaciteMax());
        entity.setCours(cours);

        // Set class if provided
        if (coursProgrammer.getClasseId() != null) {
            ClassesEntity classe = daoAccessorService.getRepository(ClassesRepository.class)
                    .findById(coursProgrammer.getClasseId())
                    .orElseThrow(() -> new RuntimeException("Class not found with ID: " + coursProgrammer.getClasseId()));
            entity.setClasse(classe);
        }

        // Set participants if provided
        if (coursProgrammer.getParticipantsIds() != null && !coursProgrammer.getParticipantsIds().isEmpty()) {
            List<UtilisateursEntity> participants = coursProgrammer.getParticipantsIds().stream()
                    .map(id -> daoAccessorService.getRepository(UtilisateursRepository.class)
                            .findById(id)
                            .orElseThrow(() -> new RuntimeException("User not found: " + id)))
                    .collect(Collectors.toList());
            entity.setParticipants(participants);
        }

        return entity;
    }

    private void updateCourseStatusAfterScheduling(CoursEntity cours) {
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

    public List<CoursProgrammer> obtenirProgrammationParClasse(String classeId) {
        return daoAccessorService.getRepository(CoursProgrammerRepository.class)
                .findByClasseId(classeId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<CoursProgrammer> obtenirProgrammationParParticipant(String participantId) {
        return daoAccessorService.getRepository(CoursProgrammerRepository.class)
                .findByParticipantsId(participantId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private CoursProgrammer mapToDto(CoursProgrammerEntity entity) {
        CoursProgrammer dto = new CoursProgrammer();
        dto.setId(entity.getId());
        dto.setCoursId(entity.getCours().getId());
        dto.setDateCoursPrevue(entity.getDateCoursPrevue());
        dto.setDateDebutEffectif(entity.getDateDebutEffectif());
        dto.setDateFinEffectif(entity.getDateFinEffectif());
        dto.setEtatCoursProgramme(entity.getEtatCoursProgramme());
        dto.setLieu(entity.getLieu());
        dto.setDescription(entity.getDescription());
        dto.setCapaciteMax(entity.getCapaciteMax());

        if (entity.getClasse() != null) {
            dto.setClasseId(entity.getClasse().getId());
        }

        if (entity.getParticipants() != null) {
            dto.setParticipantsIds(entity.getParticipants().stream()
                    .map(UtilisateursEntity::getId)
                    .collect(Collectors.toList()));
        }

        return dto;
    }
}