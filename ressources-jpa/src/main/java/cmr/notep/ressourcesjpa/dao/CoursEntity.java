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

    @Column(name = "restriction")
    private String restriction;

    @Column(name = "contenu", columnDefinition = "TEXT")
    private String contenu;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "redacteur_id", nullable = false)
    private ProfesseursEntity redacteur;

    @OneToMany(mappedBy = "cours", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ChapitreEntity> chapitres = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "cours_matiere", schema = "ressources",
            joinColumns = @JoinColumn(name = "cours_id"),
            inverseJoinColumns = @JoinColumn(name = "matiere_id"))
    @Mapping("matieres")
    private List<MatiereEntity> matieres = new ArrayList<>();
}