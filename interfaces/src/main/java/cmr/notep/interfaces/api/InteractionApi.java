package cmr.notep.interfaces.api;

import cmr.notep.interfaces.modeles.Interaction;
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
    Interaction createInteraction(@RequestBody Interaction interaction);

    @GetMapping(
            path = "/event/{eventId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    List<Interaction> getInteractionsByEvent(@PathVariable String eventId);

    @GetMapping(
            path = "/message/{messageId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    List<Interaction> getInteractionsByMessage(@PathVariable String messageId);

    @GetMapping(
            path = "/user/{userId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseStatus(HttpStatus.OK)
    List<Interaction> getInteractionsByUser(@PathVariable String userId);
}