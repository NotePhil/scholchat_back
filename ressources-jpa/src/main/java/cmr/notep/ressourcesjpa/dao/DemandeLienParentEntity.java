package cmr.notep.ressourcesjpa.dao;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Demande de suivi parental PAR CLASSE : un parent demande à suivre un élève ayant son propre compte (enfant majeur)
 * dans une classe ; validée par un gestionnaire de cette classe (ou un administrateur), pas par l'élève. La demande
 * ACCEPTEE vaut autorisation de lecture du parent pour cette classe.
 * Identifiants seulement (pas d'association) : un compte peut avoir plusieurs sous-types.
 */
@Getter
@Setter
@Entity
@Table(name = "demandes_lien_parent", schema = "ressources")
public class DemandeLienParentEntity {

    /** En attente de la décision d'un gestionnaire d'une classe de l'élève (ou d'un administrateur). */
    public static final String EN_ATTENTE = "EN_ATTENTE";
    /** Validée : lien parent_eleve créé. */
    public static final String ACCEPTEE = "ACCEPTEE";
    /** Refusée par un gestionnaire / un administrateur (motif facultatif). */
    public static final String REFUSEE = "REFUSEE";
    /** Annulée par le parent avant décision (ou ancienne demande sans objet, voir changeset 31). */
    public static final String ANNULEE = "ANNULEE";
    /** Suivi accordé puis retiré par un gestionnaire / un administrateur (lien parent_eleve supprimé). */
    public static final String RETIREE = "RETIREE";

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "parent_id", nullable = false)
    private String parentId;

    @Column(name = "eleve_id", nullable = false)
    private String eleveId;

    /** Classe suivie (une demande par parent, élève et classe) ; null pour une ancienne demande. */
    @Column(name = "classe_id")
    private String classeId;

    @Column(name = "etat", nullable = false, length = 30)
    private String etat;

    @Column(name = "date_demande", nullable = false)
    private LocalDateTime dateDemande;

    @Column(name = "date_reponse")
    private LocalDateTime dateReponse;

    /** Motif du refus (ou note d'annulation / de retrait). */
    @Column(name = "motif", length = 500)
    private String motif;

    /** Utilisateur (gestionnaire / administrateur / parent) qui a traité la demande. */
    @Column(name = "traite_par")
    private String traitePar;
}
