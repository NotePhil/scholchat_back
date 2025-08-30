package cmr.notep.ressourcesjpa.dao;

import cmr.notep.modele.EtatCoursProgramme;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "cours_programmer", schema = "ressources")
public class CoursProgrammerEntity {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", nullable = false, updatable = false)
    private String id;

    @Column(name = "date_cours_prevue", nullable = false)
    private LocalDateTime dateCoursPrevue;

    @Column(name = "date_debut_effectif", nullable = false)
    private LocalDateTime dateDebutEffectif;

    @Column(name = "date_fin_effectif", nullable = false)
    private LocalDateTime dateFinEffectif;

    @Enumerated(EnumType.STRING)
    @Column(name = "etat_cours_programme", nullable = false)
    private EtatCoursProgramme etatCoursProgramme;

    @Column(name = "lieu", nullable = false)
    private String lieu;

    @Column(name = "description")
    private String description;

    // SUPPRIMER capacite_max
    // @Column(name = "capacite_max")
    // private Integer capaciteMax;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cours_id", nullable = false)
    private CoursEntity cours;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "professeur_id", nullable = false)
    private ProfesseursEntity professeur;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "cours_programmer_classes",
            schema = "ressources",
            joinColumns = @JoinColumn(name = "cours_programmer_id"),
            inverseJoinColumns = @JoinColumn(name = "classe_id")
    )
    private List<ClassesEntity> classes;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "cours_programmer_participants",
            schema = "ressources",
            joinColumns = @JoinColumn(name = "cours_programmer_id"),
            inverseJoinColumns = @JoinColumn(name = "utilisateur_id")
    )
    private List<UtilisateursEntity> participants;

//    // Audit fields
//    @Column(name = "date_creation")
//    private LocalDateTime dateCreation;
//
//    @Column(name = "date_modification")
//    private LocalDateTime dateModification;
//
//    @Column(name = "cree_par")
//    private String creePar;
//
//    @Column(name = "modifie_par")
//    private String modifiePar;

//    @PrePersist
//    protected void onCreate() {
//        dateCreation = LocalDateTime.now();
//        dateModification = LocalDateTime.now();
//        if (etatCoursProgramme == null) {
//            etatCoursProgramme = EtatCoursProgramme.PLANIFIE;
//        }
//    }
//
//    @PreUpdate
//    protected void onUpdate() {
//        dateModification = LocalDateTime.now();
//    }
}