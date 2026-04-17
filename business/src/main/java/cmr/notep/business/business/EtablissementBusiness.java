package cmr.notep.business.business;

import cmr.notep.business.exceptions.SchoolException;
import cmr.notep.business.exceptions.enums.SchoolErrorCode;
import cmr.notep.business.services.EmailTemplateService;
import cmr.notep.business.services.MailService;
import cmr.notep.business.services.NotificationService;
import cmr.notep.business.services.TokenService;
import cmr.notep.interfaces.modeles.Etablissement;
import cmr.notep.interfaces.modeles.Utilisateurs;
import cmr.notep.ressourcesjpa.commun.DaoAccessorService;
import cmr.notep.ressourcesjpa.dao.EtablissementEntity;
import cmr.notep.ressourcesjpa.dao.UtilisateursEntity;
import cmr.notep.ressourcesjpa.repository.EtablissementRepository;
import cmr.notep.ressourcesjpa.repository.UtilisateursRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static cmr.notep.business.config.BusinessConfig.dozerMapperBean;

@Component
@Slf4j

public class EtablissementBusiness {
    private final DaoAccessorService daoAccessorService;
    private final TokenService tokenService;
    private final MailService mailService;
    private final EmailTemplateService emailTemplateService;
    private final NotificationService notificationService;

    public EtablissementBusiness(DaoAccessorService daoAccessorService, TokenService tokenService,
                                  MailService mailService, EmailTemplateService emailTemplateService,
                                  NotificationService notificationService) {
        this.daoAccessorService = daoAccessorService;
        this.tokenService = tokenService;
        this.mailService = mailService;
        this.emailTemplateService = emailTemplateService;
        this.notificationService = notificationService;
    }

    public Etablissement creerEtablissement(Etablissement etablissement) {
            EtablissementEntity entity = dozerMapperBean.map(etablissement, EtablissementEntity.class);
            entity.setId(UUID.randomUUID().toString());
            
            // Handle gestionnaire if provided
            if (etablissement.getGestionnaire() != null && etablissement.getGestionnaire().getId() != null) {
                UtilisateursEntity gestionnaire = daoAccessorService
                        .getRepository(UtilisateursRepository.class)
                        .findById(etablissement.getGestionnaire().getId())
                        .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Gestionnaire introuvable"));
                entity.setGestionnaire(gestionnaire);
            }
            
            // Auto-generate unique code
            entity.setCodeUnique(tokenService.generateUniqueCode());
            
            EtablissementEntity saved = daoAccessorService.getRepository(EtablissementRepository.class).save(entity);
            Etablissement result = mapToEtablissement(saved);
            if (result.getGestionnaire() != null && result.getGestionnaire().getEmail() != null) {
                try {
                    String html = emailTemplateService.generateGestionnaireAjoutEmail(result.getGestionnaire(), result);
                    mailService.sendEmail(result.getGestionnaire().getEmail(),
                            "Vous avez été ajouté comme gestionnaire - " + result.getNom(), html);
                } catch (Exception e) {
                    log.warn("Impossible d'envoyer l'email au gestionnaire: {}", e.getMessage());
                }
                notificationService.createEtablissementCreatedNotification(
                        result.getId(), result.getNom(),
                        result.getGestionnaire().getId(),
                        result.getGestionnaire().getPrenom() + " " + result.getGestionnaire().getNom());
            }
            return result;
    }

    public Etablissement modifierEtablissement(String id, Etablissement etablissementModifie) {
            EtablissementRepository repo = daoAccessorService.getRepository(EtablissementRepository.class);
            EtablissementEntity existing = repo.findById(id)
                    .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Établissement non trouvé avec l'ID: " + id));

            // Update basic fields
            if (etablissementModifie.getNom() != null) {
                existing.setNom(etablissementModifie.getNom());
            }
            if (etablissementModifie.getLocalisation() != null) {
                existing.setLocalisation(etablissementModifie.getLocalisation());
            }
            if (etablissementModifie.getPays() != null) {
                existing.setPays(etablissementModifie.getPays());
            }
            if (etablissementModifie.getEmail() != null) {
                existing.setEmail(etablissementModifie.getEmail());
            }
            if (etablissementModifie.getTelephone() != null) {
                existing.setTelephone(etablissementModifie.getTelephone());
            }
            
            existing.setOptionEnvoiMailNewClasse(etablissementModifie.isOptionEnvoiMailNewClasse());
            existing.setOptionTokenGeneral(etablissementModifie.isOptionTokenGeneral());
            
            // Update code unique if provided, otherwise keep existing
            if (etablissementModifie.getCodeUnique() != null && !etablissementModifie.getCodeUnique().trim().isEmpty()) {
                existing.setCodeUnique(etablissementModifie.getCodeUnique());
            }
            
            // Handle gestionnaire update
            boolean gestionnaireChanged = false;
            if (etablissementModifie.getGestionnaire() != null) {
                if (etablissementModifie.getGestionnaire().getId() != null) {
                    String newGestionnaireId = etablissementModifie.getGestionnaire().getId();
                    boolean alreadySame = existing.getGestionnaire() != null
                            && existing.getGestionnaire().getId().equals(newGestionnaireId);
                    if (!alreadySame) {
                        UtilisateursEntity gestionnaire = daoAccessorService
                                .getRepository(UtilisateursRepository.class)
                                .findById(newGestionnaireId)
                                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Gestionnaire introuvable"));
                        existing.setGestionnaire(gestionnaire);
                        gestionnaireChanged = true;
                    }
                } else {
                    existing.setGestionnaire(null);
                }
            }

            EtablissementEntity updated = repo.save(existing);
            log.info("Etablissement modifié avec succès: {}", updated.getId());
            Etablissement result = mapToEtablissement(updated);
            if (gestionnaireChanged && result.getGestionnaire() != null && result.getGestionnaire().getEmail() != null) {
                try {
                    String html = emailTemplateService.generateGestionnaireAjoutEmail(result.getGestionnaire(), result);
                    mailService.sendEmail(result.getGestionnaire().getEmail(),
                            "Vous avez été ajouté comme gestionnaire - " + result.getNom(), html);
                } catch (Exception e) {
                    log.warn("Impossible d'envoyer l'email au gestionnaire: {}", e.getMessage());
                }
                notificationService.createEtablissementCreatedNotification(
                        result.getId(), result.getNom(),
                        result.getGestionnaire().getId(),
                        result.getGestionnaire().getPrenom() + " " + result.getGestionnaire().getNom());
            }
            return result;
    }

    public void supprimerEtablissement(String id) {
            EtablissementRepository repo = daoAccessorService.getRepository(EtablissementRepository.class);
            if (!repo.existsById(id)) {
                throw new SchoolException(SchoolErrorCode.NOT_FOUND, "Établissement non trouvé avec l'ID: " + id);
            }
            repo.deleteById(id);
    }

    public Etablissement obtenirEtablissementParId(String id) {
            EtablissementRepository repo = daoAccessorService.getRepository(EtablissementRepository.class);
            EtablissementEntity entity = repo.findById(id)
                    .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Établissement non trouvé avec l'ID: " + id));
            return mapToEtablissement(entity);
    }

    public List<Etablissement> obtenirTousLesEtablissements() {
            EtablissementRepository repo = daoAccessorService.getRepository(EtablissementRepository.class);
            return repo.findAll()
                    .stream()
                    .map(this::mapToEtablissement)
                    .collect(Collectors.toList());
    }
    
    private Etablissement mapToEtablissement(EtablissementEntity entity) {
        Etablissement etablissement = dozerMapperBean.map(entity, Etablissement.class);
        
        // Map gestionnaire if exists
        if (entity.getGestionnaire() != null) {
            Utilisateurs gestionnaire = new Utilisateurs();
            gestionnaire.setId(entity.getGestionnaire().getId());
            gestionnaire.setNom(entity.getGestionnaire().getNom());
            gestionnaire.setPrenom(entity.getGestionnaire().getPrenom());
            gestionnaire.setEmail(entity.getGestionnaire().getEmail());
            etablissement.setGestionnaire(gestionnaire);
            etablissement.setGestionnaireId(entity.getGestionnaire().getId());
        }

        return etablissement;
    }
    
    public List<Etablissement> obtenirEtablissementsParGestionnaire(String gestionnaireId) {
        EtablissementRepository repo = daoAccessorService.getRepository(EtablissementRepository.class);
        return repo.findByGestionnaireId(gestionnaireId)
                .stream()
                .map(this::mapToEtablissement)
                .collect(Collectors.toList());
    }
    
    public Utilisateurs obtenirGestionnaireEtablissement(String idEtablissement) {
        EtablissementRepository repo = daoAccessorService.getRepository(EtablissementRepository.class);
        EtablissementEntity entity = repo.findById(idEtablissement)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Établissement non trouvé avec l'ID: " + idEtablissement));
        
        if (entity.getGestionnaire() == null) {
            throw new SchoolException(SchoolErrorCode.NOT_FOUND, "Aucun gestionnaire assigné à cet établissement");
        }
        
        return dozerMapperBean.map(entity.getGestionnaire(), Utilisateurs.class);
    }

    private String generateToken(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder token = new StringBuilder();
        for (int i = 0; i < length; i++) {
            token.append(chars.charAt(random.nextInt(chars.length())));
        }
        return token.toString();
    }



    public void approuverClasseParEtablissement(String classeId, String etablissementId) {
        // This will be implemented in ClassesBusiness but called from here
        log.info("Approbation de la classe {} par l'établissement {}", classeId, etablissementId);
    }

}
