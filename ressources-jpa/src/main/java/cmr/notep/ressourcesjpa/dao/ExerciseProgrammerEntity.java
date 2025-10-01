package cmr.notep.ressourcesjpa.dao;

import cmr.notep.modele.EtatExercise;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "exercises_programmer", schema = "ressources")
@PrimaryKeyJoinColumn(name = "exercise_id")
public class ExerciseProgrammerEntity extends ExerciseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exercise_id", insertable = false, updatable = false)
    private ExerciseEntity exercise;

    @Column(name = "date_exo_prevue", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateExoPrevue;

    @Column(name = "date_debut_exo_effectif", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateDebutExoEffectif;

    @Column(name = "date_fin_exo_effectif", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateFinExoEffectif;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "programme_par_id", nullable = false)
    private ProfesseursEntity programmePar;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "exercise_programmer_classes", schema = "ressources",
            joinColumns = @JoinColumn(name = "exercise_programmer_id"),
            inverseJoinColumns = @JoinColumn(name = "classe_id"))
    private List<ClassesEntity> classesDiffusees = new ArrayList<>();

    @OneToMany(mappedBy = "exerciseProgrammer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ParticiperExoEntity> participants = new ArrayList<>();
}