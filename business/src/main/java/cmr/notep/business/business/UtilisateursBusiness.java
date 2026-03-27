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
import cmr.notep.ressourcesjpa.repository.ParentsRepository;
import cmr.notep.ressourcesjpa.repository.ElevesRepository;
import cmr.notep.ressourcesjpa.repository.UserRoleRepository;
import java.util.Optional;


import java.net.URI;
import java.time.LocalDateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static cmr.notep.business.config.BusinessConfig.dozerMapperBean;
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

    @Autowired
    private TemplateEngine templateEngine;

    public UtilisateursBusiness(DaoAccessorService daoAccessorService,
                                ActivationEmailService activationEmailService,
                                JwtUtil jwtUtil,
                                MailServiceInterface mailService,
                                IRejectionEmailService rejectionEmailService,
                                RoleService roleService, UserValidationService userValidationService, UserValidationService userValidationService1,
                                AwaitingValidationEmailService awaitingValidationEmailService) {
        this.daoAccessorService = daoAccessorService;
        this.activationEmailService = activationEmailService;
        this.jwtUtil = jwtUtil;
        this.mailService = mailService;
        this.rejectionEmailService = rejectionEmailService;
        this.roleService = roleService;
        this.userValidationService = userValidationService1;
        this.awaitingValidationEmailService = awaitingValidationEmailService;
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
            log.info("Processing professor update for: {}", existingUser.getEmail());
            Professeurs existingProf = (Professeurs) existingUser;
            EtatUtilisateur originalState = existingProf.getEtat();
            log.info("Original state: {}", originalState);
            
            handleProfessorUpdates(existingProf, (Professeurs) partialUpdate);
            
            // Check if professor has all documents and should transition to awaiting validation
            boolean hasAllDocuments = existingProf.getCniUrlRecto() != null &&
                    existingProf.getCniUrlVerso() != null &&
                    existingProf.getSelfieUrl() != null;
            boolean wasNotAwaitingValidation = originalState != EtatUtilisateur.AWAITING_VALIDATION;
            
            log.info("Email conditions - hasAllDocuments: {}, wasNotAwaitingValidation: {}", hasAllDocuments, wasNotAwaitingValidation);
            
            if (hasAllDocuments) {
                log.info("Sending awaiting validation email for professor: {}", existingProf.getEmail());
                existingProf.setHasUploaded(true);
                existingProf.setEtat(EtatUtilisateur.AWAITING_VALIDATION);
                awaitingValidationEmailService.sendAwaitingValidationEmail(existingProf);
            }
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
        // Update hasUploaded status based on document presence
        boolean hasUploaded = existingProf.getCniUrlRecto() != null &&
                existingProf.getCniUrlVerso() != null &&
                existingProf.getSelfieUrl() != null;
        existingProf.setHasUploaded(hasUploaded);
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

        // Check if email already exists - if so, add the new role to existing user
        Optional<UtilisateursEntity> existingUserOpt = daoAccessorService.getRepository(UtilisateursRepository.class)
                .findByEmail(utilisateur.getEmail());
        if (existingUserOpt.isPresent()) {
            UtilisateursEntity existingEntity = existingUserOpt.get();
            String newRoleType = mapTypeToRole(utilisateur);

            // Check if this role already exists for this user
            if (daoAccessorService.getRepository(UserRoleRepository.class)
                    .existsByUtilisateurIdAndRoleType(existingEntity.getId(), newRoleType)) {
                throw new SchoolException(SchoolErrorCode.DUPLICATE_RESOURCE,
                        "Ce compte a deja ce role / This account already has this role: " + newRoleType);
            }

            // Add the new role to user_roles table
            addRoleToUser(existingEntity.getId(), newRoleType);

            // For parent role, insert directly into parents table via native SQL
            // (JPA JOINED inheritance prevents using JPA save for a different subtype)
            if ("PARENT".equals(newRoleType)) {
                try {
                    jakarta.persistence.EntityManager em = daoAccessorService.getRepository(UtilisateursRepository.class)
                            .findById(existingEntity.getId()).map(e -> e).orElse(null) != null ?
                            null : null; // dummy to get context
                    // Use native query via repository
                    daoAccessorService.getRepository(UtilisateursRepository.class)
                            .insertParentRole(existingEntity.getId());
                    log.info("Created parent entry for user {}", existingEntity.getId());
                } catch (Exception e) {
                    log.warn("Parent entry may already exist or could not be created: {}", e.getMessage());
                }
            }

            // For student role, insert directly into eleves table
            if ("STUDENT".equals(newRoleType)) {
                try {
                    String niveau = (utilisateur instanceof Eleves eleve) ? eleve.getNiveau() : "6eme";
                    daoAccessorService.getRepository(UtilisateursRepository.class)
                            .insertEleveRole(existingEntity.getId(), niveau != null ? niveau : "6eme");
                    log.info("Created eleve entry for user {}", existingEntity.getId());
                } catch (Exception e) {
                    log.warn("Eleve entry may already exist or could not be created: {}", e.getMessage());
                }
            }

            log.info("Added role {} to existing user {}", newRoleType, existingEntity.getEmail());
            return mapUtilisateursEntityToModele(existingEntity);
        }

        // Configuration par défaut
        utilisateur.setAdmin(false);
        utilisateur.setEtat(utilisateur instanceof Professeurs ?
                EtatUtilisateur.AWAITING_VALIDATION : EtatUtilisateur.PENDING);
        utilisateur.setCreationDate(LocalDateTime.now());

        // For professors, set hasUploaded based on whether documents are provided
        if (utilisateur instanceof Professeurs professeur) {
            boolean hasUploaded = professeur.getCniUrlRecto() != null &&
                    professeur.getCniUrlVerso() != null &&
                    professeur.getSelfieUrl() != null;
            professeur.setHasUploaded(hasUploaded);
        }

        // Mapping et sauvegarde
        UtilisateursEntity userEntity = mapUtilisateursModeleToEntity(utilisateur);
        userEntity.setId(UUID.randomUUID().toString());
        UtilisateursEntity savedUserEntity = daoAccessorService.getRepository(UtilisateursRepository.class)
                .save(userEntity);

        // Add role to user_roles table
        String roleType = mapTypeToRole(utilisateur);
        addRoleToUser(savedUserEntity.getId(), roleType);

        try {
            if (savedUserEntity.getId() != null && !savedUserEntity.getId().equals("temp")) {
                String userFolderPath = "users/" + savedUserEntity.getId();
               // mediaService.ensureFolderExists(userFolderPath);
                log.info("Created user folder for ID: {}", savedUserEntity.getId());
                // Create standard subfolders
                //mediaService.ensureFolderExists(userFolderPath + "/photos");
                //mediaService.ensureFolderExists(userFolderPath + "/documents");
                //mediaService.ensureFolderExists(userFolderPath + "/videos");
            }
        } catch (Exception e) {
            log.error("Failed to create user folder for ID: {}", savedUserEntity.getId(), e);
            // Don't fail the operation, just log the error
        }

        // Send activation email only for non-professors or professors who have uploaded documents
        if (!(savedUserEntity instanceof ProfesseursEntity professeurEntity)) {
            // For non-professors, send activation email immediately
            List<String> roles = roleService.determineUserRoles(mapUtilisateursEntityToModele(savedUserEntity));
            String activationToken = jwtUtil.generateAccessToken(savedUserEntity.getEmail(), roles);
            savedUserEntity.setActivationToken(activationToken);
            savedUserEntity = daoAccessorService.getRepository(UtilisateursRepository.class).save(savedUserEntity);
            activationEmailService.sendActivationEmail(
                    mapUtilisateursEntityToModele(savedUserEntity),
                    activationToken
            );
        } else if (Boolean.TRUE.equals(professeurEntity.getHasUploaded())) {
            // For professors who already have uploaded documents during creation
            List<String> roles = roleService.determineUserRoles(mapUtilisateursEntityToModele(savedUserEntity));
            String activationToken = jwtUtil.generateAccessToken(savedUserEntity.getEmail(), roles);
            savedUserEntity.setActivationToken(activationToken);
            savedUserEntity = daoAccessorService.getRepository(UtilisateursRepository.class).save(savedUserEntity);
            activationEmailService.sendActivationEmail(
                    mapUtilisateursEntityToModele(savedUserEntity),
                    activationToken
            );
        }
        // For professors without uploads, DO NOT send any email during creation

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
        else if (utilisateurEntity instanceof GestionnairesEntity)
            return dozerMapperBean.map(utilisateurEntity, Gestionnaires.class);
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
        else if (utilisateur instanceof Gestionnaires)
            return dozerMapperBean.map(utilisateur, GestionnairesEntity.class);
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

        // Ensure the professor has uploaded all required documents
        ProfesseursEntity professeurEntity = (ProfesseursEntity) userEntity;
        if (!Boolean.TRUE.equals(professeurEntity.getHasUploaded())) {
            throw new SchoolException(
                    SchoolErrorCode.INVALID_STATE,
                    "Le professeur n'a pas encore uploadé tous les documents requis"
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

    /**
     * Map user type to role string
     */
    private String mapTypeToRole(Utilisateurs utilisateur) {
        if (utilisateur instanceof Professeurs) return "PROFESSOR";
        if (utilisateur instanceof Eleves) return "STUDENT";
        if (utilisateur instanceof Parents) return "PARENT";
        if (utilisateur instanceof Repetiteurs) return "TUTOR";
        if (utilisateur instanceof Gestionnaires) return "GESTIONNAIRE";
        if (utilisateur.isAdmin()) return "ADMIN";
        return "USER";
    }

    /**
     * Get all active roles for a user from user_roles table
     */
    public List<String> getUserRoles(String userId) {
        try {
            UserRoleRepository roleRepo = daoAccessorService.getRepository(UserRoleRepository.class);
            return roleRepo.findByUtilisateurIdAndIsActiveTrue(userId).stream()
                    .map(UserRoleEntity::getRoleType)
                    .collect(java.util.stream.Collectors.toList());
        } catch (Exception e) {
            log.warn("Could not fetch user roles for {}: {}", userId, e.getMessage());
            return new java.util.ArrayList<>();
        }
    }

    /**
     * Add a role to an existing user
     */
    public void addRoleToUser(String userId, String roleType) {
        UserRoleRepository roleRepo = daoAccessorService.getRepository(UserRoleRepository.class);
        if (!roleRepo.existsByUtilisateurIdAndRoleType(userId, roleType)) {
            UserRoleEntity role = new UserRoleEntity();
            role.setId(UUID.randomUUID().toString());
            role.setUtilisateurId(userId);
            role.setRoleType(roleType);
            role.setIsActive(true);
            role.setDateAttribution(LocalDateTime.now());
            roleRepo.save(role);
            log.info("Added role {} to user {}", roleType, userId);
        }
    }

    /**
     * Get children for a parent from parent_eleve table
     */
    public List<AuthResponse.ChildInfo> getChildrenForParent(String parentId) {
        try {
            Optional<ParentsEntity> parentOpt = daoAccessorService.getRepository(ParentsRepository.class)
                    .findById(parentId);
            if (parentOpt.isEmpty() || parentOpt.get().getEnfants() == null) {
                return new java.util.ArrayList<>();
            }
            return parentOpt.get().getEnfants().stream()
                    .map(e -> AuthResponse.ChildInfo.builder()
                            .id(e.getId())
                            .nom(e.getNom())
                            .prenom(e.getPrenom())
                            .niveau(e.getNiveau())
                            .build())
                    .collect(java.util.stream.Collectors.toList());
        } catch (Exception e) {
            log.warn("Could not fetch children for parent {}: {}", parentId, e.getMessage());
            return new java.util.ArrayList<>();
        }
    }
}