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

@Component
@Slf4j
@Transactional(noRollbackFor = SchoolException.class)
public class UtilisateursBusiness {
    private final DaoAccessorService daoAccessorService ;
    private final ActivationEmailService activationEmailService;
    private final JwtUtil jwtUtil;
    private final MailServiceInterface mailService;
    private final IRejectionEmailService rejectionEmailService;
    private final AwaitingValidationEmailService awaitingValidationEmailService;


    @Autowired
    private TemplateEngine templateEngine;

    public UtilisateursBusiness(DaoAccessorService daoAccessorService,
                                ActivationEmailService activationEmailService,
                                JwtUtil jwtUtil,
                                MailServiceInterface mailService,
                                IRejectionEmailService rejectionEmailService,
                                AwaitingValidationEmailService awaitingValidationEmailService) {
        this.daoAccessorService = daoAccessorService;
        this.activationEmailService = activationEmailService;
        this.jwtUtil = jwtUtil;
        this.mailService = mailService;
        this.rejectionEmailService = rejectionEmailService;
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
        log.info("Création d'un nouvel utilisateur");

        if (dozerMapperBean == null) {
            throw new IllegalStateException("DozerBeanMapper is not initialized.");
        }

        // Set user state based on user type
        if (utilisateur instanceof Professeurs) {
            utilisateur.setEtat(EtatUtilisateur.AWAITING_VALIDATION);
        } else {
            utilisateur.setEtat(EtatUtilisateur.PENDING);
        }

        // Set creation date
        utilisateur.setCreationDate(LocalDateTime.now());

        // Map the user model to entity
        UtilisateursEntity userEntity = mapUtilisateursModeleToEntity(utilisateur);
        if (userEntity == null) {
            throw new SchoolException(SchoolErrorCode.MAPPING_FAILED, "User entity mapping failed.");
        }

        // Set role information here:
        if (utilisateur instanceof Professeurs) {
            userEntity.setAdmin(false);
        } else {
            userEntity.setAdmin(utilisateur.isAdmin());
        }

        // Save the user entity
        UtilisateursEntity savedUserEntity = daoAccessorService.getRepository(UtilisateursRepository.class)
                .save(userEntity);

        // Handle email sending based on user type
        if (savedUserEntity instanceof ProfesseursEntity) {
            // For professors in AWAITING_VALIDATION state, send awaiting validation email
            Utilisateurs savedUtilisateur = mapUtilisateursEntityToModele(savedUserEntity);
            awaitingValidationEmailService.sendAwaitingValidationEmail(savedUtilisateur);
            log.info("Awaiting validation email sent for professor {}", savedUserEntity.getEmail());
        } else {
            // For all other users, proceed with normal activation process
            List<String> roles = new ArrayList<>();
            if (savedUserEntity.getAdmin()) {
                roles.add("ROLE_ADMIN");
            } else {
                roles.add("ROLE_USER");
            }

            // Add user type specific roles
            if (savedUserEntity instanceof ElevesEntity) {
                roles.add("ROLE_STUDENT");
            } else if (savedUserEntity instanceof ParentsEntity) {
                roles.add("ROLE_PARENT");
            } else if (savedUserEntity instanceof RepetiteursEntity) {
                roles.add("ROLE_TUTOR");
            }

            String activationToken = jwtUtil.generateAccessToken(savedUserEntity.getEmail(), roles);
            savedUserEntity.setActivationToken(activationToken);
            savedUserEntity = daoAccessorService.getRepository(UtilisateursRepository.class)
                    .save(savedUserEntity);

            // Send activation email
            Utilisateurs savedUtilisateur = mapUtilisateursEntityToModele(savedUserEntity);
            activationEmailService.sendActivationEmail(savedUtilisateur, activationToken);
            log.info("Activation email sent for user {}", savedUserEntity.getEmail());
        }

        // Return the saved user model
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


    @Transactional(readOnly = true)
    public Utilisateurs avoirUtilisateurParEmail(String email) {
        log.info("Fetching user with email: {}", email);
        return mapUtilisateursEntityToModele(
                daoAccessorService.getRepository(UtilisateursRepository.class)
                        .findByEmail(email)
                        .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Utilisateur introuvable avec l'email: " + email))
        );
    }


    @Retryable(
            value = MessagingException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 5000)
    )
    public Utilisateurs regenererActivationEmail(String email) {
        log.info("Regenerating activation email for: {}", email);

        // Fetch the user
        UtilisateursEntity utilisateurEntity = daoAccessorService.getRepository(UtilisateursRepository.class)
                .findByEmail(email)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Utilisateur introuvable avec l'email: " + email));

        // Check if the user is in PENDING state (only allow email regeneration for users in this state)
        if (utilisateurEntity.getEtat() != EtatUtilisateur.PENDING) {
            throw new SchoolException(SchoolErrorCode.INVALID_STATE, "Activation email can only be regenerated for users in PENDING state");
        }

        // Determine the roles based on the user type
        List<String> roles = new ArrayList<>();

        // Check the type of user and assign roles accordingly
        if (utilisateurEntity instanceof ProfesseursEntity) {
            roles.add("ROLE_PROFESSOR");
        } else if (utilisateurEntity instanceof ElevesEntity) {
            roles.add("ROLE_STUDENT");
        } else if (utilisateurEntity instanceof ParentsEntity) {
            roles.add("ROLE_PARENT");
        } else if (utilisateurEntity instanceof RepetiteursEntity) {
            roles.add("ROLE_TUTOR");
        } else {
            roles.add("ROLE_USER");
        }

        // If the user is an admin, add the admin role
        if (utilisateurEntity.getAdmin()) {
            roles.add("ROLE_ADMIN");
        }

        // Generate a new activation token with email and roles
        String activationToken = jwtUtil.generateAccessToken(utilisateurEntity.getEmail(), roles);

        // Set the new activation token in the entity
        utilisateurEntity.setActivationToken(activationToken);

        // Save the updated user entity
        utilisateurEntity = daoAccessorService.getRepository(UtilisateursRepository.class).save(utilisateurEntity);

        // Map the entity to the model
        Utilisateurs utilisateur = mapUtilisateursEntityToModele(utilisateurEntity);

        // Send the activation email
        try {
            activationEmailService.sendActivationEmail(utilisateur, activationToken);
            log.info("New activation email sent successfully to {}", utilisateur.getEmail());
        } catch (Exception e) {
            log.error("Failed to send activation email to {}: {}", utilisateur.getEmail(), e.getMessage());
            throw new SchoolException(SchoolErrorCode.EMAIL_NOT_SENT, "L'email d'activation n'a pas pu être envoyé.");
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