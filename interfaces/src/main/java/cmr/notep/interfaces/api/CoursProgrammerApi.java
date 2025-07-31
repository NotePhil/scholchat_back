package cmr.notep.interfaces.api;

import cmr.notep.interfaces.modeles.CoursProgrammer;
import lombok.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/cours-programmes")
public interface CoursProgrammerApi {

    @PostMapping(
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.CREATED)
    CoursProgrammer programmerCours(@NonNull @RequestBody CoursProgrammer coursProgrammer);

    @PutMapping(
            path = "/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    CoursProgrammer mettreAJourCoursProgramme(
            @NonNull @PathVariable("id") String id,
            @NonNull @RequestBody CoursProgrammer coursProgrammer
    );

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void supprimerCoursProgramme(@NonNull @PathVariable("id") String id);

    @GetMapping(
            path = "/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    CoursProgrammer obtenirCoursProgrammeParId(@NonNull @PathVariable("id") String id);

    @GetMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    List<CoursProgrammer> obtenirTousLesCoursProgrammes();

    @GetMapping(
            path = "/by-cours/{coursId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    List<CoursProgrammer> obtenirProgrammationParCours(@NonNull @PathVariable("coursId") String coursId);

    @GetMapping(
            path = "/by-classe/{classeId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    List<CoursProgrammer> obtenirProgrammationParClasse(@NonNull @PathVariable("classeId") String classeId);

    @GetMapping(
            path = "/by-participant/{participantId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    List<CoursProgrammer> obtenirProgrammationParParticipant(@NonNull @PathVariable("participantId") String participantId);
}