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
    Messages avoirMessage(@NonNull @RequestParam String idMessage);

    @GetMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    List<Messages> avoirToutMessages();

    @PostMapping(
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    Messages posterMessage(@NonNull @RequestBody Messages message);
}
