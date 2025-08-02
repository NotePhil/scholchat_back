package cmr.notep.business.business;
import cmr.notep.business.services.*;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import cmr.notep.business.exceptions.SchoolException;
import cmr.notep.business.exceptions.enums.SchoolErrorCode;
import cmr.notep.business.utils.JwtUtil;
import cmr.notep.interfaces.modeles.*;
import cmr.notep.modele.EtatUtilisateur;
import cmr.notep.ressourcesjpa.commun.DaoAccessorService;
import cmr.notep.ressourcesjpa.dao.*;
import cmr.notep.ressourcesjpa.repository.UtilisateursRepository;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.transaction.annotation.Transactional;

import cmr.notep.interfaces.modeles.Professeurs;
import cmr.notep.interfaces.modeles.Utilisateurs;

import cmr.notep.ressourcesjpa.dao.MotifRejetEntity;
import cmr.notep.ressourcesjpa.dao.ProfesseursEntity;
import cmr.notep.ressourcesjpa.repository.MotifRejetRepository;
import cmr.notep.ressourcesjpa.repository.ProfesseursRepository;


import java.net.URI;
import java.time.LocalDateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static cmr.notep.business.config.BusinessConfig.dozerMapperBean;
import cmr.notep.business.services.MediaService;
@Component
@Slf4j
@Transactional(noRollbackFor = SchoolException.class)
public class UtilisateursBusiness {
    private final DaoAccessorService daoAccessorService ;
    private final ActivationEmailService activationEmailService;
    private final JwtUtil jwtUtil;
    private final MailServiceInterface mailService;
    private final IRejectionEmailService rejectionEmailService;
    private final RoleService roleService;
    private final UserValidationService userValidationService;
    private final AwaitingValidationEmailService awaitingValidationEmailService;
    private final MediaService mediaService;

    @Autowired
    private TemplateEngine templateEngine;

    public UtilisateursBusiness(DaoAccessorService daoAccessorService,
                                ActivationEmailService activationEmailService,
                                JwtUtil jwtUtil,
                                MailServiceInterface mailService,
                                IRejectionEmailService rejectionEmailService,
                                RoleService roleService, UserValidationService userValidationService, UserValidationService userValidationService1,
                                AwaitingValidationEmailService awaitingValidationEmailService,
                                MediaService mediaService) {
        this.daoAccessorService = daoAccessorService;
        this.activationEmailService = activationEmailService;
        this.jwtUtil = jwtUtil;
        this.mailService = mailService;
        this.rejectionEmailService = rejectionEmailService;
        this.roleService = roleService;
        this.userValidationService = userValidationService1;
        this.awaitingValidationEmailService = awaitingValidationEmailService;
        this.mediaService = mediaService;
    }
    public Utilisateurs patcherUtilisateur(String idUtilisateur, Utilisateurs partialUpdate) {
        log.info("Patching user with ID: {}", idUtilisateur);

        // 1. Fetch and validate existing user
        UtilisateursEntity existingEntity = daoAccessorService.getRepository(UtilisateursRepository.class)
                .findById(idUtilisateur)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND,
                        "Utilisateur introuvable avec l'ID: " + idUtilisateur));

        // 2. Map to model for easier manipulation
        Utilisateurs existingUser = mapUtilisateursEntityToModele(existingEntity);

        // 4. Update common fields with null checks
        updateCommonFields(existingUser, partialUpdate);

        // 5. Handle type-specific updates
        if (existingUser instanceof Professeurs && partialUpdate instanceof Professeurs) {
            handleProfessorUpdates((Professeurs) existingUser, (Professeurs) partialUpdate);
        } else if (existingUser instanceof Eleves && partialUpdate instanceof Eleves) {
            handleStudentUpdates((Eleves) existingUser, (Eleves) partialUpdate);
        }

        // 6. Map back to entity and save
        UtilisateursEntity updatedEntity = mapUtilisateursModeleToEntity(existingUser);
        updatedEntity = daoAccessorService.getRepository(UtilisateursRepository.class).save(updatedEntity);

        // 7. Return updated model
        return mapUtilisateursEntityToModele(updatedEntity);
    }

    private void updateCommonFields(Utilisateurs existing, Utilisateurs updates) {
        if (updates.getNom() != null && !updates.getNom().isBlank()) {
            existing.setNom(updates.getNom().trim());
        }
        if (updates.getPrenom() != null && !updates.getPrenom().isBlank()) {
            existing.setPrenom(updates.getPrenom().trim());
        }
        if (updates.getEmail() != null && !updates.getEmail().isBlank()) {
            existing.setEmail(updates.getEmail().trim().toLowerCase());
        }
        if (updates.getTelephone() != null && !updates.getTelephone().isBlank()) {
            existing.setTelephone(updates.getTelephone().trim());
        }
        if (updates.getAdresse() != null && !updates.getAdresse().isBlank()) {
            existing.setAdresse(updates.getAdresse().trim());
        }
        if (updates.getEtat() != null) {
            existing.setEtat(updates.getEtat());
        }
    }

    private void handleProfessorUpdates(Professeurs existingProf, Professeurs updateProf) {
        // Validate and update CNI Recto
        if (updateProf.getCniUrlRecto() != null) {
            validateMediaUrl(updateProf.getCniUrlRecto());
            existingProf.setCniUrlRecto(updateProf.getCniUrlRecto());
        }

        // Validate and update CNI Verso
        if (updateProf.getCniUrlVerso() != null) {
            validateMediaUrl(updateProf.getCniUrlVerso());
            existingProf.setCniUrlVerso(updateProf.getCniUrlVerso());
        }

        // Validate and update Selfie
        if (updateProf.getSelfieUrl() != null) {
            validateMediaUrl(updateProf.getSelfieUrl());
            existingProf.setSelfieUrl(updateProf.getSelfieUrl());
        }

        // Update matricule if provided
        if (updateProf.getMatriculeProfesseur() != null && !updateProf.getMatriculeProfesseur().isBlank()) {
            existingProf.setMatriculeProfesseur(updateProf.getMatriculeProfesseur().trim());
        }
    }

    private void handleStudentUpdates(Eleves existingEleve, Eleves updateEleve) {
        if (updateEleve.getNiveau() != null && !updateEleve.getNiveau().isBlank()) {
            existingEleve.setNiveau(updateEleve.getNiveau().trim());
        }
    }

    private void validateMediaUrl(String url) {
        if (url == null || url.isBlank()) {
            throw new SchoolException(SchoolErrorCode.INVALID_INPUT, "L'URL du média ne peut pas être vide");
        }

        try {
            new URI(url).toURL(); // Validate URL format
        } catch (Exception e) {
            throw new SchoolException(SchoolErrorCode.INVALID_INPUT,
                    "L'URL du média n'est pas valide: " + url);
        }

        // Optionally verify the URL points to your Minio storage
        if (!url.startsWith("http://localhost:9000") && !url.startsWith("https://your-minio-domain")) {
            log.warn("Media URL points to external storage: {}", url);
        }
    }
    public Utilisateurs avoirUtilisateur(String idUtilisateur) {
        log.info("Récupération de l'utilisateur avec ID: {}", idUtilisateur);
        UtilisateursEntity utilisateurEntity = daoAccessorService.getRepository(UtilisateursRepository.class)
                .findById(idUtilisateur)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Utilisateur introuvable avec l'ID: " + idUtilisateur));

        // Force loading of received messages
        Hibernate.initialize(utilisateurEntity.getMessagesEnvoyerEntities());
        Hibernate.initialize(utilisateurEntity.getMessagesRecusEntities());

        return mapUtilisateursEntityToModele(utilisateurEntity);
    }


    public Utilisateurs posterUtilisateur(Utilisateurs utilisateur) {
        log.info("Creating new user: {}", utilisateur.getEmail());

        // Validation des données
        userValidationService.validateUserData(utilisateur);

        // Configuration par défaut
        utilisateur.setAdmin(false);
        utilisateur.setEtat(utilisateur instanceof Professeurs ?
                EtatUtilisateur.AWAITING_VALIDATION : EtatUtilisateur.PENDING);
        utilisateur.setCreationDate(LocalDateTime.now());

        // Mapping et sauvegarde
        UtilisateursEntity userEntity = mapUtilisateursModeleToEntity(utilisateur);
        UtilisateursEntity savedUserEntity = daoAccessorService.getRepository(UtilisateursRepository.class)
                .save(userEntity);
        try {
            if (savedUserEntity.getId() != null && !savedUserEntity.getId().equals("temp")) {
                String userFolderPath = "users/" + savedUserEntity.getId();
                mediaService.ensureFolderExists(userFolderPath);
                log.info("Created user folder for ID: {}", savedUserEntity.getId());

                // Create standard subfolders
                mediaService.ensureFolderExists(userFolderPath + "/photos");
                mediaService.ensureFolderExists(userFolderPath + "/documents");
                mediaService.ensureFolderExists(userFolderPath + "/videos");
            }
        } catch (Exception e) {
            log.error("Failed to create user folder for ID: {}", savedUserEntity.getId(), e);
            // Don't fail the operation, just log the error
        }

        // Génération du token et envoi d'email (sauf pour les professeurs)
        if (!(savedUserEntity instanceof ProfesseursEntity)) {
            List<String> roles = roleService.determineUserRoles(mapUtilisateursEntityToModele(savedUserEntity));
            String activationToken = jwtUtil.generateAccessToken(savedUserEntity.getEmail(), roles);
            savedUserEntity.setActivationToken(activationToken);
            savedUserEntity = daoAccessorService.getRepository(UtilisateursRepository.class).save(savedUserEntity);

            activationEmailService.sendActivationEmail(
                    mapUtilisateursEntityToModele(savedUserEntity),
                    activationToken
            );
        }

        return mapUtilisateursEntityToModele(savedUserEntity);
    }


    public List<Utilisateurs> avoirToutUtilisateurs() {
        log.info("Récupération de tous les utilisateurs");
        return daoAccessorService.getRepository(UtilisateursRepository.class).findAll()
                .stream().map(utilisateursEntity -> mapUtilisateursEntityToModele(utilisateursEntity))
                .collect(Collectors.toList());
    }

    public Utilisateurs mettreUtilisateurAJour(Utilisateurs utilisateur) {
        log.info("Mise à jour de l'utilisateur avec ID: {}", utilisateur.getId());

        // Fetch the existing user entity from the repository
        UtilisateursEntity existingEntity = daoAccessorService.getRepository(UtilisateursRepository.class)
                .findById(utilisateur.getId())
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Utilisateur introuvable avec l'ID: " + utilisateur.getId()));

        // Map the model to the entity and update the entity
        UtilisateursEntity updatedEntity = mapUtilisateursModeleToEntity(utilisateur);

        // Save the updated entity
        updatedEntity = daoAccessorService.getRepository(UtilisateursRepository.class).save(updatedEntity);

        // Return the updated model
        return mapUtilisateursEntityToModele(updatedEntity);
    }

    private static Utilisateurs mapUtilisateursEntityToModele(UtilisateursEntity utilisateurEntity) {
        if(utilisateurEntity instanceof ProfesseursEntity)
            return dozerMapperBean.map(utilisateurEntity, Professeurs.class);
        else if(utilisateurEntity instanceof ElevesEntity)
            return dozerMapperBean.map(utilisateurEntity, Eleves.class);
        else if (utilisateurEntity instanceof RepetiteursEntity)
            return dozerMapperBean.map(utilisateurEntity, Repetiteurs.class);
        else if (utilisateurEntity instanceof ParentsEntity)
            return dozerMapperBean.map(utilisateurEntity, Parents.class);
        else
            return dozerMapperBean.map(utilisateurEntity, Utilisateurs.class);
    }


    public static UtilisateursEntity mapUtilisateursModeleToEntity(IUtilisateurs utilisateur) {
        if (utilisateur instanceof Professeurs)
            return dozerMapperBean.map(utilisateur, ProfesseursEntity.class);
        else if (utilisateur instanceof Eleves)
            return dozerMapperBean.map(utilisateur, ElevesEntity.class);
        else if (utilisateur instanceof Repetiteurs)
            return dozerMapperBean.map(utilisateur, RepetiteursEntity.class);
        else if (utilisateur instanceof Parents)
            return dozerMapperBean.map(utilisateur, ParentsEntity.class);
        else
            return dozerMapperBean.map(utilisateur, UtilisateursEntity.class);
    }


    public Utilisateurs avoirUtilisateurParEmail(String email) {
        log.info("Fetching user with email: {}", email);
        try {
            UtilisateursEntity entity = daoAccessorService.getRepository(UtilisateursRepository.class)
                    .findByEmail(email)
                    .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Utilisateur introuvable avec l'email: " + email));

            return mapUtilisateursEntityToModele(entity);
        } catch (Exception e) {
            log.error("Error fetching user by email: {}", email, e);
            throw new SchoolException(SchoolErrorCode.EMAIL_ERROR, "Erreur lors de la récupération de l'utilisateur par email");
        }
    }


    public Utilisateurs regenererActivationEmail(String email) {
        log.info("Regenerating activation email for: {}", email);

        UtilisateursEntity utilisateurEntity = daoAccessorService.getRepository(UtilisateursRepository.class)
                .findByEmail(email)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND,
                        "Utilisateur introuvable avec l'email: " + email));

        if (utilisateurEntity.getEtat() != EtatUtilisateur.PENDING) {
            throw new SchoolException(SchoolErrorCode.INVALID_STATE,
                    "Activation email can only be regenerated for users in PENDING state");
        }

        // Utilisation du service de rôles
        Utilisateurs utilisateur = mapUtilisateursEntityToModele(utilisateurEntity);
        List<String> roles = roleService.determineUserRoles(utilisateur);

        String activationToken = jwtUtil.generateAccessToken(utilisateurEntity.getEmail(), roles);
        utilisateurEntity.setActivationToken(activationToken);
        utilisateurEntity = daoAccessorService.getRepository(UtilisateursRepository.class).save(utilisateurEntity);

        try {
            activationEmailService.sendActivationEmail(utilisateur, activationToken);
            log.info("New activation email sent successfully to {}", utilisateur.getEmail());
        } catch (Exception e) {
            log.error("Failed to send activation email to {}: {}", utilisateur.getEmail(), e.getMessage());
            throw new SchoolException(SchoolErrorCode.EMAIL_NOT_SENT,
                    "L'email d'activation n'a pas pu être envoyé.");
        }

        return utilisateur;
    }




    public Utilisateurs validerProfesseur(String professorId) {
        log.info("Processing professor validation for ID: {}", professorId);

        // Fetch the professor by ID
        UtilisateursEntity userEntity = daoAccessorService.getRepository(UtilisateursRepository.class)
                .findById(professorId)
                .orElseThrow(() -> new SchoolException(
                        SchoolErrorCode.NOT_FOUND,
                        "Professeur introuvable avec l'ID: " + professorId
                ));

        // Ensure the user is a professor
        if (!(userEntity instanceof ProfesseursEntity)) {
            throw new SchoolException(
                    SchoolErrorCode.INVALID_OPERATION,
                    "L'utilisateur n'est pas un professeur"
            );
        }

        // Ensure the professor is in the correct state
        if (userEntity.getEtat() != EtatUtilisateur.AWAITING_VALIDATION) {
            throw new SchoolException(
                    SchoolErrorCode.INVALID_STATE,
                    "Le professeur n'est pas en attente de validation"
            );
        }

        // Change the state to 'PENDING' (not 'VALIDATED' directly)
        userEntity.setEtat(EtatUtilisateur.PENDING);

        // Generate activation token with professor's email, not admin's
        List<String> roles = new ArrayList<>();
        roles.add("ROLE_PROFESSOR");

        String activationToken = jwtUtil.generateAccessToken(userEntity.getEmail(), roles); // Utilisez l'email du professeur ici
        userEntity.setActivationToken(activationToken);

        // Save the validated professor entity
        userEntity = daoAccessorService.getRepository(UtilisateursRepository.class)
                .save(userEntity);

        // Convert the entity to model and send activation email
        Utilisateurs utilisateur = mapUtilisateursEntityToModele(userEntity);
        activationEmailService.sendActivationEmail(utilisateur, activationToken);

        log.info("Professor {} validated successfully", professorId);

        return utilisateur;
    }


    public List<Utilisateurs> avoirProfesseursEnAttente() {
        log.info("Fetching all professors awaiting validation");

        // Fetch all ProfesseursEntity with the AWAITING_VALIDATION state
        List<UtilisateursEntity> professeursEntities = daoAccessorService.getRepository(UtilisateursRepository.class)
                .findByEtat(EtatUtilisateur.AWAITING_VALIDATION);

        // Map the entities to the Utilisateurs model
        return professeursEntities.stream()
                .map(UtilisateursBusiness::mapUtilisateursEntityToModele)
                .collect(Collectors.toList());
    }



    public Utilisateurs rejeterProfesseur(String professorId, String codeErreur, String motifSupplementaire) {
        log.info("Rejet du professeur avec l'ID: {}", professorId);

        // Récupérer le professeur
        ProfesseursEntity professeurEntity = daoAccessorService.getRepository(ProfesseursRepository.class)
                .findById(professorId)
                .orElseThrow(() -> new SchoolException(
                        SchoolErrorCode.NOT_FOUND,
                        "Professeur introuvable avec l'ID: " + professorId
                ));

        // Vérifiez que le token est bien présent
        if (professeurEntity.getActivationToken() == null) {
            professeurEntity.setActivationToken(jwtUtil.generateRefreshToken(professeurEntity.getEmail()));
        }
        // Vérifier que le professeur est en attente de validation
        if (professeurEntity.getEtat() != EtatUtilisateur.AWAITING_VALIDATION) {
            throw new SchoolException(
                    SchoolErrorCode.INVALID_STATE,
                    "Le professeur doit être en attente de validation pour être rejeté. Statut actuel: " + professeurEntity.getEtat()
            );
        }

        // Récupérer le motif de rejet
        MotifRejetEntity motifEntity = daoAccessorService.getRepository(MotifRejetRepository.class)
                .findByCode(codeErreur)
                .orElseThrow(() -> new SchoolException(
                        SchoolErrorCode.NOT_FOUND,
                        "Motif de rejet introuvable avec le code: " + codeErreur
                ));

        // Mettre à jour le statut du professeur
        professeurEntity.setEtat(EtatUtilisateur.REJECTED);
        ProfesseursEntity savedEntity = daoAccessorService.getRepository(ProfesseursRepository.class)
                .save(professeurEntity);

        // Envoyer l'email de rejet via le service dédié
        rejectionEmailService.sendRejectionEmail(professeurEntity, motifEntity, motifSupplementaire);

        return dozerMapperBean.map(savedEntity, Utilisateurs.class);
    }
}