package cmr.notep.interfaces.api;

import cmr.notep.interfaces.dto.RepondreRequestDTO;
import cmr.notep.interfaces.dto.RepondreResponseDTO;
import lombok.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/reponses")
public interface RepondreApi {

    @PostMapping(
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.CREATED)
    RepondreResponseDTO repondreQuestion(@NonNull @RequestBody RepondreRequestDTO repondre);

    @PutMapping(
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    RepondreResponseDTO mettreAJourReponse(@NonNull @RequestBody RepondreRequestDTO repondre);

    @GetMapping(
            path = "/utilisateur/{utilisateurId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    List<RepondreResponseDTO> obtenirReponsesParUtilisateur(@NonNull @PathVariable String utilisateurId);

    @GetMapping(
            path = "/question/{questionId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    List<RepondreResponseDTO> obtenirReponsesParQuestion(@NonNull @PathVariable String questionId);

    @GetMapping(
            path = "/exercise/{exerciseId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    List<RepondreResponseDTO> obtenirReponsesParExercise(@NonNull @PathVariable String exerciseId);

    @GetMapping(
            path = "/utilisateur/{utilisateurId}/question/{questionId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    RepondreResponseDTO obtenirReponse(
            @NonNull @PathVariable String utilisateurId,
            @NonNull @PathVariable String questionId
    );

    @DeleteMapping(
            path = "/utilisateur/{utilisateurId}/question/{questionId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void supprimerReponse(
            @NonNull @PathVariable String utilisateurId,
            @NonNull @PathVariable String questionId
    );
}