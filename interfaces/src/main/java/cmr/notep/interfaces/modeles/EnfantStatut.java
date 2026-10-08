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
    }
}
