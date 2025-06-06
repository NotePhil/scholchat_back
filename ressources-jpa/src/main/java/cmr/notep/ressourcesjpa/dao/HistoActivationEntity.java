package cmr.notep.ressourcesjpa.dao;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "histo_activation", schema = "ressources")
public class HistoActivationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    @JoinColumn(name = "classe_id", nullable = false)
    private ClassesEntity classe;

    @ManyToOne
    @JoinColumn(name = "professeur_id", nullable = false)
    private ProfesseursEntity professeur;

    @Column(name = "date_activation", nullable = false)
    private LocalDateTime dateActivation;

    @Column(name = "date_desactivation")
    private LocalDateTime dateDesactivation;

    @Column(name = "motif_desactivation")
    private String motifDesactivation;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;
}