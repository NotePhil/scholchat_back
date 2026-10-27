package cmr.notep.business.impl;

import cmr.notep.business.business.ConversationsBusiness;
import cmr.notep.business.business.CorbeilleMessagerieBusiness;
import cmr.notep.business.business.MessagesBusiness;
import cmr.notep.interfaces.api.ConversationsApi;
import cmr.notep.interfaces.dto.ChatMessageDto;
import cmr.notep.interfaces.dto.ChatMessagePageDto;
import cmr.notep.interfaces.dto.ConversationDto;
import cmr.notep.interfaces.dto.CorbeilleDto;
import cmr.notep.interfaces.dto.CreerConversationRequest;
import cmr.notep.interfaces.dto.EnvoyerMessageChatRequest;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class ConversationsService implements ConversationsApi {

    private final ConversationsBusiness conversationsBusiness;
    private final MessagesBusiness messagesBusiness;
    private final CorbeilleMessagerieBusiness corbeilleBusiness;

    public ConversationsService(ConversationsBusiness conversationsBusiness, MessagesBusiness messagesBusiness,
                                CorbeilleMessagerieBusiness corbeilleBusiness) {
        this.conversationsBusiness = conversationsBusiness;
        this.messagesBusiness = messagesBusiness;
        this.corbeilleBusiness = corbeilleBusiness;
    }

    @Override
    public CorbeilleDto corbeille() {
        return corbeilleBusiness.corbeille();
    }

    @Override
    public void viderCorbeille() {
        corbeilleBusiness.vider();
    }

    @Override
    public void restaurerMessage(String messageId) {
        corbeilleBusiness.restaurerMessage(messageId);
    }

    @Override
    public void supprimerMessageDefinitivement(String messageId) {
        corbeilleBusiness.supprimerMessageDefinitivement(messageId);
    }

    @Override
    public ConversationDto restaurerConversation(String id) {
        return corbeilleBusiness.restaurerConversation(id);
    }

    @Override
    public void supprimerConversationDefinitivement(String id) {
        corbeilleBusiness.supprimerConversationDefinitivement(id);
    }

    @Override
    public ConversationDto definirFavori(String id, boolean favori) {
        return conversationsBusiness.definirFavori(id, favori);
    }

    @Override
    public ConversationDto marquerNonLue(String id) {
        return conversationsBusiness.marquerNonLue(id);
    }

    @Override
    public List<ConversationDto> mesConversations() {
        return conversationsBusiness.mesConversations();
    }

    @Override
    public ConversationDto creerConversation(CreerConversationRequest request) {
        return messagesBusiness.creerConversation(request);
    }

    @Override
    public Map<String, Long> compterNonLus() {
        return conversationsBusiness.compterNonLus();
    }

    @Override
    public ConversationDto conversationDuMessage(String messageId) {
        return conversationsBusiness.parMessage(messageId);
    }

    @Override
    public ConversationDto obtenirConversation(String id) {
        return conversationsBusiness.obtenir(id);
    }

    @Override
    public ChatMessagePageDto messages(String id, String before, int size) {
        return conversationsBusiness.messagesDe(id, before, size);
    }

    @Override
    public ChatMessageDto envoyer(String id, EnvoyerMessageChatRequest request) {
        return messagesBusiness.envoyerDansConversation(id, request);
    }

    @Override
    public Map<String, String> marquerLue(String id) {
        return conversationsBusiness.marquerLue(id);
    }

    @Override
    public void supprimerMessage(String id, String messageId, boolean pourTous) {
        messagesBusiness.supprimerDansConversation(id, messageId, pourTous);
    }

    @Override
    public void masquerConversation(String id) {
        conversationsBusiness.masquer(id);
    }

    @Override
    public ConversationDto definirMuet(String id, boolean muted) {
        return conversationsBusiness.definirMuet(id, muted);
    }
}
