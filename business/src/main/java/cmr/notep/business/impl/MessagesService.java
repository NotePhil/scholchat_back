package cmr.notep.business.impl;

import cmr.notep.business.business.MessagesBusiness;
import cmr.notep.interfaces.api.MessagesApi;
import cmr.notep.interfaces.modeles.Messages;
import lombok.NonNull;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
@RestController
public class MessagesService implements MessagesApi {
    private final MessagesBusiness messagesBusiness;

    public MessagesService(MessagesBusiness messagesBusiness) {
        this.messagesBusiness = messagesBusiness;
    }

    @Override
    public Messages avoirMessage(@NonNull String idMessage) {
        return messagesBusiness.avoirMessage(idMessage);
    }

    @Override
    public List<Messages> avoirToutMessages() {
        return messagesBusiness.avoirToutMessages();
    }

    @Override
    public Messages posterMessage(@NonNull Messages message) {
        return messagesBusiness.posterMessage(message);
    }
}