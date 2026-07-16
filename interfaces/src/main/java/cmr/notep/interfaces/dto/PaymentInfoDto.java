package cmr.notep.interfaces.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentInfoDto {
    private String paymentMethod; // "CARD", "OM", "MOMO"
    private String cardNumber;
    private String expiryDate;
    private String cvv;
    private String cardHolderName;
    private String phoneNumber; // For OM/MOMO
    private Double amount;
}
