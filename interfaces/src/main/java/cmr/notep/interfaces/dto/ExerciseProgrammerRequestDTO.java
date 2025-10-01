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
public class ExerciseProgrammerRequestDTO {
    private String exerciseId; // ID de l'exercice existant à programmer
    private String programmeParId;
    private Date dateExoPrevue;
    private Date dateDebutExoEffectif;
    private Date dateFinExoEffectif;
    private EtatExercise etat;
    private List<String> classeIds;
}