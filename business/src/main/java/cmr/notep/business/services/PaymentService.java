package cmr.notep.business.services;

import cmr.notep.interfaces.dto.ClasseCreationDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PaymentService {
    
    public boolean processPayment(ClasseCreationDto.PaymentInfoDto paymentInfo) {
        log.info("Processing {} payment for amount: {}", paymentInfo.getPaymentMethod(), paymentInfo.getAmount());
        
        return switch (paymentInfo.getPaymentMethod()) {
            case "CARD" -> processCardPayment(paymentInfo);
            case "OM" -> processMobilePayment(paymentInfo, "Orange Money");
            case "MOMO" -> processMobilePayment(paymentInfo, "MTN Mobile Money");
            default -> {
                log.error("Unsupported payment method: {}", paymentInfo.getPaymentMethod());
                yield false;
            }
        };
    }
    
    private boolean processCardPayment(ClasseCreationDto.PaymentInfoDto paymentInfo) {
        if (paymentInfo.getCardNumber() == null || paymentInfo.getCardNumber().length() < 16) {
            return false;
        }
        if (paymentInfo.getCvv() == null || paymentInfo.getCvv().length() != 3) {
            return false;
        }
        if (paymentInfo.getExpiryDate() == null || !paymentInfo.getExpiryDate().matches("\\d{2}/\\d{2}")) {
            return false;
        }
        log.info("Card payment processed successfully");
        return true;
    }
    
    private boolean processMobilePayment(ClasseCreationDto.PaymentInfoDto paymentInfo, String provider) {
        if (paymentInfo.getPhoneNumber() == null || !paymentInfo.getPhoneNumber().matches("\\+?237[0-9]{9}")) {
            log.error("Invalid phone number for {}", provider);
            return false;
        }
        log.info("{} payment processed successfully", provider);
        return true;
    }
}