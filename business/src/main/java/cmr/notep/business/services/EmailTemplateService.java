package cmr.notep.business.services;

import cmr.notep.interfaces.modeles.*;
import cmr.notep.ressourcesjpa.dao.MotifRejetEntity;
import cmr.notep.ressourcesjpa.dao.ProfesseursEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service

public class EmailTemplateService {
    private final SpringTemplateEngine templateEngine;

    public EmailTemplateService(SpringTemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

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

    public String generateRejectionEmail(ProfesseursEntity professeur, MotifRejetEntity motif, String motifSupplementaire) {
        Context context = new Context();
        if (professeur == null) {
            throw new IllegalArgumentException("ProfesseurEntity cannot be null");
        }
        context.setVariable("nom", professeur.getNom());
        context.setVariable("prenom", professeur.getPrenom());
        context.setVariable("email", professeur.getEmail());
        context.setVariable("activationToken", professeur.getActivationToken());
        context.setVariable("motif", motif.getDescriptif());
        context.setVariable("motifSupplementaire", motifSupplementaire);
        // Construisez l'URL directement dans le service
        String updateUrl = "http://localhost:3000/schoolchat/signup?email=" +
                professeur.getEmail() + "&token=" + professeur.getActivationToken();
        context.setVariable("updateUrl", updateUrl);
        return templateEngine.process("email/professor-rejection", context);
    }
}