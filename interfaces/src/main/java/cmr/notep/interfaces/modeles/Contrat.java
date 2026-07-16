package cmr.notep.interfaces.modeles;

import cmr.notep.modele.PeriodiciteContrat;
import cmr.notep.modele.StatutContrat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Contrat {
    private String id;
    private String offreId;
    private String offreNom;
    private String classeId;
    private String etablissementId;
    private PeriodiciteContrat periodicite;
    private BigDecimal prixPaye;
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
    private StatutContrat statut;
    private LocalDateTime datePaiement;
    private String contratPrecedentId;
    private String createdBy;
    private LocalDateTime dateCreation;
    // Champs calcules utiles a l'affichage (quota etablissement)
    private Integer classesUtilisees;
    private Integer classesMax;
    // Prix courants de l'offre liee (pour permettre au frontend de recalculer le montant
    // si l'utilisateur bascule la periodicite lors d'un "prolonger", independamment du
    // prixPaye qui est un instantane historique de la souscription/du dernier paiement).
    private java.math.BigDecimal prixMensuel;
    private java.math.BigDecimal prixAnnuel;
    // Restrictions informatives heritees de l'offre
    private Integer elevesMax;
    private Integer stockageMax;
    private Boolean messagerie;
    // Purge automatique : renseigne uniquement quand une suppression definitive est planifiee
    // (contrat expire depuis plus longtemps que le delai de rappel configure sur l'offre)
    private boolean suppressionImminente;
    private LocalDateTime dateSuppressionPrevue;
}
