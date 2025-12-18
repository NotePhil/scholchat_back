package cmr.notep.business.impl;

import cmr.notep.business.business.MessagesBusiness;
import cmr.notep.interfaces.dto.GroupMessageDto;
import cmr.notep.interfaces.api.MessagesApi;
import cmr.notep.interfaces.modeles.MessageDto;
import cmr.notep.interfaces.modeles.Messages;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;
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

    @Override
    public Messages posterMessageGroupe(@NonNull @RequestBody GroupMessageDto groupMessageDto) {
        log.info("Envoi d'un message de groupe aux classes {} avec {} copie(s)",
                groupMessageDto.getClassIds(),
                groupMessageDto.getCopieRecipientIds() != null ? groupMessageDto.getCopieRecipientIds().size() : 0);
        return messagesBusiness.posterMessageGroupe(groupMessageDto);
    }

    @Override
    public List<Messages> obtenirMessagesParUtilisateur(String utilisateurId) {
        log.info("Obtenir tous les messages pour l'utilisateur {}", utilisateurId);
        return messagesBusiness.obtenirMessagesParUtilisateur(utilisateurId);
    }

    @Override
    public List<MessageDto> obtenirMessagesEnvoyes(String utilisateurId) {
        log.info("Obtenir les messages envoyés par l'utilisateur {}", utilisateurId);
        return messagesBusiness.obtenirMessagesEnvoyes(utilisateurId);
    }

    @Override
    public List<MessageDto> obtenirMessagesRecus(String utilisateurId) {
        log.info("Obtenir les messages reçus par l'utilisateur {}", utilisateurId);
        return messagesBusiness.obtenirMessagesRecus(utilisateurId);
    }
    
    @Override
    public void supprimerMessage(String messageId) {
        log.info("Suppression du message avec ID: {}", messageId);
        messagesBusiness.supprimerMessage(messageId);
    }
    
    @Override
    public List<MessageDto> obtenirMessagesCorbeille(String utilisateurId) {
        log.info("Obtenir les messages dans la corbeille pour l'utilisateur {}", utilisateurId);
        return messagesBusiness.obtenirMessagesCorbeille(utilisateurId);
    }
    
    @Override
    public void viderCorbeille() {
        log.info("Vider la corbeille - suppression définitive des anciens messages");
        messagesBusiness.viderCorbeille();
    }
    
    @Override
    public void restaurerMessage(String messageId) {
        log.info("Restauration du message avec ID: {}", messageId);
        messagesBusiness.restaurerMessage(messageId);
    }

}