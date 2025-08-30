package cmr.notep.business.services;

import cmr.notep.interfaces.modeles.Classes;
import cmr.notep.interfaces.modeles.Utilisateurs;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AccessConfirmationEmailService {
    private final MailServiceInterface mailService;
    private final EmailTemplateService emailTemplateService;

    public AccessConfirmationEmailService(MailServiceInterface mailService,
                                          EmailTemplateService emailTemplateService) {
        this.mailService = mailService;
        this.emailTemplateService = emailTemplateService;
    }

    @Async
    public void sendConfirmationEmail(Utilisateurs utilisateur, Classes classe) {
        try {
            log.info("Envoi d'email de confirmation d'accès à {}", utilisateur.getEmail());
            String htmlContent = emailTemplateService.generateAccessConfirmationEmail(utilisateur, classe);

            String subject = "Confirmation d'accès à la classe " + classe.getNom();

            mailService.sendEmail(utilisateur.getEmail(), subject, htmlContent);
            log.info("Email de confirmation d'accès envoyé avec succès à {}", utilisateur.getEmail());
        } catch (MessagingException e) {
            log.error("Échec de l'envoi de l'email de confirmation d'accès: {}", e.getMessage());
            throw new RuntimeException("Échec de l'envoi de l'email de confirmation d'accès", e);
        }
    }
}