package cmr.notep.interfaces.dto;

import cmr.notep.modele.PeriodiciteContrat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClasseCreationDto {
    private String nom;
    private String niveau;
    private String etablissementId;
    private String codeUnique;
    private String moderatorId;
    private String creatorId;
    private boolean accesMajeur;
    private PaymentInfoDto paymentInfo;
    // Offre/Contrat (uniquement pour une classe sans etablissement) : voir ContratBusiness.
    private String offreId;
    private PeriodiciteContrat periodicite;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentInfoDto {
        private String paymentMethod; // "CARD", "OM", "MOMO"
        private String cardNumber;
        private String expiryDate;
        private String cvv;
        private String cardHolderName;
        private String phoneNumber; // For OM/MOMO
        private Double amount;
    }
}