package cmr.notep.interfaces.api;

import cmr.notep.interfaces.modeles.Evenement;
import cmr.notep.interfaces.modeles.Interaction;
import cmr.notep.interfaces.modeles.CommentRequest;
import cmr.notep.interfaces.modeles.PagedResponse;
import lombok.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RequestMapping("/evenements")
public interface EvenementApi {

    @PostMapping(
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.CREATED)
    Evenement creerEvenement(@NonNull @RequestBody Evenement evenement);

    @PutMapping(
            path = "/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    Evenement mettreAJourEvenement(
            @NonNull @PathVariable("id") String id,
            @NonNull @RequestBody Evenement evenement
    );

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void supprimerEvenement(@NonNull @PathVariable("id") String id);

    @GetMapping(
            path = "/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    Evenement obtenirEvenementParId(@NonNull @PathVariable("id") String id);

    @GetMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    List<Evenement> obtenirTousEvenements();

    /**
     * Paginated fetch. Returns page metadata + the events for that page.
     * Reuses the generic {@link PagedResponse} wrapper so other modules
     * (courses, exercises…) can adopt the same contract.
     */
    @GetMapping(
            path = "/pagines",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    PagedResponse<Evenement> obtenirEvenementsPagines(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    );

    @GetMapping(
            path = "/professeur/{professeurId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    List<Evenement> obtenirEvenementsParProfesseur(@NonNull @PathVariable("professeurId") String professeurId);

    // Nouveaux endpoints pour les interactions
    @PostMapping(
            path = "/{eventId}/like",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    void likerEvenement(@NonNull @PathVariable("eventId") String eventId);

    @PostMapping(
            path = "/{eventId}/comment",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.CREATED)
    Interaction commenterEvenement(
            @NonNull @PathVariable("eventId") String eventId,
            @NonNull @RequestBody CommentRequest commentRequest
    );

    @PostMapping(
            path = "/{eventId}/join",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    void rejoindreEvenement(@NonNull @PathVariable("eventId") String eventId);

    @PostMapping(
            path = "/{eventId}/unjoin",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    void quitterEvenement(@NonNull @PathVariable("eventId") String eventId);
}