package cmr.notep.business.services;

import cmr.notep.interfaces.modeles.Utilisateurs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * E-mail envoyé au professeur quand l'administrateur valide ses pièces justificatives (profil VALIDE)
 * sur un compte déjà actif. La première validation d'un compte AWAITING_VALIDATION n'utilise pas ce
 * service : l'e-mail d'activation (choix du mot de passe) part déjà à ce moment-là.
 */
@Service
@Slf4j
public class ProfessorVerificationEmailService {
    private final MailServiceInterface mailService;
    private final EmailTemplateService emailTemplateService;

    public ProfessorVerificationEmailService(MailServiceInterface mailService,
                                             EmailTemplateService emailTemplateService) {
        this.mailService = mailService;
        this.emailTemplateService = emailTemplateService;
    }

    @Async
    public void sendProfileValidatedEmail(Utilisateurs utilisateur, boolean roleSupplementaire) {
        try {
            String html = emailTemplateService.generateProfessorVerificationValidatedEmail(utilisateur, roleSupplementaire);
            mailService.sendEmail(utilisateur.getEmail(), "Votre profil professeur ScholChat est validé", html);
            log.info("Professor profile-validated email sent to {}", utilisateur.getEmail());
        } catch (Exception e) {
            log.error("Failed to send professor profile-validated email to {}: {}", utilisateur.getEmail(), e.getMessage());
        }
    }
}
