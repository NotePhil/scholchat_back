package cmr.notep.business.services;

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
 *   "type": "NEW_MESSAGE" | "MESSAGE_DELETED" | "MESSAGE_RESTORED",
 *   "message": { ...MessageDto fields... }
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

    /** The message disappeared for these users (deleted for everyone, or for the caller on another device). */
    public void pushDeleted(String messageId, Collection<String> userIds) {
        MessageDto dto = new MessageDto();
        dto.setId(messageId);
        userIds.stream().filter(Objects::nonNull).distinct()
                .forEach(id -> send(id, new MessageEvent("MESSAGE_DELETED", dto)));
    }

    /** The message is visible again for this user (restored from trash). */
    public void pushRestored(MessageDto messageDto, String userId) {
        if (userId != null) {
            send(userId, new MessageEvent("MESSAGE_RESTORED", messageDto));
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

    public record MessageEvent(String type, MessageDto message) {}
}
