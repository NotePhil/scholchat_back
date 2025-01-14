package cmr.notep.business.services;

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
    public void sendWelcomeEmail(String to, String userName, String activationToken) throws MessagingException {
        String subject = "Bienvenue sur ScholChat!";
        String htmlContent = emailTemplateService.generateWelcomeEmail(userName, to, activationToken);

        sendEmail(to, subject, htmlContent);
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
    public void recover(MessagingException e, String to, String userName, String activationToken) {
        // Log and throw a SchoolException to maintain consistent error handling
        log.error("Failed to send activation email after retries: {}", e.getMessage());
        throw new SchoolException(SchoolErrorCode.EMAIL_NOT_SENT, "Failed to send activation email after retries.");
    }
}