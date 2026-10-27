package cmr.notep.business.services;

import cmr.notep.interfaces.dto.ChatMessageDto;
import cmr.notep.interfaces.modeles.MessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Objects;

/**
 * Pushes MessageDto events to connected WebSocket clients in real time.
 *
 * Each user subscribes to /topic/messages/{userId} and receives a push
 * whenever a message is sent to them (or sent by them, so their own sent-box
 * updates without polling).
 *
 * Event shape:
 * {
 *   "type": "NEW_MESSAGE" | "MESSAGE_DELETED" | "MESSAGE_RESTORED" | "CONVERSATION_READ"
 *         | "CONVERSATION_UPDATED" (favori, non lu, muet, corbeille : à l'utilisateur seulement)
 *         | "CORBEILLE_UPDATED" (corbeille vidée / élément supprimé définitivement),
 *   "message": { ...MessageDto fields (legacy clients)... },
 *   "conversationId": "...",            // chat conversation of the message (when known)
 *   "chat": { ...ChatMessageDto... },   // NEW_MESSAGE of a conversation
 *   "lecteurId": "...", "luJusqua": "ISO",  // CONVERSATION_READ
 *   "pourTous": true|false              // MESSAGE_DELETED
 * }
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MessagePublisher {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Push a new message to every recipient AND to the sender so both sides
     * update their inbox/sent-box in real time without polling.
     * Recipients receive it with lu=false, the sender with lu=true.
     */
    public void pushNewMessage(MessageDto messageDto, String senderId) {
        if (messageDto.getDestinataires() != null) {
            // convertAndSend serializes immediately, so toggling lu between sends is safe
            messageDto.setLu(false);
            messageDto.getDestinataires().stream()
                    .map(dest -> dest.getId())
                    .filter(id -> id != null && !id.equals(senderId))
                    .distinct()
                    .forEach(id -> send(id, new MessageEvent("NEW_MESSAGE", messageDto)));
        }
        if (senderId != null) {
            messageDto.setLu(true);
            send(senderId, new MessageEvent("NEW_MESSAGE", messageDto));
        }
    }

    /**
     * New message of a conversation, pushed to every participant (sender included, for their other devices).
     * {@code legacy} keeps old clients working (lu=false for recipients, true for the sender).
     */
    public void pushChatMessage(MessageDto legacy, ChatMessageDto chat, Collection<String> participantIds, String senderId) {
        participantIds.stream().filter(Objects::nonNull).distinct().forEach(id -> {
            boolean moi = id.equals(senderId);
            legacy.setLu(moi);
            send(id, new MessageEvent("NEW_MESSAGE", legacy, chat.getConversationId(), chat, null, null, null));
        });
    }

    /** The message disappeared for these users (deleted for everyone, or for the caller on another device). */
    public void pushDeleted(String messageId, Collection<String> userIds) {
        pushDeleted(messageId, null, false, userIds);
    }

    public void pushDeleted(String messageId, String conversationId, boolean pourTous, Collection<String> userIds) {
        MessageDto dto = new MessageDto();
        dto.setId(messageId);
        dto.setConversationId(conversationId);
        userIds.stream().filter(Objects::nonNull).distinct()
                .forEach(id -> send(id, new MessageEvent("MESSAGE_DELETED", dto, conversationId, null, null, null, pourTous)));
    }

    /** Read receipt: {@code lecteurId} has read the conversation up to {@code luJusqua} (ISO). */
    public void pushConversationRead(String conversationId, String lecteurId, String luJusqua, Collection<String> userIds) {
        userIds.stream().filter(Objects::nonNull).distinct()
                .forEach(id -> send(id, new MessageEvent("CONVERSATION_READ", null, conversationId, null, lecteurId, luJusqua, null)));
    }

    /** The message is visible again for this user (restored from trash). */
    public void pushRestored(MessageDto messageDto, String userId) {
        if (userId != null) {
            send(userId, new MessageEvent("MESSAGE_RESTORED", messageDto));
        }
    }

    /**
     * The caller's own view of a conversation changed (favourite, marked unread, muted, moved to / restored
     * from the trash, permanently deleted): their other tabs and devices reload the list and the trash.
     */
    public void pushConversationUpdated(String conversationId, String userId) {
        if (userId != null) {
            send(userId, new MessageEvent("CONVERSATION_UPDATED", null, conversationId, null, null, null, null));
        }
    }

    /** The caller's trash changed without a conversation (emptied, message permanently deleted). */
    public void pushCorbeilleUpdated(String userId, String conversationId) {
        if (userId != null) {
            send(userId, new MessageEvent("CORBEILLE_UPDATED", null, conversationId, null, null, null, null));
        }
    }

    private void send(String userId, MessageEvent event) {
        try {
            messagingTemplate.convertAndSend("/topic/messages/" + userId, event);
            log.debug("Pushed {} to {}", event.type(), userId);
        } catch (Exception e) {
            log.warn("Failed to push {} to {}: {}", event.type(), userId, e.getMessage());
        }
    }

    // ── Inner event wrapper ───────────────────────────────────────────────────

    public record MessageEvent(String type, MessageDto message, String conversationId, ChatMessageDto chat,
                               String lecteurId, String luJusqua, Boolean pourTous) {
        public MessageEvent(String type, MessageDto message) {
            this(type, message, message != null ? message.getConversationId() : null, null, null, null, null);
        }
    }
}
