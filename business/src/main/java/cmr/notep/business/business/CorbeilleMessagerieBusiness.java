package cmr.notep.business.business;

import cmr.notep.business.services.MessagePublisher;
import cmr.notep.interfaces.dto.ChatMessageDto;
import cmr.notep.interfaces.dto.ConversationDto;
import cmr.notep.interfaces.dto.CorbeilleDto;
import cmr.notep.ressourcesjpa.dao.UtilisateursEntity;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Corbeille de la messagerie (chat), propre à l'appelant : discussions supprimées pour lui
 * ({@code conversation_participants.deleted_at}) et messages supprimés pour lui ou supprimés pour tous par lui
 * (corbeille historique de {@link MessagesBusiness}). Chaque élément est restaurable ou supprimable
 * définitivement ; « Vider la corbeille » supprime définitivement le tout.
 */
@Component
@Slf4j
@Transactional
public class CorbeilleMessagerieBusiness {

    private final ConversationsBusiness conversationsBusiness;
    private final MessagesBusiness messagesBusiness;
    private final MessagePublisher messagePublisher;

    public CorbeilleMessagerieBusiness(ConversationsBusiness conversationsBusiness, MessagesBusiness messagesBusiness,
                                       MessagePublisher messagePublisher) {
        this.conversationsBusiness = conversationsBusiness;
        this.messagesBusiness = messagesBusiness;
        this.messagePublisher = messagePublisher;
    }

    public CorbeilleDto corbeille() {
        UtilisateursEntity moi = conversationsBusiness.appelant();
        List<ConversationDto> conversations = conversationsBusiness.conversationsCorbeille(moi.getId());
        List<ChatMessageDto> messages = messagesBusiness.corbeilleChat(moi.getId());
        CorbeilleDto dto = new CorbeilleDto();
        dto.setConversations(conversations);
        dto.setMessages(messages);
        dto.setTotal(conversations.size() + (long) messages.size());
        return dto;
    }

    public ConversationDto restaurerConversation(String conversationId) {
        return conversationsBusiness.restaurer(conversationId);
    }

    public void supprimerConversationDefinitivement(String conversationId) {
        UtilisateursEntity moi = conversationsBusiness.appelant();
        LocalDateTime plancher = conversationsBusiness.supprimerDefinitivement(conversationId, moi.getId());
        messagesBusiness.purgerCorbeilleConversation(moi.getId(), conversationId, plancher);
    }

    public void restaurerMessage(String messageId) {
        messagesBusiness.restaurerMessage(messageId);
    }

    public void supprimerMessageDefinitivement(String messageId) {
        messagesBusiness.purgerMessage(messageId);
    }

    /** Permanently deletes every conversation and message of the caller's trash (caller only). */
    public void vider() {
        UtilisateursEntity moi = conversationsBusiness.appelant();
        for (String cid : conversationsBusiness.idsCorbeille(moi.getId())) {
            LocalDateTime plancher = conversationsBusiness.supprimerDefinitivement(cid, moi.getId());
            messagesBusiness.purgerCorbeilleConversation(moi.getId(), cid, plancher);
        }
        messagesBusiness.viderCorbeille();
        try {
            messagePublisher.pushCorbeilleUpdated(moi.getId(), null);
        } catch (Exception e) {
            log.warn("Failed to push trash update: {}", e.getMessage());
        }
    }
}
