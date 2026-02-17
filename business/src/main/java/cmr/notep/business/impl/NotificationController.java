package cmr.notep.business.impl;

import cmr.notep.business.services.NotificationService;
import cmr.notep.interfaces.api.NotificationApi;
import cmr.notep.interfaces.modeles.Notification;
import cmr.notep.ressourcesjpa.dao.NotificationEntity;
import cmr.notep.ressourcesjpa.dao.UtilisateursEntity;
import cmr.notep.ressourcesjpa.repository.UtilisateursRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequiredArgsConstructor
public class NotificationController implements NotificationApi {

    private final NotificationService notificationService;
    private final UtilisateursRepository utilisateursRepository;
    
    @Override
    public List<Notification> getUserNotifications() {
        String userId = getCurrentUserId();
        log.info("Getting notifications for user: {}", userId);
        
        List<NotificationEntity> entities = notificationService.getUserNotifications(userId);
        return entities.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Notification> getUnreadNotifications() {
        String userId = getCurrentUserId();
        log.info("Getting unread notifications for user: {}", userId);
        
        List<NotificationEntity> entities = notificationService.getUnreadNotifications(userId);
        return entities.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    @Override
    public Long getUnreadCount() {
        String userId = getCurrentUserId();
        return notificationService.getUnreadCount(userId);
    }
    
    @Override
    public Notification markAsRead(String id) {
        log.info("Marking notification as read: {}", id);
        NotificationEntity entity = notificationService.markAsRead(id);
        return convertToDto(entity);
    }
    
    @Override
    public void markAllAsRead() {
        String userId = getCurrentUserId();
        log.info("Marking all notifications as read for user: {}", userId);
        notificationService.markAllAsRead(userId);
    }
    
    @Override
    public void deleteNotification(String id) {
        String userId = getCurrentUserId();
        log.info("Deleting notification: {} for user: {}", id, userId);
        notificationService.deleteNotification(id, userId);
    }
    
    @Override
    public void deleteAllNotifications() {
        String userId = getCurrentUserId();
        log.info("Deleting all notifications for user: {}", userId);
        notificationService.deleteAllNotifications(userId);
    }
    
    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String email = authentication.getName();
            // Resolve email to actual user ID since JWT stores email as subject
            UtilisateursEntity user = utilisateursRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
            return user.getId();
        }
        throw new RuntimeException("User not authenticated");
    }
    
    private Notification convertToDto(NotificationEntity entity) {
        Notification dto = new Notification();
        dto.setId(entity.getId());
        dto.setUserId(entity.getUserId());
        dto.setType(entity.getType());
        dto.setTitle(entity.getTitle());
        dto.setMessage(entity.getMessage());
        dto.setActorId(entity.getActorId());
        dto.setActorName(entity.getActorName());
        dto.setRelatedEntityId(entity.getRelatedEntityId());
        dto.setRelatedEntityType(entity.getRelatedEntityType());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setRead(entity.isRead());
        return dto;
    }
}
