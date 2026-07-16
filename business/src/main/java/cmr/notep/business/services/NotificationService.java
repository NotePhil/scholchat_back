package cmr.notep.business.services;

import cmr.notep.ressourcesjpa.dao.EtablissementEntity;
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
    private final NotificationPublisher notificationPublisher;

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
    public void createAssignmentNotification(String assignmentId, String assignmentTitle, String professorId, String professorName, String classId) {
        List<String> studentIds = accederRepository.findUserIdsByClasseId(classId);
        for (String studentId : studentIds) {
            if (!studentId.equals(professorId)) {
                saveNotification(studentId, "ASSIGNMENT_GIVEN", "Nouveau devoir",
                        professorName + " a donné un devoir: " + assignmentTitle,
                        professorId, professorName, assignmentId, "ASSIGNMENT");
            }
        }
        log.info("Assignment notification sent for assignment {} in class {}", assignmentId, classId);
    }

    @Transactional
    public void createClassCreatedNotification(String classeId, String className, String professorId, String professorName) {
        createClassCreatedNotification(classeId, className, professorId, professorName, null);
    }

    @Transactional
    public void createClassCreatedNotification(String classeId, String className, String professorId, String professorName, EtablissementEntity etablissement) {
        // Notify admins
        List<String> adminIds = utilisateursRepository.findAdminUserIds();
        for (String adminId : adminIds) {
            saveNotification(adminId, "CLASS_CREATED", "Nouvelle classe créée",
                    professorName + " a créé une nouvelle classe: " + className,
                    professorId, professorName, classeId, "CLASS");
        }

        // Notify gestionnaire of the establishment if applicable
        if (etablissement != null && etablissement.getGestionnaire() != null) {
            String gestionnaireId = etablissement.getGestionnaire().getId();
            // Avoid duplicate if gestionnaire is also an admin
            if (!adminIds.contains(gestionnaireId)) {
                saveNotification(gestionnaireId, "CLASS_CREATED", "Nouvelle classe dans votre établissement",
                        professorName + " a créé la classe '" + className + "' dans " + etablissement.getNom(),
                        professorId, professorName, classeId, "CLASS");
            }
        }

        log.info("Class created notification sent to admins and gestionnaire for class {}", classeId);
    }

    @Transactional
    public void createCourseCreatedNotification(String coursId, String coursName, String professorId, String professorName, List<String> classeIds) {
        // Notify all students in the related classes
        if (classeIds != null && !classeIds.isEmpty()) {
            for (String classeId : classeIds) {
                List<String> userIds = accederRepository.findUserIdsByClasseId(classeId);
                for (String userId : userIds) {
                    if (!userId.equals(professorId)) {
                        saveNotification(userId, "NEW_COURSE", "Nouveau cours disponible",
                                professorName + " a publié un nouveau cours: " + coursName,
                                professorId, professorName, coursId, "COURSE");
                    }
                }
            }
        }

        // Notify admins
        List<String> adminIds = utilisateursRepository.findAdminUserIds();
        for (String adminId : adminIds) {
            if (!adminId.equals(professorId)) {
                saveNotification(adminId, "NEW_COURSE", "Nouveau cours créé",
                        professorName + " a créé le cours: " + coursName,
                        professorId, professorName, coursId, "COURSE");
            }
        }
        log.info("Course created notifications sent for course {}", coursId);
    }

    @Transactional
    public void createExerciseCreatedNotification(String exerciseId, String exerciseName, String professorId, String professorName) {
        // Notify admins
        List<String> adminIds = utilisateursRepository.findAdminUserIds();
        for (String adminId : adminIds) {
            if (!adminId.equals(professorId)) {
                saveNotification(adminId, "EXERCISE_CREATED", "Nouvel exercice créé",
                        professorName + " a créé l'exercice: " + exerciseName,
                        professorId, professorName, exerciseId, "EXERCISE");
            }
        }
        log.info("Exercise created notification sent to admins for exercise {}", exerciseId);
    }

    @Transactional
    public void createDevoirSoumisNotification(String exerciseProgrammerId, String exerciseName,
                                               String studentId, String studentName, String professorId) {
        // Notify the professor that a student submitted a devoir awaiting correction
        saveNotification(professorId, "DEVOIR_SOUMIS", "Devoir soumis à corriger",
                studentName + " a soumis le devoir \"" + exerciseName + "\" et attend votre correction.",
                studentId, studentName, exerciseProgrammerId, "EXERCISE");
        // Confirm to the student
        saveNotification(studentId, "DEVOIR_SOUMIS", "Devoir soumis avec succès",
                "Votre devoir \"" + exerciseName + "\" a été soumis. Vous recevrez une notification dès que le professeur l'aura corrigé.",
                professorId, null, exerciseProgrammerId, "EXERCISE");
        log.info("Devoir soumis notifications sent: student={}, professor={}", studentId, professorId);
    }

    @Transactional
    public void createCorrectionDisponibleNotification(String exerciseProgrammerId, String exerciseName,
                                                       String studentId, String professorId, String professorName,
                                                       String note) {
        // Notify the student that their devoir has been corrected
        String message = professorName + " a corrigé votre devoir \"" + exerciseName + "\"";
        if (note != null && !note.isBlank()) {
            message += ". Note obtenue : " + note;
        }
        saveNotification(studentId, "CORRECTION_DISPONIBLE", "Correction disponible",
                message, professorId, professorName, exerciseProgrammerId, "EXERCISE");
        log.info("Correction disponible notification sent to student {}", studentId);
    }

    @Transactional
    public void createClassMemberNotification(String classeId, String className, String actionType, String title, String message, String actorId, String actorName) {
        // Notify all members of the class
        List<String> userIds = accederRepository.findUserIdsByClasseId(classeId);
        for (String userId : userIds) {
            if (!userId.equals(actorId)) {
                saveNotification(userId, actionType, title, message,
                        actorId, actorName, classeId, "CLASS");
            }
        }
        log.info("{} notification sent to {} class members for class {}", actionType, userIds.size(), classeId);
    }

    @Transactional
    public void createClasseDemandeAdhesionNotification(String classeId, String classeNom,
                                                        String professorId, String professorNom,
                                                        String etablissementNom, String gestionnaireId) {
        // Notify the gestionnaire: new pending request
        if (gestionnaireId != null) {
            saveNotification(gestionnaireId, "CLASSE_ADHESION_DEMANDE",
                    "Demande d'adhésion à votre établissement",
                    professorNom + " demande que la classe '" + classeNom + "' rejoigne l'établissement '" + etablissementNom + "'",
                    professorId, professorNom, classeId, "CLASS");
        }
        // Notify the professor: confirmation
        if (professorId != null) {
            saveNotification(professorId, "CLASSE_ADHESION_DEMANDE",
                    "Demande d'adhésion envoyée",
                    "Votre demande d'adhésion de la classe '" + classeNom + "' à l'établissement '" + etablissementNom + "' est en attente de validation.",
                    null, null, classeId, "CLASS");
        }
        log.info("Adhesion demand notifications sent for class {} to etablissement {}", classeId, etablissementNom);
    }

    @Transactional
    public void createEtablissementCreatedNotification(String etablissementId, String etablissementNom, String gestionnaireId, String gestionnaireNom) {
        saveNotification(gestionnaireId, "ETABLISSEMENT_CREATED", "Vous êtes gestionnaire d'un établissement",
                "Vous avez été ajouté comme gestionnaire de l'établissement '" + etablissementNom + "'",
                null, null, etablissementId, "ETABLISSEMENT");
        log.info("Etablissement created notification sent to gestionnaire {}", gestionnaireId);
    }

    @Transactional
    public void createProfessorCreatedNotification(String professorId, String professorName) {
        // Notify admins
        List<String> adminIds = utilisateursRepository.findAdminUserIds();
        for (String adminId : adminIds) {
            saveNotification(adminId, "PROFESSOR_CREATED", "Nouveau professeur",
                    "Un nouveau professeur s'est inscrit: " + professorName,
                    professorId, professorName, professorId, "PROFESSOR");
        }
        log.info("Professor created notification sent to admins for {}", professorId);
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

    @Transactional
    public void createMessageNotification(String recipientId, String senderId, String senderName, String messageSubject) {
        saveNotification(recipientId, "MESSAGE_SENT", "Nouveau message",
                senderName + " vous a envoye un message: " + (messageSubject != null ? messageSubject : "Sans objet"),
                senderId, senderName, null, "MESSAGE");
        log.info("Message notification sent to {} from {}", recipientId, senderName);
    }

    @Transactional
    public void createOffreExpirationBientotNotification(String userId, String nomEntite, String entiteId, String entiteType) {
        saveNotification(userId, "OFFRE_EXPIRATION_BIENTOT", "Offre bientôt expirée",
                "L'offre de \"" + nomEntite + "\" arrive bientôt à expiration. Pensez à la renouveler.",
                null, null, entiteId, entiteType);
    }

    @Transactional
    public void createOffreExpireeNotification(String userId, String nomEntite, String entiteId, String entiteType) {
        saveNotification(userId, "OFFRE_EXPIREE", "Offre expirée",
                "L'offre de \"" + nomEntite + "\" a expiré. Renouvelez-la pour réactiver l'accès.",
                null, null, entiteId, entiteType);
    }

    @Transactional
    public void createSuppressionImminenteNotification(String userId, String nomEntite, String entiteId, String entiteType) {
        saveNotification(userId, "SUPPRESSION_IMMINENTE", "Suppression imminente",
                "\"" + nomEntite + "\" sera définitivement supprimé(e) si l'offre n'est pas renouvelée rapidement.",
                null, null, entiteId, entiteType);
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
            notificationPublisher.push(notification);
        } catch (Exception e) {
            log.error("Failed to save notification for user {}: {}", userId, e.getMessage());
        }
    }
}
