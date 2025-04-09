package cmr.notep.business.services;

import cmr.notep.interfaces.modeles.*;

import org.springframework.scheduling.annotation.Async;


@Async
public interface ActivationEmailService {
    void sendActivationEmail(Utilisateurs utilisateur, String activationToken);

    static ActivationEmailService create(MailServiceInterface mailService,
                                         EmailTemplateService emailTemplateService) {
        return new ActivationEmailServiceImpl(mailService, emailTemplateService);
    }
}
