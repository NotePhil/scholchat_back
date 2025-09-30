package cmr.notep.interfaces.dto;

import cmr.notep.modele.EtatExercise;
import cmr.notep.modele.ListeNiveau;
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
    private Date dateExoPrevue;
    private Date dateDebutExoEffectif;
    private Date dateFinExoEffectif;
    private List<CoursSummaryDTO> coursLies;
    private List<MatiereSummaryDTO> matieres;
    private List<QuestionReponseSummaryDTO> questions;
    private List<ClasseSummaryDTO> classesDiffusees;
}