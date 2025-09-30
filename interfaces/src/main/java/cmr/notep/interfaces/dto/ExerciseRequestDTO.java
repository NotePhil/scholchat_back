package cmr.notep.interfaces.dto;

import cmr.notep.modele.EtatExercise;
import cmr.notep.modele.EtatX;
import cmr.notep.modele.ListeNiveau;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExerciseRequestDTO {
    private String nom;
    private String description;
    private ListeNiveau niveau;
    private String restriction;
    private String redacteurId;
    private EtatExercise etat;
}