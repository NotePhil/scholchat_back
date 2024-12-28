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
    public Messages avoirMessage(@NonNull String idMessage) {
        log.info("Récupération du message avec ID: {}", idMessage);
            return messagesBusiness.avoirMessage(idMessage);
    }

    @Override
    public List<Messages> avoirToutMessages() {
        log.info("Récupération de tous les messages");
        return messagesBusiness.avoirToutMessages();
    }

    @Override
    public Messages posterMessage(@NonNull Messages message) {
        log.info("Envoi d'un nouveau message");
        return messagesBusiness.posterMessage(message);
    }
}
