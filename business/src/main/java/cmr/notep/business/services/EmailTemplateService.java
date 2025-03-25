package cmr.notep.business.services;

import cmr.notep.interfaces.modeles.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service
@RequiredArgsConstructor
public class EmailTemplateService {
    private final SpringTemplateEngine templateEngine;

    @Value("${app.activation-url}")
    private String activationUrl;


    public String generateActivationEmail(Utilisateurs utilisateur, String activationToken) {
        Context context = new Context();
        context.setVariable("userName", utilisateur.getNom());
        context.setVariable("userEmail", utilisateur.getEmail());
        context.setVariable("activationUrl", activationUrl + "?activationToken=" + activationToken);

        // Select template based on user type
        String templateName = switch (utilisateur) {
            case Professeurs p -> "email/professor-activation";
            case Eleves e -> "email/student-activation";
            case Parents p -> "email/parent-activation";
            default -> "email/default-activation";
        };

        return templateEngine.process(templateName, context);
    }
}