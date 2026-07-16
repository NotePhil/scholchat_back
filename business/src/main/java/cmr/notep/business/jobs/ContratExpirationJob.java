package cmr.notep.business.jobs;

import cmr.notep.business.business.ClasseSuppressionBusiness;
import cmr.notep.business.business.ContratBusiness;
import cmr.notep.business.services.ContratEmailService;
import cmr.notep.business.services.NotificationService;
import cmr.notep.modele.EtatClasse;
import cmr.notep.modele.StatutContrat;
import cmr.notep.ressourcesjpa.dao.ClassesEntity;
import cmr.notep.ressourcesjpa.dao.ContratEntity;
import cmr.notep.ressourcesjpa.dao.EtablissementEntity;
import cmr.notep.ressourcesjpa.repository.ClassesRepository;
import cmr.notep.ressourcesjpa.repository.ContratRepository;
import cmr.notep.ressourcesjpa.repository.EtablissementRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Verifie periodiquement les contrats/offres (classes independantes et etablissements) :
 * - desactive automatiquement ceux qui viennent d'expirer, et avertit par email ceux qui
 *   arrivent bientot a expiration (3 jours avant) ;
 * - avertit (in-app + email) et supprime definitivement (classe ou etablissement, en cascade)
 *   ceux dont l'offre est expiree depuis plus longtemps que le delai configure par l'admin sur
 *   l'offre elle-meme (OffreEntity.delaiRappelSuppressionMinutes / delaiSuppressionMinutes) -
 *   ces delais sont modifiables a tout moment depuis l'ecran d'administration des offres.
 *   S'applique aussi aux contrats jamais payes (EN_ATTENTE_PAIEMENT), a partir de leur creation.
 * Tourne toutes les minutes : necessaire pour que l'offre de test (rappel a 1 min, suppression a
 * 2 min apres expiration) traverse bien les deux etapes separement plutot que de sauter directement
 * a la suppression si le cycle etait plus espace que l'ecart entre ces deux delais.
 */
@Component
@Slf4j
public class ContratExpirationJob {

    private static final int JOURS_AVANT_RAPPEL_EXPIRATION = 3;

    private final ContratRepository contratRepository;
    private final ClassesRepository classesRepository;
    private final EtablissementRepository etablissementRepository;
    private final ContratBusiness contratBusiness;
    private final ContratEmailService contratEmailService;
    private final NotificationService notificationService;
    private final ClasseSuppressionBusiness classeSuppressionBusiness;

    public ContratExpirationJob(ContratRepository contratRepository, ClassesRepository classesRepository,
                                 EtablissementRepository etablissementRepository, ContratBusiness contratBusiness,
                                 ContratEmailService contratEmailService, NotificationService notificationService,
                                 ClasseSuppressionBusiness classeSuppressionBusiness) {
        this.contratRepository = contratRepository;
        this.classesRepository = classesRepository;
        this.etablissementRepository = etablissementRepository;
        this.contratBusiness = contratBusiness;
        this.contratEmailService = contratEmailService;
        this.notificationService = notificationService;
        this.classeSuppressionBusiness = classeSuppressionBusiness;
    }

    @Scheduled(cron = "0 * * * * ?") // Toutes les minutes
    @Transactional
    public void verifierExpirations() {
        LocalDateTime now = LocalDateTime.now();
        traiterContratsExpires(now);
        traiterRappelsAvantExpiration(now);
        traiterPurgeApresExpiration(now);
    }

    private void traiterContratsExpires(LocalDateTime now) {
        List<ContratEntity> expires = contratRepository.findByStatutAndDateFinBefore(StatutContrat.ACTIF, now);
        if (expires.isEmpty()) return;
        log.info("ContratExpirationJob: {} contrat(s) venant d'expirer", expires.size());

        for (ContratEntity contrat : expires) {
            contrat.setStatut(StatutContrat.EXPIRE);
            contratRepository.save(contrat);

            String emailDestinataire = null;
            String userId = null;
            String nomEntite = null;
            String entiteId = null;
            String entiteType = null;

            if (contrat.getClasse() != null) {
                ClassesEntity classe = contrat.getClasse();
                contratBusiness.desactiverClassePourExpiration(classe);
                if (classe.getModerator() != null) {
                    emailDestinataire = classe.getModerator().getEmail();
                    userId = classe.getModerator().getId();
                }
                nomEntite = classe.getNom();
                entiteId = classe.getId();
                entiteType = "CLASSE";
            } else if (contrat.getEtablissement() != null) {
                EtablissementEntity etablissement = contrat.getEtablissement();
                String etablissementId = etablissement.getId();
                List<ClassesEntity> classesActives = classesRepository.findByEtablissementIdAndEtat(etablissementId, EtatClasse.ACTIF);
                for (ClassesEntity classe : classesActives) {
                    contratBusiness.desactiverClassePourExpiration(classe);
                }
                etablissement.setExpireParOffre(true);
                etablissementRepository.save(etablissement);
                if (etablissement.getGestionnaire() != null) {
                    emailDestinataire = etablissement.getGestionnaire().getEmail();
                    userId = etablissement.getGestionnaire().getId();
                }
                nomEntite = etablissement.getNom();
                entiteId = etablissement.getId();
                entiteType = "ETABLISSEMENT";
                log.info("Établissement {} : {} classe(s) désactivée(s) suite à l'expiration du forfait", etablissementId, classesActives.size());
            }

            contratEmailService.sendOffreExpireeEmail(contrat, emailDestinataire);
            if (userId != null) {
                notificationService.createOffreExpireeNotification(userId, nomEntite, entiteId, entiteType);
            }
        }
    }

    private void traiterRappelsAvantExpiration(LocalDateTime now) {
        LocalDateTime seuil = now.plusDays(JOURS_AVANT_RAPPEL_EXPIRATION);
        List<ContratEntity> bientotExpires = contratRepository.findByStatutAndDateFinBetween(StatutContrat.ACTIF, now, seuil);
        for (ContratEntity contrat : bientotExpires) {
            LocalDateTime derniereNotif = contrat.getDateDerniereNotificationExpiration();
            if (derniereNotif != null && derniereNotif.isAfter(now.minusHours(24))) {
                continue; // deja notifie recemment
            }
            String emailDestinataire = null;
            String userId = null;
            String nomEntite = null;
            String entiteId = null;
            String entiteType = null;
            if (contrat.getClasse() != null) {
                ClassesEntity classe = contrat.getClasse();
                nomEntite = classe.getNom();
                entiteId = classe.getId();
                entiteType = "CLASSE";
                if (classe.getModerator() != null) {
                    emailDestinataire = classe.getModerator().getEmail();
                    userId = classe.getModerator().getId();
                }
            } else if (contrat.getEtablissement() != null) {
                EtablissementEntity etab = contrat.getEtablissement();
                nomEntite = etab.getNom();
                entiteId = etab.getId();
                entiteType = "ETABLISSEMENT";
                if (etab.getGestionnaire() != null) {
                    emailDestinataire = etab.getGestionnaire().getEmail();
                    userId = etab.getGestionnaire().getId();
                }
            }
            contratEmailService.sendOffreExpirationBientotEmail(contrat, emailDestinataire);
            if (userId != null) {
                notificationService.createOffreExpirationBientotNotification(userId, nomEntite, entiteId, entiteType);
            }
            contrat.setDateDerniereNotificationExpiration(now);
            contratRepository.save(contrat);
        }
    }

    /**
     * Purge automatique : pour chaque contrat EXPIRE (depuis dateFin) ou jamais paye
     * (EN_ATTENTE_PAIEMENT, depuis dateCreation), compare le temps ecoule aux delais configures
     * sur l'offre. Envoie un rappel de suppression imminente une seule fois, puis supprime
     * definitivement (classe ou etablissement, en cascade) une fois le delai de suppression atteint.
     * Une offre sans delai configure (champ nul) n'est jamais purgee automatiquement.
     */
    private void traiterPurgeApresExpiration(LocalDateTime now) {
        List<ContratEntity> candidats = new ArrayList<>();
        candidats.addAll(contratRepository.findByStatut(StatutContrat.EXPIRE));
        candidats.addAll(contratRepository.findByStatut(StatutContrat.EN_ATTENTE_PAIEMENT));

        for (ContratEntity contrat : candidats) {
            if (contrat.getOffre() == null) continue;
            Long delaiRappel = contrat.getOffre().getDelaiRappelSuppressionMinutes();
            Long delaiSuppression = contrat.getOffre().getDelaiSuppressionMinutes();
            if (delaiRappel == null && delaiSuppression == null) continue;

            LocalDateTime pointDepart = contrat.getStatut() == StatutContrat.EXPIRE
                    ? contrat.getDateFin()
                    : contrat.getDateCreation();
            if (pointDepart == null) continue;

            if (delaiSuppression != null && !now.isBefore(pointDepart.plusMinutes(delaiSuppression))) {
                supprimerDefinitivement(contrat);
                continue;
            }

            if (delaiRappel != null && contrat.getDateRappelSuppressionEnvoye() == null
                    && !now.isBefore(pointDepart.plusMinutes(delaiRappel))) {
                envoyerRappelSuppression(contrat, delaiSuppression != null ? pointDepart.plusMinutes(delaiSuppression) : null);
                contrat.setDateRappelSuppressionEnvoye(now);
                contratRepository.save(contrat);
            }
        }
    }

    private void envoyerRappelSuppression(ContratEntity contrat, LocalDateTime dateSuppressionPrevue) {
        String emailDestinataire = null;
        String userId = null;
        String nomEntite = null;
        String entiteId = null;
        String entiteType = null;

        if (contrat.getClasse() != null) {
            ClassesEntity classe = contrat.getClasse();
            nomEntite = classe.getNom();
            entiteId = classe.getId();
            entiteType = "CLASSE";
            if (classe.getModerator() != null) {
                emailDestinataire = classe.getModerator().getEmail();
                userId = classe.getModerator().getId();
            }
        } else if (contrat.getEtablissement() != null) {
            EtablissementEntity etab = contrat.getEtablissement();
            nomEntite = etab.getNom();
            entiteId = etab.getId();
            entiteType = "ETABLISSEMENT";
            if (etab.getGestionnaire() != null) {
                emailDestinataire = etab.getGestionnaire().getEmail();
                userId = etab.getGestionnaire().getId();
            }
        }

        contratEmailService.sendSuppressionImminenteEmail(contrat, emailDestinataire, dateSuppressionPrevue);
        if (userId != null) {
            notificationService.createSuppressionImminenteNotification(userId, nomEntite, entiteId, entiteType);
        }
        log.info("Rappel de suppression imminente envoyé pour {} {}", entiteType, entiteId);
    }

    private void supprimerDefinitivement(ContratEntity contrat) {
        try {
            String emailDestinataire = null;
            String nomEntite;
            String offreNom = contrat.getOffre().getNom();

            if (contrat.getClasse() != null) {
                ClassesEntity classe = contrat.getClasse();
                nomEntite = classe.getNom();
                if (classe.getModerator() != null) emailDestinataire = classe.getModerator().getEmail();
                String classeId = classe.getId();
                contratEmailService.sendEntiteSupprimeeEmail(emailDestinataire, "la classe " + nomEntite, offreNom);
                classeSuppressionBusiness.supprimerClasseDefinitivement(classeId);
            } else if (contrat.getEtablissement() != null) {
                EtablissementEntity etab = contrat.getEtablissement();
                nomEntite = etab.getNom();
                if (etab.getGestionnaire() != null) emailDestinataire = etab.getGestionnaire().getEmail();
                String etablissementId = etab.getId();
                contratEmailService.sendEntiteSupprimeeEmail(emailDestinataire, "l'établissement " + nomEntite, offreNom);
                classeSuppressionBusiness.supprimerEtablissementDefinitivement(etablissementId);
            }
        } catch (Exception e) {
            log.error("Erreur lors de la suppression définitive du contrat {}: {}", contrat.getId(), e.getMessage(), e);
        }
    }
}
