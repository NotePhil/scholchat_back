package cmr.notep.interfaces.api;

import cmr.notep.interfaces.dto.QuestionReponseRequestDTO;
import cmr.notep.interfaces.dto.QuestionReponseResponseDTO;
import lombok.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/questions")
public interface QuestionReponseApi {

    @PostMapping(
            path = "/exercise/{exerciseId}",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.CREATED)
    QuestionReponseResponseDTO creerQuestion(
            @NonNull @PathVariable String exerciseId,
            @NonNull @RequestBody QuestionReponseRequestDTO question
    );

    @GetMapping(
            path = "/exercise/{exerciseId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    List<QuestionReponseResponseDTO> obtenirQuestionsParExercise(@NonNull @PathVariable String exerciseId);

    @PutMapping(
            path = "/{questionId}",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    QuestionReponseResponseDTO mettreAJourQuestion(
            @NonNull @PathVariable String questionId,
            @NonNull @RequestBody QuestionReponseRequestDTO question
    );

    @DeleteMapping(
            path = "/{questionId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void supprimerQuestion(@NonNull @PathVariable String questionId);
}