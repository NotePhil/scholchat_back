package cmr.notep.interfaces.modeles;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Notification {
    private String id;
    private String userId;
    private String type; // ACCESS_REQUEST, ACTIVITY_CREATED, CLASS_VALIDATED, ASSIGNMENT_GIVEN, etc.
    private String title;
    private String message;
    private String actorId;
    private String actorName;
    private String relatedEntityId; // ID of class, event, message, etc.
    private String relatedEntityType; // CLASS, EVENT, MESSAGE, etc.
    private LocalDateTime createdAt;
    private boolean isRead;
}
