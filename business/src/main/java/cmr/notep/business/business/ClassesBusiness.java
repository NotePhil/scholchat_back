package cmr.notep.business.business;

import cmr.notep.business.exceptions.SchoolException;
import cmr.notep.business.exceptions.enums.SchoolErrorCode;
import cmr.notep.business.services.EmailTemplateService;
import cmr.notep.business.services.MailService;
import cmr.notep.business.services.TokenService;
import cmr.notep.business.services.PaymentService;
import cmr.notep.interfaces.dto.ClasseCreationDto;
import cmr.notep.interfaces.dto.ClasseCreationResponseDto;
import cmr.notep.interfaces.modeles.*;
import cmr.notep.ressourcesjpa.commun.DaoAccessorService;
import cmr.notep.ressourcesjpa.dao.*;
import cmr.notep.ressourcesjpa.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import cmr.notep.modele.DroitPublication;
import cmr.notep.modele.EtatClasse;
import org.springframework.transaction.annotation.Transactional;
import jakarta.mail.MessagingException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static cmr.notep.business.config.BusinessConfig.dozerMapperBean;

@Component
@Slf4j
@Transactional
public class ClassesBusiness {

    private final DaoAccessorService daoAccessorService;
    private final MailService mailService;
    private final EmailTemplateService emailTemplateService;
    private final TokenService tokenService;
    private final PaymentService paymentService;

    public ClassesBusiness(DaoAccessorService daoAccessorService, MailService mailService, 
                          EmailTemplateService emailTemplateService, TokenService tokenService,
                          PaymentService paymentService) {
        this.daoAccessorService = daoAccessorService;
        this.mailService = mailService;
        this.emailTemplateService = emailTemplateService;
        this.tokenService = tokenService;
        this.paymentService = paymentService;
    }

    public ClasseCreationResponseDto creerNouvelleClasse(ClasseCreationDto classeDto) throws SchoolException {
        log.info("Creating new class with data: {}", classeDto);
        
        ClassesEntity classesEntity = new ClassesEntity();
        classesEntity.setId(UUID.randomUUID().toString());
        classesEntity.setNom(classeDto.getNom());
        classesEntity.setNiveau(classeDto.getNiveau());
        classesEntity.setDateCreation(new java.util.Date());
        classesEntity.setCodeActivation(generateActivationCode());
        
        String token = null;
        EtablissementEntity etablissement = null;
        
        if (classeDto.getEtablissementId() != null && !classeDto.getEtablissementId().trim().isEmpty()) {
            etablissement = daoAccessorService.getRepository(EtablissementRepository.class)
                    .findById(classeDto.getEtablissementId())
                    .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Établissement introuvable"));
            
            if (etablissement.isOptionTokenGeneral()) {
                if (classeDto.getCodeUnique() == null || classeDto.getCodeUnique().trim().isEmpty()) {
                    throw new SchoolException(SchoolErrorCode.INVALID_INPUT, "Code unique requis pour cet établissement");
                }
                if (!etablissement.getCodeUnique().equals(classeDto.getCodeUnique())) {
                    throw new SchoolException(SchoolErrorCode.UNAUTHORIZED, "Code unique invalide");
                }
            }
            
            classesEntity.setEtablissement(etablissement);
            classesEntity.setEtat(EtatClasse.EN_ATTENTE_APPROBATION);
            classesEntity.setPaymentRequired(false);
            token = tokenService.generateClassToken();
            
        } else {
            if (classeDto.getPaymentInfo() == null) {
                throw new SchoolException(SchoolErrorCode.INVALID_INPUT, "Informations de paiement requises");
            }
            
            boolean paymentSuccess = paymentService.processPayment(classeDto.getPaymentInfo());
            if (!paymentSuccess) {
                throw new SchoolException(SchoolErrorCode.PAYMENT_FAILED, "Échec du paiement");
            }
            
            classesEntity.setEtat(EtatClasse.ACTIF);
            classesEntity.setPaymentRequired(true);
            token = tokenService.generateClassToken();
        }
        
        if (classeDto.getModeratorId() != null && !classeDto.getModeratorId().trim().isEmpty()) {
            ProfesseursEntity moderator = daoAccessorService.getRepository(ProfesseursRepository.class)
                    .findById(classeDto.getModeratorId())
                    .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Modérateur introuvable"));
            classesEntity.setModerator(moderator);
        }
        
        ClassesEntity savedEntity = daoAccessorService.getRepository(ClassesRepository.class).save(classesEntity);
        
        if (etablissement != null && etablissement.isOptionEnvoiMailNewClasse() && etablissement.getEmail() != null) {
            sendApprovalRequestEmail(savedEntity, etablissement);
        }
        
        Classes classeResponse = dozerMapperBean.map(savedEntity, Classes.class);
        classeResponse.setDateCreation(savedEntity.getDateCreation());
        
        return ClasseCreationResponseDto.builder()
                .classe(classeResponse)
                .token(token)
                .etat(savedEntity.getEtat())
                .paymentRequired(savedEntity.isPaymentRequired())
                .message(etablissement != null ? "Classe créée en attente de validation" : "Classe créée et activée")
                .build();
    }

    public Classes creerClasse(Classes classes) throws SchoolException {
        log.info("Creating class with data: {}", classes);
        ClassesEntity classesEntity = dozerMapperBean.map(classes, ClassesEntity.class);
        classesEntity.setId(UUID.randomUUID().toString());

        EtablissementEntity etablissement = null;
        if (classes.getEtablissement() != null && 
            classes.getEtablissement().getId() != null && 
            !classes.getEtablissement().getId().trim().isEmpty()) {
            etablissement = daoAccessorService.getRepository(EtablissementRepository.class)
                    .findById(classes.getEtablissement().getId())
                    .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Établissement introuvable"));
            
            if (etablissement.isOptionTokenGeneral()) {
                validateEtablissementToken(etablissement, classes.getEtablissementToken());
            }
            
            classesEntity.setEtablissement(etablissement);
            classesEntity.setPaymentRequired(false);
        } else {
            classesEntity.setPaymentRequired(true);
        }

        if (classesEntity.getEtat() == null) {
            if (etablissement == null) {
                classesEntity.setEtat(EtatClasse.EN_ATTENTE_APPROBATION);
            } else {
                classesEntity.setEtat(EtatClasse.EN_ATTENTE_APPROBATION);
            }
        }

        // Generate a random activation code if not provided
        if (classesEntity.getCodeActivation() == null) {
            classesEntity.setCodeActivation(generateActivationCode());
        }

        // Handle moderator if provided
        if (classes.getModerator() != null && classes.getModerator().getId() != null) {
            log.info("Assigning moderator with ID: {}", classes.getModerator().getId());
            ProfesseursEntity moderator = daoAccessorService
                    .getRepository(ProfesseursRepository.class)
                    .findById(classes.getModerator().getId())
                    .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Modérateur introuvable"));

            classesEntity.setModerator(moderator);
            moderator.getModeratedClasses().add(classesEntity);
            daoAccessorService.getRepository(ProfesseursRepository.class).save(moderator);
            log.info("Moderator assigned successfully");
        } else {
            classesEntity.setModerator(null);
            log.info("No moderator assigned");
        }

        ClassesEntity savedEntity = daoAccessorService.getRepository(ClassesRepository.class)
                .save(classesEntity);

        // Send approval email if needed (only when establishment exists)
        if (etablissement != null) {
            handleClassCreationEmail(savedEntity, etablissement);
        } else {
            log.info("No establishment provided - class created with payment requirement");
        }

        // Map back to return proper response with moderator ID if exists
        Classes result = dozerMapperBean.map(savedEntity, Classes.class);
        result.setDateCreation(savedEntity.getDateCreation());
        if (savedEntity.getModerator() != null) {
            Professeurs moderator = new Professeurs();
            moderator.setId(savedEntity.getModerator().getId());
            result.setModerator(moderator);
        }

        log.info("Class created successfully: {}", result);
        return result;
    }
    public Classes modifierClasse(String idClasse, Classes classeModifiee) throws SchoolException {
        ClassesRepository classesRepository = daoAccessorService.getRepository(ClassesRepository.class);

        ClassesEntity classeExistante = classesRepository.findById(idClasse)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Classe non trouvée avec l'ID: " + idClasse));

        // Handle moderator update - FIXED VERSION
        updateModerator(classeExistante, classeModifiee);

        // Basic field updates
        if (classeModifiee.getNom() != null) {
            classeExistante.setNom(classeModifiee.getNom());
        }
        if (classeModifiee.getNiveau() != null) {
            classeExistante.setNiveau(classeModifiee.getNiveau());
        }
        if (classeModifiee.getEtat() != null) {
            classeExistante.setEtat(classeModifiee.getEtat());
        }
        if (classeModifiee.getDateCreation() != null) {
            classeExistante.setDateCreation(classeModifiee.getDateCreation());
        }
        if (classeModifiee.getCodeActivation() != null) {
            classeExistante.setCodeActivation(classeModifiee.getCodeActivation());
        }
        if (classeModifiee.getDroitPublication() != null) {
            classeExistante.setDroitPublication(classeModifiee.getDroitPublication());
        }

        // Etablissement Update
        if (classeModifiee.getEtablissement() != null) {
            EtablissementEntity etablissement = daoAccessorService
                    .getRepository(EtablissementRepository.class)
                    .findById(classeModifiee.getEtablissement().getId())
                    .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Établissement introuvable"));
            classeExistante.setEtablissement(etablissement);
        }


        ClassesEntity classeSauvegardee = classesRepository.save(classeExistante);
        log.info("Classe modifiée avec succès: {}", idClasse);
        return dozerMapperBean.map(classeSauvegardee, Classes.class);
    }

    /**
     * Handles moderator assignment, update, and removal logic
     */
    private void updateModerator(ClassesEntity classeExistante, Classes classeModifiee) throws SchoolException {
        ProfesseursRepository professeurRepository = daoAccessorService.getRepository(ProfesseursRepository.class);

        // Case 1: New moderator is provided
        if (classeModifiee.getModerator() != null && classeModifiee.getModerator().getId() != null) {
            String newModeratorId = classeModifiee.getModerator().getId();

            // Check if it's the same moderator (no change needed)
            if (classeExistante.getModerator() != null &&
                    classeExistante.getModerator().getId().equals(newModeratorId)) {
                log.info("Le même modérateur est déjà assigné à la classe");
                return;
            }

            // Remove old moderator if exists
            if (classeExistante.getModerator() != null) {
                ProfesseursEntity oldModerator = classeExistante.getModerator();
                oldModerator.getModeratedClasses().remove(classeExistante);
                professeurRepository.save(oldModerator);
                log.info("Ancien modérateur {} retiré de la classe", oldModerator.getId());
            }

            // Assign new moderator
            ProfesseursEntity newModerator = professeurRepository
                    .findById(newModeratorId)
                    .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND,
                            "Modérateur introuvable avec l'ID: " + newModeratorId));

            classeExistante.setModerator(newModerator);
            newModerator.getModeratedClasses().add(classeExistante);
            professeurRepository.save(newModerator);
            log.info("Nouveau modérateur {} assigné à la classe", newModeratorId);
        }
        // Case 2: Moderator is explicitly set to null (remove moderator)
        else if (classeModifiee.getModerator() != null && classeModifiee.getModerator().getId() == null) {
            if (classeExistante.getModerator() != null) {
                ProfesseursEntity oldModerator = classeExistante.getModerator();
                oldModerator.getModeratedClasses().remove(classeExistante);
                professeurRepository.save(oldModerator);
                classeExistante.setModerator(null);
                log.info("Modérateur retiré de la classe");
            }
        }
        // Case 3: Moderator field is not provided in update (no change)
        // Do nothing - keep existing moderator
    }

    /**
     * Assigns a moderator to a class
     */
    public Classes assignerModerator(String idClasse, String idModerator) throws SchoolException {
        ClassesRepository classesRepository = daoAccessorService.getRepository(ClassesRepository.class);
        ProfesseursRepository professeurRepository = daoAccessorService.getRepository(ProfesseursRepository.class);

        ClassesEntity classe = classesRepository.findById(idClasse)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Classe non trouvée"));

        ProfesseursEntity moderator = professeurRepository.findById(idModerator)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Modérateur introuvable"));

        // Add to many-to-many table (this allows multiple moderators)
        professeurRepository.addModeratorToClass(idModerator, idClasse);
        
        // If no main moderator exists, set this as the main moderator
        if (classe.getModerator() == null) {
            classe.setModerator(moderator);
            moderator.getModeratedClasses().add(classe);
            professeurRepository.save(moderator);
            classesRepository.save(classe);
        }

        log.info("Modérateur {} assigné à la classe {}", idModerator, idClasse);
        return dozerMapperBean.map(classe, Classes.class);
    }

    /**
     * Removes moderator from a class
     */
    public Classes retirerModerator(String idClasse) throws SchoolException {
        ClassesRepository classesRepository = daoAccessorService.getRepository(ClassesRepository.class);
        ProfesseursRepository professeurRepository = daoAccessorService.getRepository(ProfesseursRepository.class);

        ClassesEntity classe = classesRepository.findById(idClasse)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Classe non trouvée"));

        if (classe.getModerator() != null) {
            String moderatorId = classe.getModerator().getId();
            
            // Remove from many-to-many table
            professeurRepository.removeModeratorFromClass(moderatorId, idClasse);
            
            // Remove as main moderator
            ProfesseursEntity moderator = classe.getModerator();
            moderator.getModeratedClasses().remove(classe);
            professeurRepository.save(moderator);
            classe.setModerator(null);

            ClassesEntity saved = classesRepository.save(classe);
            log.info("Modérateur retiré de la classe {}", idClasse);
            return dozerMapperBean.map(saved, Classes.class);
        } else {
            throw new SchoolException(SchoolErrorCode.INVALID_STATE, "Aucun modérateur assigné à cette classe");
        }
    }

    /**
     * Approves a pending class (changes status from EN_ATTENTE_APPROBATION to ACTIF)
     */
    public Classes approuverClasse(String idClasse) throws SchoolException {
        ClassesRepository classesRepository = daoAccessorService.getRepository(ClassesRepository.class);
        ClassesEntity classe = classesRepository.findById(idClasse)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Classe non trouvée"));

        if (classe.getEtat() != EtatClasse.EN_ATTENTE_APPROBATION) {
            throw new SchoolException(SchoolErrorCode.INVALID_STATE,
                    "Seules les classes en attente peuvent être approuvées");
        }

        classe.setEtat(EtatClasse.ACTIF);
        ClassesEntity saved = classesRepository.save(classe);
        return dozerMapperBean.map(saved, Classes.class);
    }

    /**
     * Rejects a pending class (changes status from EN_ATTENTE_APPROBATION to INACTIF)
     */
    public Classes rejeterClasse(String idClasse, String motif) throws SchoolException {
        ClassesRepository classesRepository = daoAccessorService.getRepository(ClassesRepository.class);
        ClassesEntity classe = classesRepository.findById(idClasse)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Classe non trouvée"));

        if (classe.getEtat() != EtatClasse.EN_ATTENTE_APPROBATION) {
            throw new SchoolException(SchoolErrorCode.INVALID_STATE,
                    "Seules les classes en attente peuvent être rejetées");
        }

        classe.setEtat(EtatClasse.INACTIF);
        // You might want to store the rejection reason in a separate table
        ClassesEntity saved = classesRepository.save(classe);
        return dozerMapperBean.map(saved, Classes.class);
    }

    public void supprimerClasse(String idClasse) throws SchoolException {
        ClassesRepository classesRepository = daoAccessorService.getRepository(ClassesRepository.class);
        if (!classesRepository.existsById(idClasse)) {
            throw new SchoolException(SchoolErrorCode.NOT_FOUND, "Classe non trouvée avec l'ID: " + idClasse);
        }
        classesRepository.deleteById(idClasse);
        log.info("Classe supprimée avec succès: {}", idClasse);
    }

    public Classes obtenirClasseParId(String idClasse) throws SchoolException {
        ClassesRepository classesRepository = daoAccessorService.getRepository(ClassesRepository.class);
        ClassesEntity classeEntity = classesRepository.findById(idClasse)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Classe non trouvée avec l'ID: " + idClasse));

        Classes classe = dozerMapperBean.map(classeEntity, Classes.class);

        // Map moderated classes to just IDs to prevent circular references
        if (classeEntity.getModerator() != null) {
            Professeurs moderator = new Professeurs();
            moderator.setId(classeEntity.getModerator().getId());
            moderator.setNom(classeEntity.getModerator().getNom());
            moderator.setPrenom(classeEntity.getModerator().getPrenom());
            classe.setModerator(moderator);
        }

        return classe;
    }

    /**
     * Gets all classes with a specific status
     */
    public List<Classes> obtenirClassesParEtat(EtatClasse etat) throws SchoolException {
        ClassesRepository classesRepository = daoAccessorService.getRepository(ClassesRepository.class);
        if (etat == null) {
            return classesRepository.findAll()
                    .stream()
                    .map(c -> dozerMapperBean.map(c, Classes.class))
                    .collect(Collectors.toList());
        }
        return classesRepository.findByEtat(etat)
                .stream()
                .map(c -> dozerMapperBean.map(c, Classes.class))
                .collect(Collectors.toList());
    }

    public List<Classes> obtenirToutesLesClasses() throws SchoolException {
        ClassesRepository classesRepository = daoAccessorService.getRepository(ClassesRepository.class);
        return classesRepository.findAll()
                .stream()
                .map(c -> dozerMapperBean.map(c, Classes.class))
                .collect(Collectors.toList());
    }

    public Classes modifierDroitPublication(String idClasse, DroitPublication droitPublication) throws SchoolException {
        ClassesRepository classesRepository = daoAccessorService.getRepository(ClassesRepository.class);
        ClassesEntity classeExistante = classesRepository.findById(idClasse)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Classe non trouvée avec l'ID: " + idClasse));

        classeExistante.setDroitPublication(droitPublication);
        ClassesEntity updatedEntity = classesRepository.save(classeExistante);
        log.info("Droit de publication modifié pour la classe: {}", idClasse);
        return dozerMapperBean.map(updatedEntity, Classes.class);
    }

    private String generateActivationCode() {
        return String.format("%06d", new java.util.Random().nextInt(999999));
    }

    private void validateEtablissementToken(EtablissementEntity etablissement, String providedToken) {
        if (providedToken == null || providedToken.trim().isEmpty()) {
            throw new SchoolException(SchoolErrorCode.INVALID_INPUT, 
                "Code unique requis pour cet établissement");
        }
        
        if (etablissement.getCodeUnique() == null || !etablissement.getCodeUnique().equals(providedToken)) {
            throw new SchoolException(SchoolErrorCode.UNAUTHORIZED, 
                "Code unique invalide pour cet établissement");
        }
    }

    private void handleClassCreationEmail(ClassesEntity classe, EtablissementEntity etablissement) {
        try {
            if (etablissement.isOptionEnvoiMailNewClasse() && 
                etablissement.getEmail() != null) {
                sendApprovalRequestEmail(classe, etablissement);
            }
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email: {}", e.getMessage());
        }
    }

    private void sendClassCreationNotificationEmail(ClassesEntity classe, EtablissementEntity etablissement) {
        try {
            Classes classeDto = dozerMapperBean.map(classe, Classes.class);
            Etablissement etablissementDto = dozerMapperBean.map(etablissement, Etablissement.class);
            
            String htmlContent = emailTemplateService.generateClassCreationNotificationEmail(
                classeDto, etablissementDto);
            
            mailService.sendEmail(etablissement.getEmail(), 
                "Nouvelle classe créée - " + classe.getNom(), htmlContent);
            
            log.info("Email de notification de création envoyé à: {}", etablissement.getEmail());
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email de notification: {}", e.getMessage());
        }
    }

    private void sendApprovalRequestEmail(ClassesEntity classe, EtablissementEntity etablissement) {
        try {
            Classes classeDto = dozerMapperBean.map(classe, Classes.class);
            Etablissement etablissementDto = dozerMapperBean.map(etablissement, Etablissement.class);
            
            String htmlContent = emailTemplateService.generateClassApprovalRequestEmail(
                classeDto, etablissementDto, classe.getId(), etablissement.getId());
            
            mailService.sendEmail(etablissement.getEmail(), 
                "Demande d'approbation de classe - " + classe.getNom(), htmlContent);
            
            log.info("Email d'approbation envoyé à: {}", etablissement.getEmail());
        } catch (MessagingException e) {
            log.error("Erreur lors de l'envoi de l'email d'approbation: {}", e.getMessage());
        }
    }

    private void sendApprovalNotificationEmail(ClassesEntity classe, EtablissementEntity etablissement) {
        try {
            Classes classeDto = dozerMapperBean.map(classe, Classes.class);
            Etablissement etablissementDto = dozerMapperBean.map(etablissement, Etablissement.class);
            
            String htmlContent = emailTemplateService.generateClassApprovalNotificationEmail(
                classeDto, etablissementDto);
            
            mailService.sendEmail(etablissement.getEmail(), 
                "Classe approuvée - " + classe.getNom(), htmlContent);
            
            log.info("Email de notification d'approbation envoyé à: {}", etablissement.getEmail());
        } catch (MessagingException e) {
            log.error("Erreur lors de l'envoi de l'email de notification: {}", e.getMessage());
        }
    }

    public void approuverClasseParEtablissement(String classeId, String etablissementId) {
        try {
            ClassesEntity classe = daoAccessorService.getRepository(ClassesRepository.class)
                    .findById(classeId)
                    .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Classe introuvable"));
            
            EtablissementEntity etablissement = daoAccessorService.getRepository(EtablissementRepository.class)
                    .findById(etablissementId)
                    .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Établissement introuvable"));
            
            if (classe.getEtat() != EtatClasse.EN_ATTENTE_APPROBATION) {
                throw new SchoolException(SchoolErrorCode.INVALID_STATE, 
                    "Cette classe n'est pas en attente d'approbation");
            }
            
            classe.setEtat(EtatClasse.ACTIF);
            daoAccessorService.getRepository(ClassesRepository.class).save(classe);
            
            // Send approval notification email
            sendApprovalNotificationEmail(classe, etablissement);
            
            log.info("Classe {} approuvée par l'établissement {}", classeId, etablissementId);
        } catch (Exception e) {
            log.error("Erreur lors de l'approbation de la classe: {}", e.getMessage());
            throw e;
        }
    }

    public void rejeterClasseParEtablissement(String classeId, String etablissementId) {
        try {
            ClassesEntity classe = daoAccessorService.getRepository(ClassesRepository.class)
                    .findById(classeId)
                    .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Classe introuvable"));
            
            EtablissementEntity etablissement = daoAccessorService.getRepository(EtablissementRepository.class)
                    .findById(etablissementId)
                    .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Établissement introuvable"));
            
            if (classe.getEtat() != EtatClasse.EN_ATTENTE_APPROBATION) {
                throw new SchoolException(SchoolErrorCode.INVALID_STATE, 
                    "Cette classe n'est pas en attente d'approbation");
            }
            
            classe.setEtat(EtatClasse.INACTIF);
            daoAccessorService.getRepository(ClassesRepository.class).save(classe);
            
            log.info("Classe {} rejetée par l'établissement {}", classeId, etablissementId);
        } catch (Exception e) {
            log.error("Erreur lors du rejet de la classe: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Gets all moderators of a specific class
     */
    public List<Utilisateurs> obtenirModerateursDeLaClasse(String idClasse) throws SchoolException {
        ClassesRepository classesRepository = daoAccessorService.getRepository(ClassesRepository.class);
        ProfesseursRepository professeursRepository = daoAccessorService.getRepository(ProfesseursRepository.class);
        
        ClassesEntity classe = classesRepository.findById(idClasse)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Classe non trouvée avec l'ID: " + idClasse));

        List<Utilisateurs> moderateurs = new java.util.ArrayList<>();
        
        // Add the main moderator if exists
        if (classe.getModerator() != null) {
            Professeurs moderateur = dozerMapperBean.map(classe.getModerator(), Professeurs.class);
            moderateurs.add(moderateur);
        }
        
        // Query additional moderators from the many-to-many table using native query
        try {
            List<String> moderatorIds = professeursRepository.findModeratorIdsForClass(idClasse);
            for (String moderatorId : moderatorIds) {
                // Avoid duplicates - don't add if already the main moderator
                if (classe.getModerator() == null || !moderatorId.equals(classe.getModerator().getId())) {
                    professeursRepository.findById(moderatorId).ifPresent(prof -> {
                        Professeurs moderateur = dozerMapperBean.map(prof, Professeurs.class);
                        moderateurs.add(moderateur);
                    });
                }
            }
        } catch (Exception e) {
            log.warn("Could not fetch additional moderators from many-to-many table: {}", e.getMessage());
        }
        
        log.info("Récupération de {} modérateurs pour la classe: {}", moderateurs.size(), idClasse);
        return moderateurs;
    }

}