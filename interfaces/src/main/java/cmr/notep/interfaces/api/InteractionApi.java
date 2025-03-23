package cmr.notep.interfaces.api;

import cmr.notep.interfaces.modeles.Interaction;
import lombok.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/interactions")
public interface InteractionApi {

    @PostMapping(
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.CREATED)
    Interaction creerInteraction(@NonNull @RequestBody Interaction interaction);

    @PutMapping(
            path = "/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    Interaction modifierInteraction(
            @NonNull @PathVariable("id") String id,
            @NonNull @RequestBody Interaction interactionModifie
    );

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void supprimerInteraction(@NonNull @PathVariable("id") String id);

    @GetMapping(
            path = "/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    Interaction obtenirInteractionParId(@NonNull @PathVariable("id") String id);

    @GetMapping(
            path = "/evenement/{evenementId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    List<Interaction> obtenirInteractionsParEvenement(@NonNull @PathVariable("evenementId") String evenementId);
}