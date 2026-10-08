package cmr.notep.business.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * E-mails de l'inscription parent / élève majeur par code de classe (voir {@link InscriptionClasseService}).
 * Le mot de passe temporaire n'apparaît que dans le corps de l'e-mail : il n'est jamais journalisé.
 */
@Service
@Slf4j
public class InscriptionClasseEmailService {
    private final MailServiceInterface mailService;
    private final EmailTemplateService emailTemplateService;

    public InscriptionClasseEmailService(MailServiceInterface mailService, EmailTemplateService emailTemplateService) {
        this.mailService = mailService;
        this.emailTemplateService = emailTemplateService;
    }

    @Async
    public void envoyerIdentifiants(String email, String nomComplet, String classeNom, String motDePasseTemporaire) {
        try {
            String html = emailTemplateService.generateInscriptionClasseApprouveeEmail(
                    email, nomComplet, classeNom, motDePasseTemporaire);
            mailService.sendEmail(email, "Votre compte ScholChat est activé – classe " + classeNom, html);
            log.info("Class sign-up credentials e-mail sent to {}", email);
        } catch (Exception e) {
            log.error("Class sign-up credentials e-mail could not be sent to {}: {}", email, e.getMessage());
        }
    }

    /** Profil élève ajouté à un compte existant : demande d'accès à la classe approuvée, rôle activé. */
    @Async
    public void envoyerProfilEleveValide(String email, String nomComplet, String classeNom) {
        try {
            String html = emailTemplateService.generateProfilEleveValideEmail(email, nomComplet, classeNom);
            mailService.sendEmail(email, "Votre profil élève ScholChat est validé – classe " + classeNom, html);
            log.info("Student-role validated e-mail sent to {}", email);
        } catch (Exception e) {
            log.error("Student-role validated e-mail could not be sent to {}: {}", email, e.getMessage());
        }
    }

    /** Inscription publique d'un parent avec ses enfants : identifiants + liste des enfants (en attente). */
    @Async
    public void envoyerInscriptionParentRecue(String email, String nomComplet, String motDePasseTemporaire,
                                              java.util.List<java.util.Map<String, String>> enfants) {
        try {
            String html = emailTemplateService.generateInscriptionParentRecueEmail(email, nomComplet,
                    motDePasseTemporaire, enfants);
            mailService.sendEmail(email, "Inscription bien reçue – vos identifiants ScholChat", html);
            log.info("Parent sign-up e-mail (credentials + children) sent to {}", email);
        } catch (Exception e) {
            log.error("Parent sign-up e-mail could not be sent to {}: {}", email, e.getMessage());
        }
    }

    /** Inscription publique d'un élève majeur : accusé de réception, SANS identifiants (envoyés à l'approbation). */
    @Async
    public void envoyerInscriptionEleveRecue(String email, String nomComplet, String classeNom) {
        try {
            String html = emailTemplateService.generateInscriptionEleveRecueEmail(email, nomComplet, classeNom);
            mailService.sendEmail(email, "Inscription bien reçue – classe " + classeNom, html);
            log.info("Student sign-up acknowledgement e-mail sent to {}", email);
        } catch (Exception e) {
            log.error("Student sign-up acknowledgement e-mail could not be sent to {}: {}", email, e.getMessage());
        }
    }

    @Async
    public void envoyerEnfantAccepte(String email, String nomCompletParent, String enfantNom, String classeNom) {
        try {
            String html = emailTemplateService.generateEnfantAccesApprouveEmail(nomCompletParent, enfantNom, classeNom);
            mailService.sendEmail(email, "Votre enfant " + enfantNom + " a été accepté dans la classe " + classeNom, html);
            log.info("Child access approved e-mail sent to {}", email);
        } catch (Exception e) {
            log.error("Child access approved e-mail could not be sent to {}: {}", email, e.getMessage());
        }
    }

    /** Envoi asynchrone d'un e-mail HTML déjà généré (ex. notification du modérateur d'une nouvelle demande). */
    @Async
    public void envoyerHtml(String email, String sujet, String html) {
        try {
            mailService.sendEmail(email, sujet, html);
            log.info("E-mail « {} » sent to {}", sujet, email);
        } catch (Exception e) {
            log.error("E-mail « {} » could not be sent to {}: {}", sujet, email, e.getMessage());
        }
    }

    /** Demande d'accès à une classe acceptée (compte déjà actif) : « Votre demande d'accès … a été acceptée ». */
    @Async
    public void envoyerAccesAccorde(String email, String nomComplet, String classeNom, boolean pourParent) {
        try {
            String html = emailTemplateService.generateAccesClasseAccordeEmail(nomComplet, classeNom, pourParent);
            mailService.sendEmail(email, "Votre demande d'accès à la classe " + classeNom + " a été acceptée", html);
            log.info("Class access granted e-mail sent to {}", email);
        } catch (Exception e) {
            log.error("Class access granted e-mail could not be sent to {}: {}", email, e.getMessage());
        }
    }

    /** Demande d'accès refusée (compte déjà actif), avec le motif éventuel. */
    @Async
    public void envoyerAccesRefuse(String email, String prenom, String nom, String classeId, String classeNom,
                                   String motif) {
        try {
            cmr.notep.interfaces.modeles.Utilisateurs u = new cmr.notep.interfaces.modeles.Utilisateurs();
            u.setPrenom(prenom == null ? "" : prenom);
            u.setNom(nom == null ? "" : nom);
            u.setEmail(email);
            cmr.notep.interfaces.modeles.Classes c = new cmr.notep.interfaces.modeles.Classes();
            c.setId(classeId);
            c.setNom(classeNom);
            String html = emailTemplateService.generateAccessRejectionEmail(u, c, motif);
            mailService.sendEmail(email, "Refus d'accès à la classe " + classeNom, html);
            log.info("Class access rejected e-mail sent to {}", email);
        } catch (Exception e) {
            log.error("Class access rejected e-mail could not be sent to {}: {}", email, e.getMessage());
        }
    }

    @Async
    public void envoyerEnfantRefuse(String email, String nomCompletParent, String enfantNom, String classeNom, String motif) {
        try {
            String html = emailTemplateService.generateEnfantAccesRefuseEmail(nomCompletParent, enfantNom, classeNom, motif);
            mailService.sendEmail(email, "La demande d'inscription de " + enfantNom + " à la classe " + classeNom
                    + " n'a pas été acceptée", html);
            log.info("Child access rejected e-mail sent to {}", email);
        } catch (Exception e) {
            log.error("Child access rejected e-mail could not be sent to {}: {}", email, e.getMessage());
        }
    }

    @Async
    public void envoyerRefus(String email, String nomComplet, String classeNom, String motif) {
        try {
            String html = emailTemplateService.generateInscriptionClasseRefuseeEmail(email, nomComplet, classeNom, motif);
            mailService.sendEmail(email, "Votre demande d'inscription à la classe " + classeNom + " n'a pas été acceptée", html);
            log.info("Class sign-up rejection e-mail sent to {}", email);
        } catch (Exception e) {
            log.error("Class sign-up rejection e-mail could not be sent to {}: {}", email, e.getMessage());
        }
    }
}
