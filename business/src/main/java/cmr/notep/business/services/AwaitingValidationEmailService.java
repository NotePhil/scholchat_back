package cmr.notep.business.services;

import cmr.notep.interfaces.modeles.Utilisateurs;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AwaitingValidationEmailService {
    private final MailServiceInterface mailService;
    private final EmailTemplateService emailTemplateService;

    public AwaitingValidationEmailService(MailServiceInterface mailService,
                                          EmailTemplateService emailTemplateService) {
        this.mailService = mailService;
        this.emailTemplateService = emailTemplateService;
    }

    @Async
    public void sendAwaitingValidationEmail(Utilisateurs utilisateur) {
        try {
            log.info("Sending awaiting validation email to {}", utilisateur.getEmail());
            String htmlContent = emailTemplateService.generateAwaitingValidationEmail(utilisateur);

            String subject = "Votre compte professeur est en attente de validation";

            mailService.sendEmail(utilisateur.getEmail(), subject, htmlContent);
            log.info("Awaiting validation email sent successfully to {}", utilisateur.getEmail());
        } catch (MessagingException e) {
            log.error("Failed to send awaiting validation email to {}: {}", utilisateur.getEmail(), e.getMessage());
            throw new RuntimeException("Failed to send awaiting validation email", e);
        }
    }
}