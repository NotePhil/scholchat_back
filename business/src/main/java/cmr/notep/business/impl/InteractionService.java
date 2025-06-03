package cmr.notep.business.impl;

import cmr.notep.business.business.InteractionBusiness;
import cmr.notep.interfaces.api.InteractionApi;
import cmr.notep.interfaces.modeles.Interaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
public class InteractionService implements InteractionApi {
    private final InteractionBusiness interactionBusiness;

    @Override
    public Interaction createInteraction(Interaction interaction) {
        log.info("Creating new interaction of type: {}", interaction.getType());
        return interactionBusiness.createInteraction(interaction);
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
}