package cmr.notep.interfaces.api;

import cmr.notep.interfaces.dto.GroupMessageDto;
import cmr.notep.interfaces.dto.MessageStatutDTO;
import cmr.notep.interfaces.modeles.MessageDto;
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
    List<MessageDto> obtenirMessagesEnvoyes(@PathVariable String utilisateurId);
    
    @GetMapping(
            path = "/utilisateur/{utilisateurId}/received",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    List<MessageDto> obtenirMessagesRecus(@PathVariable String utilisateurId);
    
    @PostMapping(
            path = "/{messageId}/statut/{utilisateurId}/lu",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    MessageStatutDTO marquerLu(@PathVariable String messageId,
                               @PathVariable String utilisateurId,
                               @RequestParam boolean lu);

    @PostMapping(
            path = "/{messageId}/statut/{utilisateurId}/favori",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    MessageStatutDTO marquerFavori(@PathVariable String messageId,
                                   @PathVariable String utilisateurId,
                                   @RequestParam boolean favori);

    @GetMapping(
            path = "/{messageId}/statut/{utilisateurId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    MessageStatutDTO obtenirStatut(@PathVariable String messageId,
                                   @PathVariable String utilisateurId);

    @GetMapping(
            path = "/utilisateur/{utilisateurId}/favoris",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    List<MessageStatutDTO> obtenirFavoris(@PathVariable String utilisateurId);

    @GetMapping(
            path = "/utilisateur/{utilisateurId}/non-lus",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    List<MessageStatutDTO> obtenirNonLus(@PathVariable String utilisateurId);

    @PostMapping(
            path = "/group",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    Messages posterMessageGroupe(@NonNull @RequestBody GroupMessageDto groupMessageDto);
    
    @DeleteMapping(
            path = "/{messageId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    void supprimerMessage(@PathVariable String messageId);
    
    @GetMapping(
            path = "/utilisateur/{utilisateurId}/trash",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    List<MessageDto> obtenirMessagesCorbeille(@PathVariable String utilisateurId);
    
    @DeleteMapping(
            path = "/trash/cleanup",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    void viderCorbeille();
    
    @PostMapping(
            path = "/{messageId}/restore",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    void restaurerMessage(@PathVariable String messageId);
}