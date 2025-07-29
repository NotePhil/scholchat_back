package cmr.notep.ressourcesjpa.dao;

import cmr.notep.modele.EtatCoursProgramme;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "cours_programmes", schema = "ressources")
public class CoursProgrammerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    @JoinColumn(name = "cours_id", nullable = false)
    private CoursEntity cours;

    @Column(name = "date_cours_prevue", nullable = false)
    private LocalDateTime dateCoursPrevue;

    @Column(name = "date_debut_effectif")
    private LocalDateTime dateDebutCoursEffectif;

    @Column(name = "date_fin_effectif")
    private LocalDateTime dateFinCoursEffectif;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EtatCoursProgramme etatCoursProgramme;

    @ManyToOne
    @JoinColumn(name = "classe_id")
    private ClassesEntity classe;

    @ManyToMany
    @JoinTable(
            name = "participation_cours",
            schema = "ressources",
            joinColumns = @JoinColumn(name = "cours_programme_id"),
            inverseJoinColumns = @JoinColumn(name = "utilisateur_id")
    )
    private List<UtilisateursEntity> participants = new ArrayList<>();
}