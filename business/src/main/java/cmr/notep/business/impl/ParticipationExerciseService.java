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
    @org.springframework.beans.factory.annotation.Autowired
    private cmr.notep.business.security.CurrentUserService currentUser;
    @org.springframework.beans.factory.annotation.Autowired
    private cmr.notep.business.security.AccessControlService accessControl;


    private final ParticipationExerciseBusiness participationExerciseBusiness;

    @Override
    public ParticipationExerciseResponseDTO participerAExercise(ParticipationExerciseRequestDTO participation) {
        if (!accessControl.canActForMinorChild(participation.getUtilisateurId())) {
            currentUser.requireSelfOrAdmin(participation.getUtilisateurId());
        }
        log.info("Demande de participation à un exercice programmé");
        return participationExerciseBusiness.participerAExercise(participation);
    }

    @Override
    public ParticipationExerciseResponseDTO mettreAJourParticipation(ParticipationExerciseRequestDTO participation) {
        accessControl.requireCanUpdateParticipation(participation.getUtilisateurId(), participation.getExerciseProgrammerId());
        log.info("Mise à jour d'une participation à un exercice programmé");
        return participationExerciseBusiness.mettreAJourParticipation(participation);
    }

    @Override
    public List<ParticipationExerciseResponseDTO> obtenirParticipationsParUtilisateur(String utilisateurId) {
        accessControl.requireSelfParentOrTeacher(utilisateurId);
        log.info("Récupération des participations pour l'utilisateur: {}", utilisateurId);
        return participationExerciseBusiness.obtenirParticipationsParUtilisateur(utilisateurId);
    }

    @Override
    public List<ParticipationExerciseResponseDTO> obtenirParticipationsParExercise(String exerciseProgrammerId) {
        accessControl.requireExerciseProgrammeTeacher(exerciseProgrammerId);
        log.info("Récupération des participations pour l'exercice programmé: {}", exerciseProgrammerId);
        return participationExerciseBusiness.obtenirParticipationsParExercise(exerciseProgrammerId);
    }

    @Override
    public List<ParticipationExerciseResponseDTO> obtenirParticipationsEnAttenteCorrection(String exerciseProgrammerId) {
        accessControl.requireExerciseProgrammeTeacher(exerciseProgrammerId);
        log.info("Récupération des participations EN_ATTENTE_CORRECTION pour l'exercice: {}", exerciseProgrammerId);
        return participationExerciseBusiness.obtenirParticipationsEnAttenteCorrection(exerciseProgrammerId);
    }

    @Override
    public List<ParticipationExerciseResponseDTO> obtenirToutesParticipationsACorriger(String professeurId) {
        currentUser.requireSelfOrAdmin(professeurId);
        log.info("Récupération de toutes les participations à corriger pour le professeur: {}", professeurId);
        return participationExerciseBusiness.obtenirToutesParticipationsACorriger(professeurId);
    }

    @Override
    public void supprimerParticipation(String utilisateurId, String exerciseProgrammerId) {
        if (!currentUser.isSelf(utilisateurId)) accessControl.requireExerciseProgrammeTeacher(exerciseProgrammerId);
        log.info("Suppression de la participation de l'utilisateur {} à l'exercice {}",
                utilisateurId, exerciseProgrammerId);
        participationExerciseBusiness.supprimerParticipation(utilisateurId, exerciseProgrammerId);
    }
}