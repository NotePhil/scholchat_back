package cmr.notep.interfaces.api;

import cmr.notep.interfaces.dto.ClasseResumeEleveDTO;
import cmr.notep.interfaces.dto.CoursProgrammeResumeDTO;
import cmr.notep.interfaces.dto.ExerciceCoursClasseDTO;
import cmr.notep.interfaces.dto.ProgressionEleveDTO;
import cmr.notep.interfaces.dto.StatistiquesClasseDTO;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Lectures agrégées du suivi pédagogique : cours d'une classe, exercices d'un cours, cartes de classes de l'élève,
 * progression de l'élève et statistiques de la classe. Notes ramenées sur 20 ; seules les copies CORRIGE comptent
 * dans les moyennes. Exercices programmés à l'état ANNULE ignorés dans les compteurs.
 */
public interface SuiviPedagogiqueApi {

    /** Membres de la classe (gestionnaires, enseignants, élèves, parents d'élève) et admin. */
    @GetMapping(path = "/classes/{classeId}/cours-programmes/resume", produces = MediaType.APPLICATION_JSON_VALUE)
    List<CoursProgrammeResumeDTO> resumeCoursProgrammes(@PathVariable String classeId);

    /**
     * Exercices programmés du cours dans la classe (liste vide pour un coursId inconnu, p. ex. l'ancien « general »).
     * Sans eleveId : membres de la classe. Avec eleveId : l'élève lui-même, son parent, un enseignant de la classe
     * ou un admin ; l'élève doit avoir accès à la classe.
     */
    @GetMapping(path = "/classes/{classeId}/cours/{coursId}/exercices", produces = MediaType.APPLICATION_JSON_VALUE)
    List<ExerciceCoursClasseDTO> exercicesDuCours(@PathVariable String classeId, @PathVariable String coursId,
                                                  @RequestParam(required = false) String eleveId);

    /** Classes de l'élève : l'élève lui-même, son parent ou un admin. */
    @GetMapping(path = "/utilisateurs/{eleveId}/classes/resume", produces = MediaType.APPLICATION_JSON_VALUE)
    List<ClasseResumeEleveDTO> resumeClassesEleve(@PathVariable String eleveId);

    /**
     * Progression de l'élève (toutes ses classes, ou classeId) : l'élève, son parent, un admin, ou un enseignant
     * de la classe indiquée.
     */
    @GetMapping(path = "/eleves/{eleveId}/progression", produces = MediaType.APPLICATION_JSON_VALUE)
    ProgressionEleveDTO progressionEleve(@PathVariable String eleveId,
                                         @RequestParam(required = false) String classeId);

    /** Statistiques de la classe : gestionnaires / enseignants de la classe (droit de publication) et admin. */
    @GetMapping(path = "/classes/{classeId}/statistiques", produces = MediaType.APPLICATION_JSON_VALUE)
    StatistiquesClasseDTO statistiquesClasse(@PathVariable String classeId);
}
