package cmr.notep.business.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * E-mail du code de vérification du compte (voir {@link VerificationCompteService}). Envoi asynchrone : la
 * durée de la réponse ne révèle pas si un compte existe. Le code n'est jamais journalisé.
 */
@Service
@Slf4j
public class VerificationCompteEmailService {
    private final MailServiceInterface mailService;
    private final EmailTemplateService emailTemplateService;

    public VerificationCompteEmailService(MailServiceInterface mailService, EmailTemplateService emailTemplateService) {
        this.mailService = mailService;
        this.emailTemplateService = emailTemplateService;
    }

    @Async
    public void envoyerCode(String email, String nomComplet, String code, int validiteMinutes) {
        try {
            String html = emailTemplateService.generateCodeVerificationCompteEmail(email, nomComplet, code, validiteMinutes);
            mailService.sendEmail(email, "Votre code de vérification ScholChat : " + code, html);
            log.info("Account verification code e-mail sent to {}", email);
        } catch (Exception e) {
            log.error("Account verification code e-mail could not be sent to {}: {}", email, e.getMessage());
        }
    }
}
