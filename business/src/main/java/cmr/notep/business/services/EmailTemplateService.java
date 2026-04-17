package cmr.notep.business.services;

import cmr.notep.interfaces.modeles.*;
import cmr.notep.ressourcesjpa.dao.MotifRejetEntity;
import cmr.notep.ressourcesjpa.dao.ProfesseursEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.Date;
import java.util.List;

@Service

public class EmailTemplateService {
    private final SpringTemplateEngine templateEngine;

    public EmailTemplateService(SpringTemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    @Value("${app.activation-url}")
    private String activationUrl;

    @Value("${app.class-approval-url}")
    private String classApprovalUrl;

    @Value("${app.class-rejection-url}")
    private String classRejectionUrl;


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
    public String generateAccessConfirmationEmail(Utilisateurs utilisateur, Classes classe) {
        Context context = new Context();
        context.setVariable("user", utilisateur);
        context.setVariable("classe", classe);

        return templateEngine.process("email/access-confirmation-email", context);
    }

    public String generateAccessRequestNotification(Utilisateurs moderator, Utilisateurs demandeur, Classes classe, Date dateDemande) {
        Context context = new Context();
        context.setVariable("moderator", moderator);
        context.setVariable("demandeur", demandeur);
        context.setVariable("classe", classe);
        context.setVariable("dateDemande", dateDemande);
        context.setVariable("dashboardUrl", "http://your-frontend-url.com/moderator/dashboard");

        return templateEngine.process("email/access-request-notification", context);
    }

    public String generateAccessRejectionEmail(Utilisateurs utilisateur, Classes classe, String motifRejet) {
        Context context = new Context();
        context.setVariable("user", utilisateur);
        context.setVariable("classe", classe);
        context.setVariable("motifRejet", motifRejet);
        return templateEngine.process("email/access-rejection", context);
    }

    public String generateAwaitingValidationEmail(Utilisateurs utilisateur) {
        Context context = new Context();
        context.setVariable("userName", utilisateur.getNom());
        context.setVariable("userEmail", utilisateur.getEmail());

        return templateEngine.process("email/professor-awaiting-validation", context);
    }
    public String generateClassCreationNotificationEmail(Classes classe, Professeurs professeur, String validationUrl) {
        Context context = new Context();
        context.setVariable("classe", classe);
        context.setVariable("professeur", professeur);
        context.setVariable("validationUrl", validationUrl);

        return templateEngine.process("email/class-creation-notification", context);
    }

    public String generateParentAccessRequestEmail(Utilisateurs moderator, Utilisateurs parent, Classes classe, List<String> eleves, boolean accesMajeur) {
        Context context = new Context();
        context.setVariable("moderator", moderator);
        context.setVariable("parent", parent);
        context.setVariable("classe", classe);
        context.setVariable("eleves", eleves);
        context.setVariable("accesMajeur", accesMajeur);
        context.setVariable("dashboardUrl", "http://votre-frontend.com/moderator/dashboard");

        return templateEngine.process("email/parent-access-request", context);
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

    public String generateClassApprovalRequestEmail(Classes classe, Etablissement etablissement, String classeId, String etablissementId) {
        Context context = new Context();
        context.setVariable("classe", classe);
        context.setVariable("etablissement", etablissement);
        
        String approvalUrl = classApprovalUrl + "?classeId=" + classeId + "&etablissementId=" + etablissementId;
        String rejectionUrl = classRejectionUrl + "?classeId=" + classeId + "&etablissementId=" + etablissementId;
        
        context.setVariable("approvalUrl", approvalUrl);
        context.setVariable("rejectionUrl", rejectionUrl);
        
        return templateEngine.process("email/class-approval-request", context);
    }

    public String generateGestionnaireAjoutEmail(Utilisateurs gestionnaire, Etablissement etablissement) {
        Context context = new Context();
        context.setVariable("gestionnaire", gestionnaire);
        context.setVariable("etablissement", etablissement);
        return templateEngine.process("email/gestionnaire-ajout-etablissement", context);
    }

    public String generateClassApprovalNotificationEmail(Classes classe, Etablissement etablissement) {
        Context context = new Context();
        context.setVariable("classe", classe);
        context.setVariable("etablissement", etablissement);
        return templateEngine.process("email/class-approval-notification", context);
    }

    public String generateClassCreationNotificationEmail(Classes classe, Etablissement etablissement) {
        Context context = new Context();
        context.setVariable("classe", classe);
        context.setVariable("etablissement", etablissement);
        context.setVariable("dateCreation", new Date());
        return templateEngine.process("email/class-creation-notification", context);
    }
}