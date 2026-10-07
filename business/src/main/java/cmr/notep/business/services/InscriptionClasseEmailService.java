package cmr.notep.business.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * E-mails de l'inscription parent / élève majeur par code de classe (voir {@link InscriptionClasseService}).
 * Le mot de passe temporaire n'apparaît que dans le corps de l'e-mail : il n'est jamais journalisé.
 */
@Service
@Slf4j
public class InscriptionClasseEmailService {
    private final MailServiceInterface mailService;
    private final EmailTemplateService emailTemplateService;

    public InscriptionClasseEmailService(MailServiceInterface mailService, EmailTemplateService emailTemplateService) {
        this.mailService = mailService;
        this.emailTemplateService = emailTemplateService;
    }

    @Async
    public void envoyerIdentifiants(String email, String nomComplet, String classeNom, String motDePasseTemporaire) {
        try {
            String html = emailTemplateService.generateInscriptionClasseApprouveeEmail(
                    email, nomComplet, classeNom, motDePasseTemporaire);
            mailService.sendEmail(email, "Votre compte ScholChat est activé – classe " + classeNom, html);
            log.info("Class sign-up credentials e-mail sent to {}", email);
        } catch (Exception e) {
            log.error("Class sign-up credentials e-mail could not be sent to {}: {}", email, e.getMessage());
        }
    }

    /** Profil élève ajouté à un compte existant : demande d'accès à la classe approuvée, rôle activé. */
    @Async
    public void envoyerProfilEleveValide(String email, String nomComplet, String classeNom) {
        try {
            String html = emailTemplateService.generateProfilEleveValideEmail(email, nomComplet, classeNom);
            mailService.sendEmail(email, "Votre profil élève ScholChat est validé – classe " + classeNom, html);
            log.info("Student-role validated e-mail sent to {}", email);
        } catch (Exception e) {
            log.error("Student-role validated e-mail could not be sent to {}: {}", email, e.getMessage());
        }
    }

    @Async
    public void envoyerRefus(String email, String nomComplet, String classeNom, String motif) {
        try {
            String html = emailTemplateService.generateInscriptionClasseRefuseeEmail(email, nomComplet, classeNom, motif);
            mailService.sendEmail(email, "Votre demande d'inscription à la classe " + classeNom + " n'a pas été acceptée", html);
            log.info("Class sign-up rejection e-mail sent to {}", email);
        } catch (Exception e) {
            log.error("Class sign-up rejection e-mail could not be sent to {}: {}", email, e.getMessage());
        }
    }
}
