package cmr.notep.interfaces.dto;

import cmr.notep.modele.TypeAssignation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/** GET /classes/{classeId}/statistiques : statistiques de la classe (vue professeur / gestionnaire). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatistiquesClasseDTO {
    private String classeId;
    /** Nombre d'élèves ayant accès à la classe. */
    private int effectif;
    /** Un élément par cours (les anciens exercices programmés sans cours sont ignorés). */
    private List<Cours> cours;
    private List<Eleve> eleves;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Cours {
        private String coursId;
        private String titre;
        private int chapitresTotal;
        /** Moyenne sur les élèves de (chapitres lus / chapitres) en % (null si cours sans chapitre ou sans élève). */
        private Double progressionMoyenne;
        private List<Exercice> exercices;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Exercice {
        private String exerciseProgrammerId;
        private String titre;
        private TypeAssignation typeAssignation;
        private Date dateFinExoEffectif;
        /** Copies rendues par les élèves de la classe (SOUMIS / EN_ATTENTE_CORRECTION / CORRIGE). */
        private int rendus;
        /** Effectif de la classe. */
        private int attendus;
        private int enAttenteCorrection;
        private int corriges;
        /** Moyenne / min / max /20 des copies corrigées (null si aucune). */
        private Double moyenne;
        private Double min;
        private Double max;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Eleve {
        private String eleveId;
        private String nom;
        private String prenom;
        /** Moyenne des % de chapitres lus sur les cours ayant des chapitres (null si aucun). */
        private Double progressionMoyenne;
        private int devoirsRendus;
        private int devoirsTotal;
        private Double moyenne;
        /** Devoirs non rendus dont la date limite est dépassée. */
        private int enRetard;
    }
}
