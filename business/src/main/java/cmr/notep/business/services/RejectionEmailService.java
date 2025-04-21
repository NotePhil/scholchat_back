package cmr.notep.business.services;

import cmr.notep.ressourcesjpa.dao.ProfesseursEntity;
import cmr.notep.ressourcesjpa.dao.MotifRejetEntity;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service

@Slf4j
public class RejectionEmailService implements IRejectionEmailService {
    private final MailServiceInterface mailService;
    private final EmailTemplateService emailTemplateService;

    public RejectionEmailService(MailServiceInterface mailService, EmailTemplateService emailTemplateService) {
        this.mailService = mailService;
        this.emailTemplateService = emailTemplateService;
    }

    public void sendRejectionEmail(ProfesseursEntity professeur, MotifRejetEntity motif, String motifSupplementaire) {
        try {
            log.info("Sending rejection email asynchronously to {}", professeur.getEmail());
            String htmlContent = emailTemplateService.generateRejectionEmail(
                    professeur,
                    motif,
                    motifSupplementaire
            );

            String subject = "Votre demande de compte professeur a été rejetée";

            mailService.sendEmail(professeur.getEmail(), subject, htmlContent);
            log.info("Rejection email sent successfully to {}", professeur.getEmail());
        } catch (MessagingException e) {
            log.error("Failed to send rejection email to {}: {}", professeur.getEmail(), e.getMessage());
            throw new RuntimeException("Failed to send rejection email", e);
        }
    }
}