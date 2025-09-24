package cmr.notep.interfaces.api;

import cmr.notep.interfaces.dto.ExerciseRequestDTO;
import cmr.notep.interfaces.dto.ExerciseResponseDTO;
import cmr.notep.modele.EtatCours;
import cmr.notep.modele.ListeNiveau;
import lombok.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RequestMapping("/exercises")
public interface ExerciseApi {

    @PostMapping(
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.CREATED)
    ExerciseResponseDTO creerExercise(@NonNull @RequestBody ExerciseRequestDTO exercise);

    @PutMapping(
            path = "/{exerciseId}",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    ExerciseResponseDTO mettreAJourExercise(@NonNull @PathVariable String exerciseId, @NonNull @RequestBody ExerciseRequestDTO exercise);

    @DeleteMapping(
            path = "/{exerciseId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void supprimerExercise(@NonNull @PathVariable String exerciseId);

    @GetMapping(
            path = "/{exerciseId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    ExerciseResponseDTO obtenirExerciseParId(@NonNull @PathVariable String exerciseId);

    @GetMapping(
            path = "/professeur/{professeurId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    List<ExerciseResponseDTO> obtenirExercisesParProfesseur(@NonNull @PathVariable String professeurId);

    @GetMapping(
            path = "/niveau/{niveau}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    List<ExerciseResponseDTO> obtenirExercisesParNiveau(@NonNull @PathVariable ListeNiveau niveau);

    @GetMapping(
            path = "/accessibles/{userId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    List<ExerciseResponseDTO> obtenirExercisesAccessibles(@NonNull @PathVariable String userId);

    @GetMapping(
            path = "/cours/{coursId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    List<ExerciseResponseDTO> obtenirExercisesParCours(@NonNull @PathVariable String coursId);

    @PostMapping(
            path = "/{exerciseId}/lier-cours/{coursId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    ExerciseResponseDTO lierExerciseACours(@NonNull @PathVariable String exerciseId, @NonNull @PathVariable String coursId);

    @PostMapping(
            path = "/{exerciseId}/delier-cours/{coursId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    ExerciseResponseDTO delierExerciseDeCours(@NonNull @PathVariable String exerciseId, @NonNull @PathVariable String coursId);
}
