package cmr.notep.ressourcesjpa.dao;

import cmr.notep.modele.SessionMode;
import cmr.notep.modele.SessionStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "cours_sessions", schema = "ressources")
public class CoursSessionEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private String id;

    @Column(name = "cours_id", nullable = false)
    private String coursId;

    @Column(name = "room_name", nullable = false, unique = true)
    private String roomName;

    @Enumerated(EnumType.STRING)
    @Column(name = "mode", nullable = false)
    private SessionMode mode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SessionStatus status;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Column(name = "started_by_user_id", nullable = false)
    private String startedByUserId;

    @Column(name = "current_chapitre_id")
    private String currentChapitreId;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "cours_session_participants",
            schema = "ressources",
            joinColumns = @JoinColumn(name = "session_id")
    )
    @Column(name = "user_id")
    private List<String> participantIds = new ArrayList<>();
}
