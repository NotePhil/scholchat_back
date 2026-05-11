package cmr.notep.ressourcesjpa.dao;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "session_attendance", schema = "ressources")
@Data
@NoArgsConstructor
public class SessionAttendanceEntity {
    
    @Id
    private String id;
    
    @Column(name = "session_id", nullable = false)
    private String sessionId;
    
    @Column(name = "user_id", nullable = false)
    private String userId;
    
    @Column(name = "cours_id", nullable = false)
    private String coursId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AttendanceStatus status;
    
    @Column(name = "joined_at")
    private LocalDateTime joinedAt;
    
    @Column(name = "left_at")
    private LocalDateTime leftAt;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    public enum AttendanceStatus {
        EXPECTED,    // User was expected to attend (all class students when no specific participants)
        JOINED,      // User actually joined the session
        LEFT,        // User left the session
        COMPLETED    // User completed the session
    }
}