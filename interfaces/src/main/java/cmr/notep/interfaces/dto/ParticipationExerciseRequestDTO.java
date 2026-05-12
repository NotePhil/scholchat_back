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
public class ParticipationExerciseRequestDTO {
    private String utilisateurId;
    private String exerciseProgrammerId;
    private EtatSoumission etatSoumission;
    private Date dateDebut;
    private Date dateFin;
    private String note;
    private String appreciation;
}