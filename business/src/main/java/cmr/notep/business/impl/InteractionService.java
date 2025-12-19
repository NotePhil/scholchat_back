package cmr.notep.business.impl;

import cmr.notep.business.business.InteractionBusiness;
import cmr.notep.interfaces.api.InteractionApi;
import cmr.notep.interfaces.modeles.Interaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
public class InteractionService implements InteractionApi {
    private final InteractionBusiness interactionBusiness;

    @Override
    public Interaction createInteraction(Interaction interaction) {
        log.info("Creating new interaction of type: {} for user: {}",
                interaction.getType(), interaction.getCreatedById());

        Interaction result = interactionBusiness.createInteraction(interaction);

        if (result == null) {
            // This means a like was removed (dislike)
            log.info("Like removed by user: {}", interaction.getCreatedById());
            return null;
        }

        return result;
    }

    @Override
    public List<Interaction> getInteractionsByEvent(String eventId) {
        log.info("Getting interactions for event: {}", eventId);
        return interactionBusiness.getInteractionsByEvent(eventId);
    }

    @Override
    public List<Interaction> getInteractionsByMessage(String messageId) {
        log.info("Getting interactions for message: {}", messageId);
        return interactionBusiness.getInteractionsByMessage(messageId);
    }

    @Override
    public List<Interaction> getInteractionsByUser(String userId) {
        log.info("Getting interactions for user: {}", userId);
        return interactionBusiness.getInteractionsByUser(userId);
    }

    @GetMapping("/event/{eventId}/likes/count")
    @ResponseStatus(HttpStatus.OK)
    public int getLikeCountForEvent(@PathVariable String eventId) {
        return interactionBusiness.getInteractionsByEvent(eventId).stream()
                .filter(i -> i.getType().name().equals("LIKE"))
                .toArray().length;
    }

    @GetMapping("/message/{messageId}/likes/count")
    @ResponseStatus(HttpStatus.OK)
    public int getLikeCountForMessage(@PathVariable String messageId) {
        return interactionBusiness.getInteractionsByMessage(messageId).stream()
                .filter(i -> i.getType().name().equals("LIKE"))
                .toArray().length;
    }

    @GetMapping("/event/{eventId}/user/{userId}/has-liked")
    @ResponseStatus(HttpStatus.OK)
    public boolean hasUserLikedEvent(@PathVariable String eventId, @PathVariable String userId) {
        return interactionBusiness.hasUserLikedEvent(userId, eventId);
    }

    @GetMapping("/message/{messageId}/user/{userId}/has-liked")
    @ResponseStatus(HttpStatus.OK)
    public boolean hasUserLikedMessage(@PathVariable String messageId, @PathVariable String userId) {
        return interactionBusiness.hasUserLikedMessage(userId, messageId);
    }

    @Override
    public Interaction joinEvent(String eventId, String userId) {
        log.info("User {} joining event {}", userId, eventId);
        return interactionBusiness.joinEvent(eventId, userId);
    }

    @Override
    public Interaction unjoinEvent(String eventId, String userId) {
        log.info("User {} leaving event {}", userId, eventId);
        return interactionBusiness.unjoinEvent(eventId, userId);
    }
}