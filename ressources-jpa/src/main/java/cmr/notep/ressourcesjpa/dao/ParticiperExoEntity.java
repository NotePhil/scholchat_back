package cmr.notep.ressourcesjpa.dao;

import cmr.notep.modele.EtatSoumission;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "participer_exo", schema = "ressources")
public class ParticiperExoEntity {

    @EmbeddedId
    private ParticiperExoId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("utilisateurId")
    @JoinColumn(name = "utilisateur_id")
    private UtilisateursEntity utilisateur;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("exerciseProgrammerId")
    @JoinColumn(name = "exercise_programmer_id")
    private ExerciseProgrammerEntity exerciseProgrammer;

    @Column(name = "note")
    private String note;

    @Column(name = "appreciation")
    private String appreciation;

    @Enumerated(EnumType.STRING)
    @Column(name = "etat_soumission", nullable = false)
    private EtatSoumission etatSoumission = EtatSoumission.EN_COURS;

    @Column(name = "date_debut", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateDebut;

    @Column(name = "date_fin")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateFin;

    @Column(name = "date_soumission")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateSoumission;
}
