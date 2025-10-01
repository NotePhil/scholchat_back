package cmr.notep.business.impl;

import cmr.notep.business.business.ParticipationExerciseBusiness;
import cmr.notep.interfaces.api.ParticipationExerciseApi;
import cmr.notep.interfaces.dto.ParticipationExerciseRequestDTO;
import cmr.notep.interfaces.dto.ParticipationExerciseResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
public class ParticipationExerciseService implements ParticipationExerciseApi {

    private final ParticipationExerciseBusiness participationExerciseBusiness;

    @Override
    public ParticipationExerciseResponseDTO participerAExercise(ParticipationExerciseRequestDTO participation) {
        log.info("Demande de participation à un exercice programmé");
        return participationExerciseBusiness.participerAExercise(participation);
    }

    @Override
    public ParticipationExerciseResponseDTO mettreAJourParticipation(ParticipationExerciseRequestDTO participation) {
        log.info("Mise à jour d'une participation à un exercice programmé");
        return participationExerciseBusiness.mettreAJourParticipation(participation);
    }

    @Override
    public List<ParticipationExerciseResponseDTO> obtenirParticipationsParUtilisateur(String utilisateurId) {
        log.info("Récupération des participations pour l'utilisateur: {}", utilisateurId);
        return participationExerciseBusiness.obtenirParticipationsParUtilisateur(utilisateurId);
    }

    @Override
    public List<ParticipationExerciseResponseDTO> obtenirParticipationsParExercise(String exerciseProgrammerId) {
        log.info("Récupération des participations pour l'exercice programmé: {}", exerciseProgrammerId);
        return participationExerciseBusiness.obtenirParticipationsParExercise(exerciseProgrammerId);
    }

    @Override
    public void supprimerParticipation(String utilisateurId, String exerciseProgrammerId) {
        log.info("Suppression de la participation de l'utilisateur {} à l'exercice {}",
                utilisateurId, exerciseProgrammerId);
        participationExerciseBusiness.supprimerParticipation(utilisateurId, exerciseProgrammerId);
    }
}