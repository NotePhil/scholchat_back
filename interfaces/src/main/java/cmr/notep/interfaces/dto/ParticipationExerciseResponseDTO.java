package cmr.notep.interfaces.dto;

import cmr.notep.modele.EtatSoumission;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParticipationExerciseResponseDTO {
    private String utilisateurId;
    private String utilisateurNom;
    private String utilisateurPrenom;
    private String exerciseProgrammerId;
    private String exerciseProgrammerNom;
    /** Cours de rattachement de l'exercice programmé ; null = « Exercices généraux ». */
    private String coursId;
    private String coursTitre;
    private EtatSoumission etatSoumission;
    private String note;
    private String appreciation;
    private Date dateDebut;
    private Date dateFin;
    private Date dateSoumission;
}