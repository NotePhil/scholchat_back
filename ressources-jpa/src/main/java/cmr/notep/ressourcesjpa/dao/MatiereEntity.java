package cmr.notep.ressourcesjpa.dao;

import cmr.notep.modele.EtatMatieres;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "matieres", schema = "ressources")
public class MatiereEntity {
    @Id
    private String id;

    @Column(nullable = false, unique = true)
    private String nom;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private LocalDateTime dateCreation = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EtatMatieres etat;

    @ManyToMany
    @JoinTable(
            name = "classe_matieres",
            schema = "ressources",
            joinColumns = @JoinColumn(name = "matiere_id"),
            inverseJoinColumns = @JoinColumn(name = "classe_id")
    )
    private List<ClassesEntity> classes = new ArrayList<>();

    @ManyToMany(mappedBy = "matieresEnseignees")
    private List<ProfesseursEntity> professeurs = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "exercise_matieres",
            schema = "ressources",
            joinColumns = @JoinColumn(name = "matiere_id"),
            inverseJoinColumns = @JoinColumn(name = "exercise_id")
    )
    private List<ExerciseEntity> exercises = new ArrayList<>();
}
