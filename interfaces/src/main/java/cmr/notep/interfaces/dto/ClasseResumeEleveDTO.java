package cmr.notep.interfaces.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** GET /utilisateurs/{eleveId}/classes/resume : carte d'une classe de l'élève (vue élève / parent). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClasseResumeEleveDTO {
    private String classeId;
    private String nom;
    private String niveau;
    private String etat;
    /** Cours distincts programmés dans la classe. */
    private int nbCours;
    /** Devoirs (DEVOIR) non rendus dont la date limite n'est pas dépassée. */
    private int nbDevoirsAFaire;
    /** Devoirs non rendus dont la date limite est dépassée. */
    private int nbDevoirsEnRetard;
    /** Moyenne /20 des copies corrigées de l'élève dans la classe (null si aucune). */
    private Double moyenne;
}
