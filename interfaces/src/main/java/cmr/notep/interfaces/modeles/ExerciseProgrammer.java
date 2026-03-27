package cmr.notep.interfaces.modeles;

import cmr.notep.modele.EtatExercise;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class ExerciseProgrammer extends Exercise {
    private String exerciseId;
    private Date dateExoPrevue;
    private Date dateDebutExoEffectif;
    private Date dateFinExoEffectif;

    private String programmeParId;
    private List<Classes> classesDiffusees;
    private List<String> classeIds; // Frontend sends this
}