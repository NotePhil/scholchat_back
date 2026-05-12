package cmr.notep.interfaces.api;

import cmr.notep.interfaces.dto.ParticipationExerciseRequestDTO;
import cmr.notep.interfaces.dto.ParticipationExerciseResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/participations-exercises")
public interface ParticipationExerciseApi {

    @PostMapping(
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.CREATED)
    ParticipationExerciseResponseDTO participerAExercise(@RequestBody ParticipationExerciseRequestDTO participation);

    @PutMapping(
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    ParticipationExerciseResponseDTO mettreAJourParticipation(@RequestBody ParticipationExerciseRequestDTO participation);

    @GetMapping(
            path = "/utilisateur/{utilisateurId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    List<ParticipationExerciseResponseDTO> obtenirParticipationsParUtilisateur(@PathVariable String utilisateurId);

    @GetMapping(
            path = "/exercise/{exerciseProgrammerId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    List<ParticipationExerciseResponseDTO> obtenirParticipationsParExercise(@PathVariable String exerciseProgrammerId);

    @GetMapping(
            path = "/exercise/{exerciseProgrammerId}/en-attente-correction",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    List<ParticipationExerciseResponseDTO> obtenirParticipationsEnAttenteCorrection(@PathVariable String exerciseProgrammerId);

    @GetMapping(
            path = "/professeur/{professeurId}/a-corriger",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    List<ParticipationExerciseResponseDTO> obtenirToutesParticipationsACorriger(@PathVariable String professeurId);

    @DeleteMapping(
            path = "/utilisateur/{utilisateurId}/exercise/{exerciseProgrammerId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void supprimerParticipation(
            @PathVariable String utilisateurId,
            @PathVariable String exerciseProgrammerId
    );
}