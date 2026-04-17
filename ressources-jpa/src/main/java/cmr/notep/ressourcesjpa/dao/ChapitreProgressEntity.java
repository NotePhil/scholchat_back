package cmr.notep.ressourcesjpa.dao;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "chapitre_progress", schema = "ressources",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "chapitre_id"}))
public class ChapitreProgressEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private String id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "chapitre_id", nullable = false)
    private String chapitreId;

    @Column(name = "cours_id", nullable = false)
    private String coursId;

    @Column(name = "completed", nullable = false)
    private boolean completed;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;
}
