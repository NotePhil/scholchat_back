package cmr.notep.business.services;

import cmr.notep.interfaces.modeles.MessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Pushes MessageDto events to connected WebSocket clients in real time.
 *
 * Each user subscribes to /topic/messages/{userId} and receives a push
 * whenever a message is sent to them (or sent by them, so their own sent-box
 * updates without polling).
 *
 * Event shape:
 * {
 *   "type": "NEW_MESSAGE" | "READ_STATUS_CHANGED",
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
     */
    public void pushNewMessage(MessageDto messageDto, String senderId) {
        MessageEvent event = new MessageEvent("NEW_MESSAGE", messageDto);

        // Push to each recipient
        if (messageDto.getDestinataires() != null) {
            messageDto.getDestinataires().forEach(dest -> {
                try {
                    messagingTemplate.convertAndSend(
                            "/topic/messages/" + dest.getId(), event);
                    log.debug("Pushed NEW_MESSAGE to recipient {}", dest.getId());
                } catch (Exception e) {
                    log.warn("Failed to push message to recipient {}: {}", dest.getId(), e.getMessage());
                }
            });
        }

        // Push to sender so their sent-box refreshes
        if (senderId != null) {
            try {
                messagingTemplate.convertAndSend("/topic/messages/" + senderId, event);
                log.debug("Pushed NEW_MESSAGE to sender {}", senderId);
            } catch (Exception e) {
                log.warn("Failed to push message to sender {}: {}", senderId, e.getMessage());
            }
        }
    }

    // ── Inner event wrapper ───────────────────────────────────────────────────

    public record MessageEvent(String type, MessageDto message) {}
}
