package cmr.notep.business.services;

import cmr.notep.ressourcesjpa.dao.NotificationEntity;
import cmr.notep.ressourcesjpa.repository.NotificationRepository;
import cmr.notep.ressourcesjpa.repository.AccederRepository;
import cmr.notep.ressourcesjpa.repository.UtilisateursRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final AccederRepository accederRepository;
    private final UtilisateursRepository utilisateursRepository;

    @Transactional
    public void createAccessRequestNotification(String classeId, String className, String studentId, String studentName) {
        // Notify moderators/professors of the class
        List<String> moderatorIds = accederRepository.findModeratorsByClasseId(classeId);
        for (String moderatorId : moderatorIds) {
            saveNotification(moderatorId, "ACCESS_REQUEST", "Nouvelle demande d'accès",
                    studentName + " a demandé l'accès à votre classe " + className,
                    studentId, studentName, classeId, "CLASS");
        }

        // Notify the student (confirmation)
        saveNotification(studentId, "ACCESS_REQUEST", "Demande d'accès envoyée",
                "Votre demande d'accès à la classe " + className + " a été envoyée. En attente de validation.",
                studentId, studentName, classeId, "CLASS");

        // Notify admins
        List<String> adminIds = utilisateursRepository.findAdminUserIds();
        for (String adminId : adminIds) {
            saveNotification(adminId, "ACCESS_REQUEST", "Nouvelle demande d'accès",
                    studentName + " a demandé l'accès à la classe " + className,
                    studentId, studentName, classeId, "CLASS");
        }

        log.info("Access request notifications sent for class {} by student {}", classeId, studentId);
    }

    @Transactional
    public void createAccessApprovedNotification(String classeId, String className, String studentId, String moderatorId, String moderatorName) {
        saveNotification(studentId, "ACCESS_REQUEST", "Demande d'accès approuvée",
                "Votre demande d'accès à la classe " + className + " a été approuvée par " + moderatorName,
                moderatorId, moderatorName, classeId, "CLASS");
        log.info("Access approved notification sent to student {}", studentId);
    }

    @Transactional
    public void createAccessRejectedNotification(String classeId, String className, String studentId, String moderatorName, String motif) {
        String message = "Votre demande d'accès à la classe " + className + " a été rejetée";
        if (motif != null && !motif.isEmpty()) {
            message += ". Motif: " + motif;
        }
        saveNotification(studentId, "ACCESS_REQUEST", "Demande d'accès rejetée",
                message, null, moderatorName, classeId, "CLASS");
        log.info("Access rejected notification sent to student {}", studentId);
    }

    @Transactional
    public void createCourseScheduledNotification(String coursName, String professorId, String professorName, List<String> classeIds) {
        for (String classeId : classeIds) {
            List<String> studentIds = accederRepository.findUserIdsByClasseId(classeId);
            for (String studentId : studentIds) {
                if (!studentId.equals(professorId)) {
                    saveNotification(studentId, "ACTIVITY_CREATED", "Nouveau cours programmé",
                            professorName + " a programmé le cours: " + coursName,
                            professorId, professorName, classeId, "COURSE");
                }
            }
        }
        log.info("Course scheduled notifications sent for {} classes", classeIds.size());
    }

    @Transactional
    public void createExerciseAssignedNotification(String exerciseName, String professorId, String professorName, List<String> classeIds) {
        for (String classeId : classeIds) {
            List<String> studentIds = accederRepository.findUserIdsByClasseId(classeId);
            for (String studentId : studentIds) {
                if (!studentId.equals(professorId)) {
                    saveNotification(studentId, "ASSIGNMENT_GIVEN", "Nouvel exercice assigné",
                            professorName + " a assigné l'exercice: " + exerciseName,
                            professorId, professorName, classeId, "ASSIGNMENT");
                }
            }
        }
        log.info("Exercise assigned notifications sent for {} classes", classeIds.size());
    }
    
    @Transactional
    public void createActivityNotification(String eventId, String eventTitle, String creatorId, String creatorName, List<String> classeIds) {
        if (classeIds != null && !classeIds.isEmpty()) {
            for (String classeId : classeIds) {
                List<String> studentIds = accederRepository.findUserIdsByClasseId(classeId);
                for (String studentId : studentIds) {
                    if (!studentId.equals(creatorId)) {
                        saveNotification(studentId, "ACTIVITY_CREATED", "Nouvelle activité",
                                creatorName + " a créé l'activité: " + eventTitle,
                                creatorId, creatorName, eventId, "EVENT");
                    }
                }
            }
        }

        // Notify admins
        List<String> adminIds = utilisateursRepository.findAdminUserIds();
        for (String adminId : adminIds) {
            if (!adminId.equals(creatorId)) {
                saveNotification(adminId, "ACTIVITY_CREATED", "Nouvelle activité créée",
                        creatorName + " a créé l'activité: " + eventTitle,
                        creatorId, creatorName, eventId, "EVENT");
            }
        }
        log.info("Activity notifications sent for event {}", eventId);
    }
    
    @Transactional
    public void createClassValidationNotification(String classeId, String className, String professorId, String adminId, String adminName) {
        saveNotification(professorId, "CLASS_VALIDATED", "Classe validée",
                "Votre classe '" + className + "' a été validée par " + adminName,
                adminId, adminName, classeId, "CLASS");
        log.info("Class validation notification sent to professor {}", professorId);
    }

    @Transactional
    public void createClassRejectedNotification(String classeId, String className, String professorId, String adminName) {
        saveNotification(professorId, "CLASS_VALIDATED", "Classe rejetée",
                "Votre classe '" + className + "' a été rejetée par " + adminName,
                null, adminName, classeId, "CLASS");
        log.info("Class rejection notification sent to professor {}", professorId);
    }
    
    @Transactional
    public void createAssignmentNotification(String assignmentId, String assignmentTitle, String professorId, String professorName, String classeId) {
        List<String> studentIds = accederRepository.findUserIdsByClasseId(classeId);
        for (String studentId : studentIds) {
            if (!studentId.equals(professorId)) {
                saveNotification(studentId, "ASSIGNMENT_GIVEN", "Nouveau devoir",
                        professorName + " a donné un devoir: " + assignmentTitle,
                        professorId, professorName, assignmentId, "ASSIGNMENT");
            }
        }
        log.info("Assignment notification sent for assignment {} in class {}", assignmentId, classeId);
    }
    
    public List<NotificationEntity> getUserNotifications(String userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
    
    public List<NotificationEntity> getUnreadNotifications(String userId) {
        return notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
    }
    
    public Long getUnreadCount(String userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }
    
    @Transactional
    public NotificationEntity markAsRead(String notificationId) {
        NotificationEntity notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        notification.setRead(true);
        return notificationRepository.save(notification);
    }
    
    @Transactional
    public void markAllAsRead(String userId) {
        List<NotificationEntity> notifications = notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
        notifications.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(notifications);
    }
    
    @Transactional
    public void deleteNotification(String notificationId, String userId) {
        NotificationEntity notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        
        if (!notification.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized to delete this notification");
        }
        
        notificationRepository.delete(notification);
    }
    
    @Transactional
    public void deleteAllNotifications(String userId) {
        notificationRepository.deleteByUserId(userId);
    }

    private void saveNotification(String userId, String type, String title, String message,
                                  String actorId, String actorName, String relatedEntityId, String relatedEntityType) {
        try {
            NotificationEntity notification = new NotificationEntity();
            notification.setId(UUID.randomUUID().toString());
            notification.setUserId(userId);
            notification.setType(type);
            notification.setTitle(title);
            notification.setMessage(message);
            notification.setActorId(actorId);
            notification.setActorName(actorName);
            notification.setRelatedEntityId(relatedEntityId);
            notification.setRelatedEntityType(relatedEntityType);
            notification.setCreatedAt(LocalDateTime.now());
            notification.setRead(false);
            notificationRepository.save(notification);
        } catch (Exception e) {
            log.error("Failed to save notification for user {}: {}", userId, e.getMessage());
        }
    }
}
