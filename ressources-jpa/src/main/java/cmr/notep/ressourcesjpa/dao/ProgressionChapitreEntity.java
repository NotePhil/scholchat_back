package cmr.notep.ressourcesjpa.dao;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "progression_chapitre", schema = "ressources")
public class ProgressionChapitreEntity {

    @EmbeddedId
    private ProgressionChapitreId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("utilisateurId")
    @JoinColumn(name = "utilisateur_id")
    private UtilisateursEntity utilisateur;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("chapitreId")
    @JoinColumn(name = "chapitre_id")
    private ChapitreEntity chapitre;

    @Column(name = "date_completion", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateCompletion;
}
