package cmr.notep.interfaces.api;

import cmr.notep.interfaces.modeles.Messages;
import lombok.NonNull;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/messages")
public interface MessagesApi {
    @GetMapping(
            path = "/{idMessage}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    Messages getMessage(@NonNull @PathVariable(name ="idMessage") String idMessage);

    @GetMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    List<Messages> getAllMessages();

    @PostMapping(
            path = "/individual",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    Messages sendIndividualMessage(@NonNull @RequestBody Messages message);

    @PostMapping(
            path = "/group",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    Messages sendClassGroupMessage(@NonNull @RequestBody Messages message);
}