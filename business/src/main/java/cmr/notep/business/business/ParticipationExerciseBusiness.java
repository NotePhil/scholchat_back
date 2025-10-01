package cmr.notep.business.business;

import cmr.notep.business.exceptions.SchoolException;
import cmr.notep.business.exceptions.enums.SchoolErrorCode;
import cmr.notep.interfaces.dto.ParticipationExerciseRequestDTO;
import cmr.notep.interfaces.dto.ParticipationExerciseResponseDTO;
import cmr.notep.ressourcesjpa.commun.DaoAccessorService;
import cmr.notep.ressourcesjpa.dao.ExerciseProgrammerEntity;
import cmr.notep.ressourcesjpa.dao.ParticiperExoEntity;
import cmr.notep.ressourcesjpa.dao.UtilisateursEntity;
import cmr.notep.ressourcesjpa.dao.ParticiperExoId;
import cmr.notep.ressourcesjpa.repository.ExerciseProgrammerRepository;
import cmr.notep.ressourcesjpa.repository.ParticiperExoRepository;
import cmr.notep.ressourcesjpa.repository.UtilisateursRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class ParticipationExerciseBusiness {

    private final DaoAccessorService daoAccessorService;

    public ParticipationExerciseResponseDTO participerAExercise(ParticipationExerciseRequestDTO requestDTO) {
        log.info("Participation de l'utilisateur {} à l'exercice programmé {}",
                requestDTO.getUtilisateurId(), requestDTO.getExerciseProgrammerId());

        // Vérifier l'existence de l'utilisateur
        UtilisateursEntity utilisateur = daoAccessorService.getRepository(UtilisateursRepository.class)
                .findById(requestDTO.getUtilisateurId())
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Utilisateur introuvable"));

        // Vérifier l'existence de l'exercice programmé
        ExerciseProgrammerEntity exerciseProgrammer = daoAccessorService.getRepository(ExerciseProgrammerRepository.class)
                .findById(requestDTO.getExerciseProgrammerId())
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Exercice programmé introuvable"));

        // Vérifier si la participation existe déjà
        ParticiperExoRepository repository = daoAccessorService.getRepository(ParticiperExoRepository.class);
        if (repository.existsByUtilisateurIdAndExerciseProgrammerId(
                requestDTO.getUtilisateurId(), requestDTO.getExerciseProgrammerId())) {
            throw new SchoolException(SchoolErrorCode.ALREADY_EXISTS,
                    "L'utilisateur participe déjà à cet exercice");
        }

        // Créer la participation
        ParticiperExoEntity participation = new ParticiperExoEntity();
        participation.setId(new ParticiperExoId(requestDTO.getUtilisateurId(), requestDTO.getExerciseProgrammerId()));
        participation.setUtilisateur(utilisateur);
        participation.setExerciseProgrammer(exerciseProgrammer);
        participation.setDateDebut(requestDTO.getDateDebut() != null ? requestDTO.getDateDebut() : new Date());
        participation.setDateFin(requestDTO.getDateFin());
        participation.setNote(requestDTO.getNote());
        participation.setAppreciation(requestDTO.getAppreciation());
        participation.setDateSoumission(new Date());

        ParticiperExoEntity savedParticipation = repository.save(participation);

        log.info("Participation créée avec succès");
        return mapToResponseDTO(savedParticipation);
    }

    public ParticipationExerciseResponseDTO mettreAJourParticipation(ParticipationExerciseRequestDTO requestDTO) {
        log.info("Mise à jour de la participation de l'utilisateur {} à l'exercice programmé {}",
                requestDTO.getUtilisateurId(), requestDTO.getExerciseProgrammerId());

        ParticiperExoRepository repository = daoAccessorService.getRepository(ParticiperExoRepository.class);
        ParticiperExoEntity participation = repository
                .findByUtilisateurIdAndExerciseProgrammerId(
                        requestDTO.getUtilisateurId(), requestDTO.getExerciseProgrammerId())
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Participation introuvable"));

        // Mettre à jour les champs
        if (requestDTO.getDateDebut() != null) {
            participation.setDateDebut(requestDTO.getDateDebut());
        }
        if (requestDTO.getDateFin() != null) {
            participation.setDateFin(requestDTO.getDateFin());
        }
        if (requestDTO.getNote() != null) {
            participation.setNote(requestDTO.getNote());
        }
        if (requestDTO.getAppreciation() != null) {
            participation.setAppreciation(requestDTO.getAppreciation());
        }

        ParticiperExoEntity updatedParticipation = repository.save(participation);

        log.info("Participation mise à jour avec succès");
        return mapToResponseDTO(updatedParticipation);
    }

    public List<ParticipationExerciseResponseDTO> obtenirParticipationsParUtilisateur(String utilisateurId) {
        log.info("Récupération des participations de l'utilisateur: {}", utilisateurId);

        ParticiperExoRepository repository = daoAccessorService.getRepository(ParticiperExoRepository.class);
        return repository.findByUtilisateurId(utilisateurId)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public List<ParticipationExerciseResponseDTO> obtenirParticipationsParExercise(String exerciseProgrammerId) {
        log.info("Récupération des participations à l'exercice programmé: {}", exerciseProgrammerId);

        ParticiperExoRepository repository = daoAccessorService.getRepository(ParticiperExoRepository.class);
        return repository.findByExerciseProgrammerId(exerciseProgrammerId)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    public void supprimerParticipation(String utilisateurId, String exerciseProgrammerId) {
        log.info("Suppression de la participation de l'utilisateur {} à l'exercice {}",
                utilisateurId, exerciseProgrammerId);

        ParticiperExoRepository repository = daoAccessorService.getRepository(ParticiperExoRepository.class);
        ParticiperExoEntity participation = repository
                .findByUtilisateurIdAndExerciseProgrammerId(utilisateurId, exerciseProgrammerId)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Participation introuvable"));

        repository.delete(participation);
        log.info("Participation supprimée avec succès");
    }

    private ParticipationExerciseResponseDTO mapToResponseDTO(ParticiperExoEntity participation) {
        return ParticipationExerciseResponseDTO.builder()
                .utilisateurId(participation.getUtilisateur().getId())
                .utilisateurNom(participation.getUtilisateur().getNom())
                .utilisateurPrenom(participation.getUtilisateur().getPrenom())
                .exerciseProgrammerId(participation.getExerciseProgrammer().getId())
                .exerciseProgrammerNom(participation.getExerciseProgrammer().getNom())
                .note(participation.getNote())
                .appreciation(participation.getAppreciation())
                .dateDebut(participation.getDateDebut())
                .dateFin(participation.getDateFin())
                .dateSoumission(participation.getDateSoumission())
                .build();
    }
}