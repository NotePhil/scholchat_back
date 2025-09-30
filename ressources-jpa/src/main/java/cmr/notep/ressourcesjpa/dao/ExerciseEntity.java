package cmr.notep.ressourcesjpa.dao;

import cmr.notep.modele.EtatExercise;
import cmr.notep.modele.EtatX;
import cmr.notep.modele.ListeNiveau;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "exercises", schema = "ressources")
@Inheritance(strategy = InheritanceType.JOINED)
public class ExerciseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String nom;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateCreation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EtatExercise etat;

    @Column(nullable = false)
    private String restriction; // PUBLIC/PRIVE

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ListeNiveau niveau;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "redacteur_id", nullable = false)
    private ProfesseursEntity redacteur;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "exercise_matieres", schema = "ressources",
            joinColumns = @JoinColumn(name = "exercise_id"),
            inverseJoinColumns = @JoinColumn(name = "matiere_id"))
    private List<MatiereEntity> matieres = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "cours_exercises", schema = "ressources",
            joinColumns = @JoinColumn(name = "exercise_id"),
            inverseJoinColumns = @JoinColumn(name = "cours_id"))
    private List<CoursEntity> coursLies = new ArrayList<>();

    @OneToMany(mappedBy = "exercise", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuestionReponseEntity> questions = new ArrayList<>();

    // Add relationship with ExerciseProgrammerEntity
    @OneToMany(mappedBy = "exercise", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExerciseProgrammerEntity> exercisesProgrammes = new ArrayList<>();
}