package cmr.notep.interfaces.dto;

import cmr.notep.modele.EtatExercise;
import cmr.notep.modele.ListeNiveau;
import cmr.notep.modele.TypeAssignation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExerciseProgrammerResponseDTO {
    private String id;
    private String exerciseId;  // base exercise ID — needed for /questions/exercise/{exerciseId}
    private String nom;
    private String description;
    private Date dateCreation;
    private EtatExercise etat;
    private String restriction;
    private ListeNiveau niveau;
    private String redacteurId;
    private String programmeParId;
    private String programmeParNom;
    private String programmeParPrenom;
    private TypeAssignation typeAssignation;
    private Date dateExoPrevue;
    private Date dateDebutExoEffectif;
    private Date dateFinExoEffectif;
    /** Cours de rattachement de l'exercice programmé ; null = « Exercices généraux ». */
    private String coursId;
    private String coursTitre;
    private List<CoursSummaryDTO> coursLies;
    private List<MatiereSummaryDTO> matieres;
    private List<QuestionReponseSummaryDTO> questions;
    private List<ClasseSummaryDTO> classesDiffusees;
    private List<ParticipationExerciseResponseDTO> participations;
    /**
     * POST /exercises-programmer[/programmer-et-diffuser] uniquement : toutes les programmations créées par la requête
     * (la première est aussi l'objet racine). Plusieurs éléments lorsque coursParClasse associe des cours différents
     * aux classes. Null sur les autres endpoints.
     */
    private List<ExerciseProgrammerResponseDTO> programmations;
    /** Nombre de programmations créées (POST uniquement ; null ailleurs). */
    private Integer nombreProgrammations;
}