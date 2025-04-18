package cmr.notep.business.services;

import cmr.notep.business.exceptions.SchoolException;
import cmr.notep.business.exceptions.enums.SchoolErrorCode;
import cmr.notep.interfaces.modeles.Utilisateurs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@Slf4j
@RequiredArgsConstructor
public class PasswordResetEmailService {

    private final MailServiceInterface mailService;
    private final TemplateEngine templateEngine;

    @Value("${app.reset-password-url}")
    private String resetPasswordUrl;

    public void sendPasswordResetEmail(Utilisateurs user, String resetToken) {
        try {
            String subject = "Réinitialisation de votre mot de passe ScholChat";
            String htmlContent = generateResetEmail(user, resetToken);

            mailService.sendEmail(user.getEmail(), subject, htmlContent);
            log.info("Password reset email sent to {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send password reset email to {}: {}", user.getEmail(), e.getMessage());
            throw new SchoolException(SchoolErrorCode.EMAIL_NOT_SENT, "Failed to send password reset email");
        }
    }

    private String generateResetEmail(Utilisateurs user, String resetToken) {
        Context context = new Context();
        context.setVariable("userName", user.getNom());
        context.setVariable("resetUrl", resetPasswordUrl + "?token=" + resetToken);

        return templateEngine.process("email/password-reset", context);
    }
}