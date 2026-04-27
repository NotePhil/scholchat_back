package cmr.notep.business.services;

import cmr.notep.ressourcesjpa.dao.NotificationEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    public void push(NotificationEntity notification) {
        try {
            messagingTemplate.convertAndSend(
                    "/topic/notifications/" + notification.getUserId(),
                    notification
            );
            log.debug("Pushed notification {} to user {}", notification.getId(), notification.getUserId());
        } catch (Exception e) {
            log.warn("Failed to push notification via WebSocket for user {}: {}", notification.getUserId(), e.getMessage());
        }
    }
}
