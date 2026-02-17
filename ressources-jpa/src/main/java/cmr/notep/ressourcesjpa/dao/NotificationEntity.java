package cmr.notep.ressourcesjpa.dao;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications", schema = "ressources")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationEntity {
    
    @Id
    @Column(name = "id")
    private String id;
    
    @Column(name = "user_id", nullable = false)
    private String userId;
    
    @Column(name = "type", nullable = false)
    private String type;
    
    @Column(name = "title", nullable = false)
    private String title;
    
    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;
    
    @Column(name = "actor_id")
    private String actorId;
    
    @Column(name = "actor_name")
    private String actorName;
    
    @Column(name = "related_entity_id")
    private String relatedEntityId;
    
    @Column(name = "related_entity_type")
    private String relatedEntityType;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "is_read", nullable = false)
    private boolean isRead;
}
