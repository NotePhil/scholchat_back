package cmr.notep.interfaces.api;

import cmr.notep.interfaces.dto.CoursProgressionDTO;
import cmr.notep.interfaces.modeles.Cours;
import cmr.notep.modele.EtatCours;
import lombok.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/cours")
public interface CoursApi {

    @PostMapping(
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.CREATED)
    Cours creerCours(@NonNull @RequestBody Cours cours);

    @GetMapping(
            path = "/professeur/{professeurId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    List<Cours> obtenirCoursParProfesseur(@NonNull @PathVariable String professeurId);

    @GetMapping(
            path = "/etat/{etat}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    List<Cours> obtenirCoursParEtat(@NonNull @PathVariable EtatCours etat);

    @GetMapping(
            path = "/accessibles/{userId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    List<Cours> obtenirCoursAccessibles(@NonNull @PathVariable String userId);

    @GetMapping(
            path = "/{coursId}/complet",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    Cours obtenirCoursAvecChapitres(@NonNull @PathVariable String coursId);



    @PutMapping(
            path = "/{coursId}",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    Cours mettreAJourCours(@NonNull @PathVariable String coursId, @NonNull @RequestBody Cours cours);

    @DeleteMapping(
            path = "/{coursId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void supprimerCours(@NonNull @PathVariable String coursId);

    @GetMapping(
            path = "/{coursId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    Cours obtenirCoursParId(@NonNull @PathVariable String coursId);

    @PostMapping(
            path = "/{coursId}/chapitres/{chapitreId}/complete/{utilisateurId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    void marquerChapitreComplete(@NonNull @PathVariable String coursId,
                                  @NonNull @PathVariable String chapitreId,
                                  @NonNull @PathVariable String utilisateurId);

    @GetMapping(
            path = "/{coursId}/progression/{utilisateurId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    CoursProgressionDTO obtenirProgression(@NonNull @PathVariable String coursId,
                                            @NonNull @PathVariable String utilisateurId);

    @GetMapping(
            path = "/restriction/{restriction}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    List<Cours> obtenirCoursParRestriction(@NonNull @PathVariable String restriction);
}