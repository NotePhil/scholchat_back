package cmr.notep.interfaces.api;

import cmr.notep.interfaces.dto.GroupMessageDto;
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
    Messages avoirMessage(@NonNull @PathVariable(name ="idMessage") String idMessage);

    @GetMapping(
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    List<Messages> avoirToutMessages();

    @PostMapping(
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    Messages posterMessage(@NonNull @RequestBody Messages message);

    @GetMapping(
            path = "/utilisateur/{utilisateurId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    List<Messages> obtenirMessagesParUtilisateur(@PathVariable String utilisateurId);
    
    @GetMapping(
            path = "/utilisateur/{utilisateurId}/sent",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    List<Messages> obtenirMessagesEnvoyes(@PathVariable String utilisateurId);
    
    @GetMapping(
            path = "/utilisateur/{utilisateurId}/received",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    List<Messages> obtenirMessagesRecus(@PathVariable String utilisateurId);
    
    @PostMapping(
            path = "/group",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    Messages posterMessageGroupe(@NonNull @RequestBody GroupMessageDto groupMessageDto);
}