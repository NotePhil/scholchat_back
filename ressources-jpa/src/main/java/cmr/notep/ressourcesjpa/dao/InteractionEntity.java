package cmr.notep.ressourcesjpa.dao;

import cmr.notep.modele.TypeInteraction;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "interactions", schema = "ressources")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InteractionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeInteraction type;

    @Column(nullable = false)
    private String contenu;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation;

    private String niveau;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evenement_id", nullable = false)
    private EvenementEntity evenement;
}