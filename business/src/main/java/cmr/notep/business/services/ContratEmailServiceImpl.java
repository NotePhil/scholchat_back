package cmr.notep.business.services;

import cmr.notep.business.exceptions.SchoolErrorEmail;
import cmr.notep.business.exceptions.enums.SchoolErrorCode;
import cmr.notep.ressourcesjpa.dao.ContratEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ContratEmailServiceImpl implements ContratEmailService {
    private final MailServiceInterface mailService;
    private final EmailTemplateService emailTemplateService;

    public ContratEmailServiceImpl(MailServiceInterface mailService, EmailTemplateService emailTemplateService) {
        this.mailService = mailService;
        this.emailTemplateService = emailTemplateService;
    }

    private String nomCible(ContratEntity contrat) {
        if (contrat.getClasse() != null) {
            return "la classe " + contrat.getClasse().getNom();
        }
        if (contrat.getEtablissement() != null) {
            return "l'établissement " + contrat.getEtablissement().getNom();
        }
        return "votre compte";
    }

    private String emailDestinataireParDefaut(ContratEntity contrat) {
        if (contrat.getClasse() != null && contrat.getClasse().getModerator() != null) {
            return contrat.getClasse().getModerator().getEmail();
        }
        if (contrat.getEtablissement() != null && contrat.getEtablissement().getGestionnaire() != null) {
            return contrat.getEtablissement().getGestionnaire().getEmail();
        }
        return null;
    }

    @Override
    public void sendConfirmationSouscriptionEmail(ContratEntity contrat) {
        String email = emailDestinataireParDefaut(contrat);
        if (email == null) {
            log.info("Aucun destinataire pour l'email de confirmation du contrat {}", contrat.getId());
            return;
        }
        try {
            String html = emailTemplateService.generateContratConfirmationEmail(
                    nomCible(contrat), contrat.getOffre().getNom(), contrat.getPeriodicite().name(),
                    contrat.getPrixPaye(), contrat.getDateFin());
            mailService.sendEmail(email, "Offre activée - " + contrat.getOffre().getNom(), html);
            log.info("Email de confirmation de souscription envoyé à {}", email);
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email de confirmation de souscription: {}", e.getMessage());
            throw new SchoolErrorEmail(SchoolErrorCode.EMAIL_ERROR, "Échec de l'envoi de l'email de confirmation", e);
        }
    }

    @Override
    public void sendOffreExpirationBientotEmail(ContratEntity contrat, String emailDestinataire) {
        if (emailDestinataire == null) return;
        try {
            String html = emailTemplateService.generateOffreExpirationBientotEmail(
                    nomCible(contrat), contrat.getOffre().getNom(), contrat.getDateFin());
            mailService.sendEmail(emailDestinataire, "Votre offre expire bientôt - " + contrat.getOffre().getNom(), html);
            log.info("Email d'expiration prochaine envoyé à {}", emailDestinataire);
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email d'expiration prochaine: {}", e.getMessage());
        }
    }

    @Override
    public void sendOffreExpireeEmail(ContratEntity contrat, String emailDestinataire) {
        if (emailDestinataire == null) return;
        try {
            String html = emailTemplateService.generateOffreExpireeEmail(nomCible(contrat), contrat.getOffre().getNom());
            mailService.sendEmail(emailDestinataire, "Votre offre a expiré - " + contrat.getOffre().getNom(), html);
            log.info("Email d'expiration envoyé à {}", emailDestinataire);
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email d'expiration: {}", e.getMessage());
        }
    }

    @Override
    public void sendRenouvellementLienEmail(String email, String nomCible, String entityType, String entityId, String token) {
        if (email == null) return;
        try {
            String html = emailTemplateService.generateRenouvellementLienEmail(nomCible, entityType, entityId, token);
            mailService.sendEmail(email, "Renouvellement de votre offre - " + nomCible, html);
            log.info("Email de lien de renouvellement envoyé à {}", email);
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email de renouvellement: {}", e.getMessage());
            throw new SchoolErrorEmail(SchoolErrorCode.EMAIL_ERROR, "Échec de l'envoi de l'email de renouvellement", e);
        }
    }

    @Override
    public void sendSuppressionImminenteEmail(ContratEntity contrat, String emailDestinataire, java.time.LocalDateTime dateSuppressionPrevue) {
        if (emailDestinataire == null) return;
        try {
            String html = emailTemplateService.generateSuppressionImminenteEmail(
                    nomCible(contrat), contrat.getOffre().getNom(), dateSuppressionPrevue);
            mailService.sendEmail(emailDestinataire, "Suppression imminente - " + contrat.getOffre().getNom(), html);
            log.info("Email de suppression imminente envoyé à {}", emailDestinataire);
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email de suppression imminente: {}", e.getMessage());
        }
    }

    @Override
    public void sendEntiteSupprimeeEmail(String email, String nomCible, String offreNom) {
        if (email == null) return;
        try {
            String html = emailTemplateService.generateEntiteSupprimeeEmail(nomCible, offreNom);
            mailService.sendEmail(email, "Suppression définitive - " + offreNom, html);
            log.info("Email de suppression définitive envoyé à {}", email);
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email de suppression définitive: {}", e.getMessage());
        }
    }
}
