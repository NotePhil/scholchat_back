package cmr.notep.interfaces.dto;

import cmr.notep.modele.EtatCours;
import cmr.notep.modele.ListeNiveau;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExerciseSummaryDTO {
    private String id;
    private String nom;
    private String description;
    private EtatCours etat;
    private ListeNiveau niveau;
}