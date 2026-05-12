package cmr.notep.interfaces.dto;

import cmr.notep.modele.EtatExercise;
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
public class ExerciseProgrammerRequestDTO {
    private String exerciseId;
    private String programmeParId;
    private TypeAssignation typeAssignation; // EXERCICE or DEVOIR
    private Date dateExoPrevue;
    private Date dateDebutExoEffectif;
    private Date dateFinExoEffectif;
    private EtatExercise etat;
    private List<String> classeIds;
    private List<String> coursIds; // optional: link to specific courses
}