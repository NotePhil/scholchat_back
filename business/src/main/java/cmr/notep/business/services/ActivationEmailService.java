package cmr.notep.business.services;

import cmr.notep.business.exceptions.SchoolErrorEmail;
import cmr.notep.business.exceptions.SchoolException;
import cmr.notep.business.exceptions.enums.SchoolErrorCode;
import cmr.notep.interfaces.modeles.*;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
public interface ActivationEmailService {
    void sendActivationEmail(Utilisateurs utilisateur, String activationToken);
}
