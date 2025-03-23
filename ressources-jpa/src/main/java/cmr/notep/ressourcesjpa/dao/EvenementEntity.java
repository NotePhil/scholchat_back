package cmr.notep.ressourcesjpa.dao;

import cmr.notep.modele.EtatEvenement;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "evenements", schema = "ressources")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EvenementEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String titre;

    private String description;
    private String lieu;

    @Column(name = "heure_debut", nullable = false)
    private LocalDateTime heureDebut;

    @Column(name = "heure_fin")
    private LocalDateTime heureFin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EtatEvenement etat;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "canal_id", nullable = false)
    private CanalEntity canal;
}