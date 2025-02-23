package cmr.notep.business.services;

import jakarta.mail.MessagingException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

public interface MailServiceInterface {

    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 2000))
    void sendEmail(String to, String subject, String htmlContent) throws MessagingException;
}
