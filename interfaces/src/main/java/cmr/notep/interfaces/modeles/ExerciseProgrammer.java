package cmr.notep.interfaces.modeles;

import cmr.notep.modele.EtatExercise;
import cmr.notep.modele.TypeAssignation;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class ExerciseProgrammer extends Exercise {
    private String exerciseId;
    private TypeAssignation typeAssignation;
    private Date dateExoPrevue;
    private Date dateDebutExoEffectif;
    private Date dateFinExoEffectif;
    private String programmeParId;
    private List<Classes> classesDiffusees;
    private List<String> classeIds;
    private List<String> coursIds;
    /** Cours de rattachement (obligatoire ; null seulement sur d'anciennes lignes, ignorées). Non mappé par Dozer depuis l'entité (champ cours). */
    private String coursId;
    /** {classeId: coursId | null} — cours choisi par classe (voir ExerciseProgrammerRequestDTO). */
    private java.util.Map<String, String> coursParClasse;
}