package cmr.notep.interfaces.api;

import cmr.notep.interfaces.dto.ExerciseProgrammerRequestDTO;
import cmr.notep.interfaces.dto.ExerciseProgrammerResponseDTO;
import cmr.notep.modele.EtatExercise;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/exercises-programmer")
public interface ExerciseProgrammerApi {

    /**
     * Programme un exercice (sans diffusion). Body : classeIds, coursId (même cours pour toutes les classes) et/ou
     * coursParClasse {classeId: coursId|null} (un cours par classe, null = « Exercice général »). Chaque couple
     * classe/cours est validé (400 COURS_NON_PROGRAMME_DANS_CLASSE, message nommant la classe). Une programmation par
     * cours distinct : réponse = la première programmation + {@code programmations} (toutes) + {@code
     * nombreProgrammations}.
     */
    @PostMapping(
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.CREATED)
    ExerciseProgrammerResponseDTO programmerExercise(@RequestBody ExerciseProgrammerRequestDTO exerciseProgrammer);
    /** Comme POST /, puis diffuse chaque programmation dans ses classes et notifie leurs élèves (une fois chacun). */
    @PostMapping(
            path = "/programmer-et-diffuser",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.CREATED)
    ExerciseProgrammerResponseDTO programmerEtDiffuserExercise(@RequestBody ExerciseProgrammerRequestDTO exerciseProgrammer);
    @PostMapping(
            path = "/{exerciseProgrammerId}/diffuser-classe/{classeId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    ExerciseProgrammerResponseDTO diffuserExerciseDansClasse(
            @PathVariable String exerciseProgrammerId,
            @PathVariable String classeId
    );

    @PostMapping(
            path = "/{exerciseProgrammerId}/retirer-classe/{classeId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    ExerciseProgrammerResponseDTO retirerExerciseDeClasse(
            @PathVariable String exerciseProgrammerId,
            @PathVariable String classeId
    );

    @GetMapping(
            path = "/professeur/{professeurId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    List<ExerciseProgrammerResponseDTO> obtenirExercisesProgrammesParProfesseur(@PathVariable String professeurId);

    @GetMapping(
            path = "/classe/{classeId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    List<ExerciseProgrammerResponseDTO> obtenirExercisesProgrammesParClasse(@PathVariable String classeId);

    @PatchMapping(
            path = "/{exerciseProgrammerId}/etat",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    ExerciseProgrammerResponseDTO mettreAJourEtatExerciseProgramme(
            @PathVariable String exerciseProgrammerId,
            @RequestParam EtatExercise nouvelEtat
    );

    /**
     * Rattache l'exercice programmé à un cours programmé dans ses classes de diffusion, ou le retire de son cours
     * (body {"coursId": null} ou {"coursId": ""} -> « Exercices généraux »). Professeur responsable / admin.
     * 400 COURS_NON_PROGRAMME_DANS_CLASSE si le cours n'existe pas ou n'est pas programmé dans une des classes.
     */
    @PatchMapping(
            path = "/{exerciseProgrammerId}/cours",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    ExerciseProgrammerResponseDTO changerCoursExerciseProgramme(
            @PathVariable String exerciseProgrammerId,
            @RequestBody java.util.Map<String, String> body
    );

    @GetMapping(
            path = "/{exerciseProgrammerId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    ExerciseProgrammerResponseDTO obtenirExerciseProgrammeParId(@PathVariable String exerciseProgrammerId);

    @GetMapping(
            path = "/exercise/{exerciseId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    List<ExerciseProgrammerResponseDTO> obtenirExercisesProgrammesParExerciseId(@PathVariable String exerciseId);

    @DeleteMapping(
            path = "/{exerciseProgrammerId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void supprimerExerciseProgramme(@PathVariable String exerciseProgrammerId);
}