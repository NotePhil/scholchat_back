package cmr.notep.interfaces.dto;

import cmr.notep.modele.TypeAssignation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/** GET /eleves/{eleveId}/progression?classeId= : progression d'un élève (vue élève / parent). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProgressionEleveDTO {
    private String eleveId;
    /** Classe filtrée (null = toutes les classes de l'élève). */
    private String classeId;
    private Global global;
    /** Un élément par cours (les anciens exercices programmés sans cours sont ignorés). */
    private List<Cours> cours;
    private List<DevoirEnRetard> devoirsEnRetard;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Global {
        /** Chapitres lus / chapitres au total sur les cours, en % (0-100, entier). */
        private int progressionCours;
        private int devoirsRendus;
        private int devoirsTotal;
        /** Moyenne /20 des copies corrigées (null si aucune). */
        private Double moyenne;
        private LocalDateTime dernierActivite;
        /** Alias orthographique de dernierActivite. */
        private LocalDateTime derniereActivite;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Cours {
        private String coursId;
        private String titre;
        private int chapitresLus;
        private int chapitresTotal;
        private int pourcentage;
        /** Exercices et devoirs rendus (SOUMIS / EN_ATTENTE_CORRECTION / CORRIGE). */
        private int exercicesFaits;
        private int exercicesTotal;
        private Double moyenne;
        private LocalDateTime derniereActivite;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DevoirEnRetard {
        private String exerciseProgrammerId;
        private String titre;
        private TypeAssignation typeAssignation;
        private String coursId;
        private String coursTitre;
        private String classeId;
        private String classeNom;
        private Date dateFinExoEffectif;
    }
}
