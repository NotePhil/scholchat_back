package cmr.notep.interfaces.api;

import cmr.notep.interfaces.dto.ChatMessageDto;
import cmr.notep.interfaces.dto.ChatMessagePageDto;
import cmr.notep.interfaces.dto.ConversationDto;
import cmr.notep.interfaces.dto.CorbeilleDto;
import cmr.notep.interfaces.dto.CreerConversationRequest;
import cmr.notep.interfaces.dto.EnvoyerMessageChatRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** Messagerie de type chat : conversations DIRECT / GROUPE (participants uniquement). */
@RequestMapping("/conversations")
public interface ConversationsApi {

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    List<ConversationDto> mesConversations();

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    ConversationDto creerConversation(@RequestBody CreerConversationRequest request);

    @GetMapping(path = "/non-lus/count", produces = MediaType.APPLICATION_JSON_VALUE)
    Map<String, Long> compterNonLus();

    // ── Corbeille de l'appelant ──

    @GetMapping(path = "/corbeille", produces = MediaType.APPLICATION_JSON_VALUE)
    CorbeilleDto corbeille();

    /** Vider la corbeille : suppression définitive de toutes les discussions et de tous les messages qu'elle contient. */
    @DeleteMapping(path = "/corbeille")
    void viderCorbeille();

    @PostMapping(path = "/corbeille/messages/{messageId}/restaurer")
    void restaurerMessage(@PathVariable("messageId") String messageId);

    @DeleteMapping(path = "/corbeille/messages/{messageId}")
    void supprimerMessageDefinitivement(@PathVariable("messageId") String messageId);

    @PostMapping(path = "/{id}/restaurer", produces = MediaType.APPLICATION_JSON_VALUE)
    ConversationDto restaurerConversation(@PathVariable("id") String id);

    @DeleteMapping(path = "/{id}/definitif")
    void supprimerConversationDefinitivement(@PathVariable("id") String id);

    // ── Filtres (favori, non lu) ──

    @PutMapping(path = "/{id}/favori", produces = MediaType.APPLICATION_JSON_VALUE)
    ConversationDto definirFavori(@PathVariable("id") String id,
                                  @RequestParam(name = "favori", defaultValue = "true") boolean favori);

    /** « Marquer comme non lu » ; « Marquer comme lu » = POST /{id}/lu. */
    @PutMapping(path = "/{id}/non-lu", produces = MediaType.APPLICATION_JSON_VALUE)
    ConversationDto marquerNonLue(@PathVariable("id") String id);

    @GetMapping(path = "/par-message/{messageId}", produces = MediaType.APPLICATION_JSON_VALUE)
    ConversationDto conversationDuMessage(@PathVariable("messageId") String messageId);

    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    ConversationDto obtenirConversation(@PathVariable("id") String id);

    @GetMapping(path = "/{id}/messages", produces = MediaType.APPLICATION_JSON_VALUE)
    ChatMessagePageDto messages(@PathVariable("id") String id,
                                @RequestParam(name = "before", required = false) String before,
                                @RequestParam(name = "size", required = false, defaultValue = "30") int size);

    @PostMapping(path = "/{id}/messages", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    ChatMessageDto envoyer(@PathVariable("id") String id, @RequestBody EnvoyerMessageChatRequest request);

    @PostMapping(path = "/{id}/lu", produces = MediaType.APPLICATION_JSON_VALUE)
    Map<String, String> marquerLue(@PathVariable("id") String id);

    @DeleteMapping(path = "/{id}/messages/{messageId}")
    void supprimerMessage(@PathVariable("id") String id, @PathVariable("messageId") String messageId,
                          @RequestParam(name = "pourTous", required = false, defaultValue = "false") boolean pourTous);

    /** « Supprimer la discussion » : déplacée dans la corbeille de l'appelant (restaurable). */
    @DeleteMapping(path = "/{id}")
    void masquerConversation(@PathVariable("id") String id);

    @PutMapping(path = "/{id}/muet", produces = MediaType.APPLICATION_JSON_VALUE)
    ConversationDto definirMuet(@PathVariable("id") String id,
                                @RequestParam(name = "muted", defaultValue = "true") boolean muted);
}
