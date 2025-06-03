package cmr.notep.ressourcesjpa.dao;

import cmr.notep.modele.InteractionType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "interactions", schema = "ressources")
public class InteractionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InteractionType type;

    @Column(nullable = false)
    private String content;

    @Column(name = "creation_date", nullable = false)
    private LocalDateTime creationDate = LocalDateTime.now();

    @Column
    private String niveau;

    @ManyToOne
    @JoinColumn(name = "created_by", nullable = false)
    private UtilisateursEntity createdBy;

    @ManyToOne
    @JoinColumn(name = "event_id")
    private EvenementEntity event;

    @ManyToOne
    @JoinColumn(name = "message_id")
    private MessagesEntity message;
}