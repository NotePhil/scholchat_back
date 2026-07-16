package cmr.notep.interfaces.modeles;

import cmr.notep.modele.TypeCibleOffre;
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
public class Offre {
    private String id;
    private String nom;
    private String description;
    private TypeCibleOffre cible;
    private BigDecimal prixMensuel;
    private Long dureeMensuelleMinutes;
    private BigDecimal prixAnnuel;
    private Long dureeAnnuelleMinutes;
    private Integer nombreClassesInclues;
    private Integer classesBonus;
    private boolean estTest;
    private boolean actif;
    private LocalDateTime dateCreation;
    private LocalDateTime dateMaj;
    private String createdBy;
    // Reduction annuelle affichee cote client, calculee en business (pas stockee)
    private Double reductionAnnuellePourcentage;
    // Purge automatique apres expiration, configurable par l'admin (voir ContratExpirationJob)
    private Long delaiRappelSuppressionMinutes;
    private Long delaiSuppressionMinutes;
    // Restrictions informatives
    private Integer elevesMax;
    private Integer stockageMaxGo;
    private Boolean messagerieIncluse;
}
