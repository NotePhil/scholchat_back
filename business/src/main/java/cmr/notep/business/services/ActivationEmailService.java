package cmr.notep.business.services;

import cmr.notep.interfaces.modeles.*;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActivationEmailService {
    private final MailService mailService;
    private final EmailTemplateService emailTemplateService;

    @Async
    public void sendActivationEmail(Utilisateurs utilisateur, String activationToken) {
        try {
            log.info("Sending activation email asynchronously to {}", utilisateur.getEmail());
            String htmlContent = emailTemplateService.generateActivationEmail(utilisateur, activationToken);

            // Customize subject based on user type
            String subject = switch (utilisateur) {
                case Professeurs p -> "Bienvenue sur ScholChat - Activation de votre compte professeur";
                case Eleves e -> "Bienvenue sur ScholChat - Activation de votre compte étudiant";
                case Parents p -> "Bienvenue sur ScholChat - Activation de votre compte parent";
                default -> "Bienvenue sur ScholChat";
            };

            mailService.sendEmail(utilisateur.getEmail(), subject, htmlContent);
            log.info("Activation email sent successfully to {}", utilisateur.getEmail());
        } catch (MessagingException e) {
            log.error("Failed to send activation email to {}: {}", utilisateur.getEmail(), e.getMessage());
            throw new RuntimeException("Failed to send activation email", e);
        }
    }
}