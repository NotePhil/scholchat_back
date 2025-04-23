package cmr.notep.business.services;

import cmr.notep.business.exceptions.SchoolErrorEmail;
import cmr.notep.business.exceptions.enums.SchoolErrorCode;
import cmr.notep.interfaces.modeles.Eleves;
import cmr.notep.interfaces.modeles.Parents;
import cmr.notep.interfaces.modeles.Professeurs;
import cmr.notep.interfaces.modeles.Utilisateurs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j

public class ActivationEmailServiceImpl implements ActivationEmailService {
    private final MailServiceInterface mailService;
    private final EmailTemplateService emailTemplateService;

    public ActivationEmailServiceImpl(MailServiceInterface mailService,
                                      EmailTemplateService emailTemplateService) {
        this.mailService = mailService;
        this.emailTemplateService = emailTemplateService;
    }

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
        }catch (Exception e) {
            log.error("Failed to send activation email to {}: {}", utilisateur.getEmail(), e.getMessage());
            throw new SchoolErrorEmail(SchoolErrorCode.EMAIL_ERROR, "Failed to send...", e);
        }
    }
}
