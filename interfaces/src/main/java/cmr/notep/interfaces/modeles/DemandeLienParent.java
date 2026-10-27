package cmr.notep.interfaces.modeles;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Demande de suivi parental PAR CLASSE (parent → élève majeur ayant son propre compte, dans une classe), validée par
 * un gestionnaire de cette classe ou un administrateur :
 * GET /classes/{classeId}/demandes-suivi, POST /demandes-suivi/{id}/approuver|refuser|annuler.
 * Sert aussi aux suivis actifs (GET /eleves/{id}/suivi-parental, GET /classes/{id}/suivis-parentaux) :
 * etat ACCEPTEE, {@code depuis} = date de validation ; DELETE /demandes-suivi/{id} retire ce suivi.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DemandeLienParent implements Serializable {
    private String id;
    private String parentId;
    private String parentPrenom;
    private String parentNom;
    private String parentEmail;
    private String eleveId;
    private String elevePrenom;
    private String eleveNom;
    private String eleveEmail;
    /** Classe suivie (une demande par parent, élève et classe). */
    private String classeId;
    private String classeNom;
    /** EN_ATTENTE | ACCEPTEE | REFUSEE | ANNULEE | RETIREE. */
    private String etat;
    private LocalDateTime dateDemande;
    private LocalDateTime dateReponse;
    /** Suivi actif : date depuis laquelle le parent suit l'élève (peut être absente pour un lien ancien). */
    private LocalDateTime depuis;
    /** Motif du refus (REFUSEE). */
    private String motif;
}
