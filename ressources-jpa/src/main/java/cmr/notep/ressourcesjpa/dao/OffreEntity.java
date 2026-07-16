package cmr.notep.ressourcesjpa.dao;

import cmr.notep.modele.TypeCibleOffre;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "offres", schema = "ressources")
public class OffreEntity {
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private String id;

    @Column(name = "nom", nullable = false)
    private String nom;

    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "cible", nullable = false)
    private TypeCibleOffre cible;

    @Column(name = "prix_mensuel", precision = 12, scale = 2)
    private BigDecimal prixMensuel;

    @Column(name = "duree_mensuelle_minutes")
    private Long dureeMensuelleMinutes;

    @Column(name = "prix_annuel", precision = 12, scale = 2)
    private BigDecimal prixAnnuel;

    @Column(name = "duree_annuelle_minutes")
    private Long dureeAnnuelleMinutes;

    @Column(name = "nombre_classes_inclues")
    private Integer nombreClassesInclues;

    @Column(name = "classes_bonus")
    private Integer classesBonus;

    @Column(name = "est_test", nullable = false)
    private boolean estTest;

    @Column(name = "actif", nullable = false)
    private boolean actif;

    @Column(name = "date_creation")
    private LocalDateTime dateCreation;

    @Column(name = "date_maj")
    private LocalDateTime dateMaj;

    @Column(name = "created_by")
    private String createdBy;

    // Delais de purge automatique apres expiration (en minutes), configurables par l'admin et
    // modifiables a tout moment. Nul = pas de rappel/suppression automatique pour cette offre.
    @Column(name = "delai_rappel_suppression_minutes")
    private Long delaiRappelSuppressionMinutes;

    @Column(name = "delai_suppression_minutes")
    private Long delaiSuppressionMinutes;

    // Restrictions optionnelles (non appliquees pour le moment, affichees a titre informatif ;
    // prevues pour une future limitation reelle cote applicatif).
    @Column(name = "eleves_max")
    private Integer elevesMax;

    @Column(name = "stockage_max_go")
    private Integer stockageMaxGo;

    @Column(name = "messagerie_incluse")
    private Boolean messagerieIncluse;
}
