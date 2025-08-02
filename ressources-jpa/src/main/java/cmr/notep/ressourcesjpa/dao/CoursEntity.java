package cmr.notep.ressourcesjpa.dao;

import cmr.notep.modele.EtatCours;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.dozer.Mapping;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "cours", schema = "ressources")
public class CoursEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String titre;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateCreation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EtatCours etat;

    @Column(columnDefinition = "TEXT")
    private String references;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String contenu;

    @ManyToOne
    @JoinColumn(name = "redacteur_id", nullable = false)
    @Mapping("redacteur")
    private ProfesseursEntity redacteur;

    @ManyToMany
    @JoinTable(name = "cours_matiere", schema = "ressources",
            joinColumns = @JoinColumn(name = "cours_id"),
            inverseJoinColumns = @JoinColumn(name = "matiere_id"))
    @Mapping("matieres")
    private List<MatiereEntity> matieres = new ArrayList<>();
}