package cmr.notep.interfaces.modeles;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/** GET /parents/{id}/enfants/statuts : un enfant du parent et l'état de ses inscriptions dans les classes. */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EnfantStatut implements Serializable {
    private String enfantId;
    private String prenom;
    private String nom;
    private String niveau;
    private List<ClasseStatut> classes;
    public static final String LIEN_LIE = "LIE";
    /** Demande de suivi en attente de la décision d'un professeur (gestionnaire d'une classe de l'enfant). */
    public static final String LIEN_EN_ATTENTE_VALIDATION_PROFESSEUR = "EN_ATTENTE_VALIDATION_PROFESSEUR";
    public static final String LIEN_REFUSE = "REFUSE";

    /** Enfant ayant son propre compte (e-mail / mot de passe) : le parent n'a qu'un accès en lecture. */
    private boolean majeur;
    /** Majeur uniquement. */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String email;
    /**
     * LIE (au moins une classe suivie acceptée) | EN_ATTENTE_VALIDATION_PROFESSEUR | REFUSE (seul LIE ouvre l'espace
     * de l'enfant). Majeur : {@code classes} = classes dont le suivi a été demandé, avec leur statut.
     */
    private String lienStatut;
    /** Demande de suivi correspondante (lienStatut différent de LIE). */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String demandeLienId;
    /** REFUSE : motif donné par le professeur (facultatif) pour la dernière classe refusée. */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String motifRefusLien;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ClasseStatut implements Serializable {
        public static final String APPROUVEE = "APPROUVEE";
        public static final String EN_ATTENTE = "EN_ATTENTE";
        public static final String REJETEE = "REJETEE";

        private String classeId;
        private String classeNom;
        /** APPROUVEE (accès accordé) | EN_ATTENTE | REJETEE. */
        private String statut;
        /** Motif du refus (REJETEE uniquement). */
        private String motifRejet;
        /** Date de la demande (absente pour un accès accordé sans demande connue). */
        private Date dateDemande;
        /**
         * Enfant majeur : demande de suivi parental de cette classe (APPROUVEE = suivi accepté, lecture possible ;
         * EN_ATTENTE = à valider par le professeur, annulable ; REJETEE = refusée, motifRejet).
         */
        private String demandeSuiviId;
    }
}
