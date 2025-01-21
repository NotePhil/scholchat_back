package cmr.notep.business.services;

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


    public String generateWelcomeEmail(String userName, String userEmail, String activationToken) {
        Context context = new Context();
        context.setVariable("userName", userName);
        context.setVariable("userEmail", userEmail);
        context.setVariable("activationUrl", activationUrl + "?activationToken=" + activationToken);

        return templateEngine.process("email/welcome", context);
    }
}
