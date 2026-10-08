package cmr.notep.interfaces.dto;

import cmr.notep.modele.EtatExercise;
import cmr.notep.modele.EtatSoumission;
import cmr.notep.modele.TypeAssignation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * GET /classes/{classeId}/cours/{coursId}/exercices : exercice programmé d'un cours dans une classe
 * (coursId « general » = exercices sans cours). Champs de participation renseignés seulement avec ?eleveId=.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExerciceCoursClasseDTO {
    private String exerciseProgrammerId;
    private String exerciseId;
    private String titre;
    private String coursId;
    private String coursTitre;
    private TypeAssignation typeAssignation;
    private EtatExercise etat;
    private Date dateExoPrevue;
    private Date dateDebutExoEffectif;
    private Date dateFinExoEffectif;
    private int nbQuestions;
    /** Barème total : somme des points des questions (1 par question sans points). */
    private int points;

    // ── Participation de l'élève (si eleveId) ──
    private String eleveId;
    /** null = pas commencé. */
    private EtatSoumission etatSoumission;
    /** Note brute (ex. « 15/20 »). */
    private String note;
    /** Note ramenée sur 20 (null si non corrigé / illisible). */
    private Double noteSur20;
    private Date dateSoumission;
    /** A_FAIRE, EN_COURS, EN_RETARD, RENDU (en attente de correction), CORRIGE. */
    private String statut;
}
