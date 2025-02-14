package cmr.notep.business.services;

import cmr.notep.interfaces.modeles.Eleves;
import cmr.notep.interfaces.modeles.IUtilisateurs;
import cmr.notep.interfaces.modeles.Parents;
import cmr.notep.interfaces.modeles.Professeurs;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.annotation.Recover;
import org.springframework.stereotype.Service;

import cmr.notep.business.exceptions.SchoolException;
import cmr.notep.business.exceptions.enums.SchoolErrorCode;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailService {
    private final JavaMailSender mailSender;
    private final EmailTemplateService emailTemplateService;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 2000))
    public void sendWelcomeEmail(IUtilisateurs utilisateur, String activationToken) throws MessagingException {
        String htmlContent = emailTemplateService.generateActivationEmail(utilisateur, activationToken);

        // Get subject based on user type
        String subject = getSubjectForUserType(utilisateur);

        sendEmail(utilisateur.getEmail(), subject, htmlContent);
    }

    private String getSubjectForUserType(IUtilisateurs utilisateur) {
        return switch (utilisateur) {
            case Professeurs p -> "Bienvenue sur ScholChat - Activation de votre compte professeur";
            case Eleves e -> "Bienvenue sur ScholChat - Activation de votre compte étudiant";
            case Parents p -> "Bienvenue sur ScholChat - Activation de votre compte parent";
            default -> "Bienvenue sur ScholChat!";
        };
    }

    public void sendEmail(String to, String subject, String htmlContent) throws MessagingException {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
            log.info("Email sent successfully to: {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
            throw e;
        }
    }

    @Recover
    public void recover(MessagingException e, IUtilisateurs utilisateur, String activationToken) {
        log.error("Failed to send activation email after retries for user {}: {}",
                utilisateur.getEmail(), e.getMessage());
        throw new SchoolException(
                SchoolErrorCode.EMAIL_NOT_SENT,
                "Échec de l'envoi de l'email d'activation après plusieurs tentatives."
        );
    }
}