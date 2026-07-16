package cmr.notep.interfaces.dto;

import cmr.notep.modele.PeriodiciteContrat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO commun pour prolonger un contrat, changer d'offre, ou simuler le paiement d'activation.
 * nouvelleOffreId reste null pour un simple renouvellement (prolongation) de l'offre courante.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContratActionDto {
    private String nouvelleOffreId;
    private PeriodiciteContrat periodicite;
    private PaymentInfoDto paymentInfo;
}
