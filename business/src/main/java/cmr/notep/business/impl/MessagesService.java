package cmr.notep.business.impl;

import cmr.notep.business.business.MessagesBusiness;
import cmr.notep.interfaces.api.MessagesApi;
import cmr.notep.interfaces.modeles.Messages;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
public class MessagesService implements MessagesApi {
    private final MessagesBusiness messagesBusiness;

    public MessagesService(MessagesBusiness messagesBusiness) {
        this.messagesBusiness = messagesBusiness;
    }

    @Override
    public Messages getMessage(@NonNull String idMessage) {
        log.info("Fetching message with ID: {}", idMessage);
        return messagesBusiness.getMessage(idMessage);
    }

    @Override
    public List<Messages> getAllMessages() {
        log.info("Fetching all messages");
        return messagesBusiness.getAllMessages();
    }

    @Override
    public Messages sendIndividualMessage(@NonNull Messages message) {
        log.info("Sending individual message");
        return messagesBusiness.sendIndividualMessage(message);
    }

    @Override
    public Messages sendClassGroupMessage(@NonNull Messages message) {
        log.info("Sending class group message");
        return messagesBusiness.sendClassGroupMessage(message);
    }
}