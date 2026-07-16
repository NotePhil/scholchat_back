package cmr.notep.ressourcesjpa.dao;

import cmr.notep.modele.PeriodiciteContrat;
import cmr.notep.modele.StatutContrat;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "contrats", schema = "ressources")
public class ContratEntity {
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private String id;

    @ManyToOne
    @JoinColumn(name = "offre_id", nullable = false)
    private OffreEntity offre;

    @ManyToOne
    @JoinColumn(name = "classe_id")
    private ClassesEntity classe;

    @ManyToOne
    @JoinColumn(name = "etablissement_id")
    private EtablissementEntity etablissement;

    @Enumerated(EnumType.STRING)
    @Column(name = "periodicite", nullable = false)
    private PeriodiciteContrat periodicite;

    @Column(name = "prix_paye", precision = 12, scale = 2)
    private BigDecimal prixPaye;

    @Column(name = "date_debut")
    private LocalDateTime dateDebut;

    @Column(name = "date_fin")
    private LocalDateTime dateFin;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false)
    private StatutContrat statut;

    @Column(name = "date_paiement")
    private LocalDateTime datePaiement;

    @ManyToOne
    @JoinColumn(name = "contrat_precedent_id")
    private ContratEntity contratPrecedent;

    @Column(name = "date_derniere_notification_expiration")
    private LocalDateTime dateDerniereNotificationExpiration;

    @Column(name = "renewal_token", length = 1000)
    private String renewalToken;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "date_creation")
    private LocalDateTime dateCreation;

    // Purge automatique apres expiration (voir OffreEntity.delaiRappelSuppressionMinutes/
    // delaiSuppressionMinutes et ContratExpirationJob) : evite de renvoyer plusieurs fois le
    // rappel de suppression imminente.
    @Column(name = "date_rappel_suppression_envoye")
    private LocalDateTime dateRappelSuppressionEnvoye;

    // true si ce contrat a ete cree/active directement par un admin (support), sans passer par
    // le paiement simule.
    @Column(name = "accorde_par_admin")
    private boolean accordeParAdmin;
}
