package cmr.notep.interfaces.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/** GET /classes/{classeId}/cours-programmes/resume : un cours programmé dans la classe et ses compteurs. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoursProgrammeResumeDTO {
    private String coursId;
    private String titre;
    /** Matière(s) du cours, noms séparés par « , » (null si aucune). */
    private String matiere;
    private List<String> matieres;
    private int nbChapitres;
    /** Nombre de programmations (cours_programmer) du cours dans la classe. */
    private int nbSessions;
    /** Prochaine séance prévue (date_cours_prevue >= maintenant, ni ANNULE ni TERMINE), null sinon. */
    private LocalDateTime prochaineSession;
    /** Exercices programmés de type EXERCICE rattachés au cours dans la classe (hors ANNULE). */
    private int nbExercices;
    /** Exercices programmés de type DEVOIR rattachés au cours dans la classe (hors ANNULE). */
    private int nbDevoirs;
}
