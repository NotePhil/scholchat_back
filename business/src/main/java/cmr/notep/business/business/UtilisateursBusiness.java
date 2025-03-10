package cmr.notep.business.business;

import cmr.notep.business.exceptions.SchoolException;
import cmr.notep.business.exceptions.enums.SchoolErrorCode;
import cmr.notep.business.services.ActivationEmailService;
import cmr.notep.business.services.MailService;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static cmr.notep.business.config.BusinessConfig.dozerMapperBean;

@Component
@Slf4j
public class UtilisateursBusiness {
    private final DaoAccessorService daoAccessorService ;
    private final ActivationEmailService activationEmailService;
    private final JwtUtil jwtUtil;

    public UtilisateursBusiness(DaoAccessorService daoAccessorService, ActivationEmailService activationEmailService, JwtUtil jwtUtil) {
        this.daoAccessorService = daoAccessorService;
        this.activationEmailService = activationEmailService;
        this.jwtUtil = jwtUtil;
    }

    public Utilisateurs avoirUtilisateur(String idUtilisateur) {
        log.info("Récupération de l'utilisateur avec ID: {}", idUtilisateur);
        return mapUtilisateursEntityToModele(daoAccessorService.getRepository(UtilisateursRepository.class)
                .findById(idUtilisateur)
                .orElseThrow(()-> new SchoolException(SchoolErrorCode.NOT_FOUND, "Utilisateur introuvable avec l'ID: " + idUtilisateur)));
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
        // If the user is a professor, mark them as a professor and ensure they aren't an admin by default
        if (utilisateur instanceof Professeurs) {
            userEntity.setAdmin(false);  // Professors are not admins by default, unless explicitly set
        } else {
            // If not a professor, handle based on type and admin flag
            userEntity.setAdmin(utilisateur.isAdmin());
        }

        // Save the user entity
        UtilisateursEntity savedUserEntity = daoAccessorService.getRepository(UtilisateursRepository.class)
                .save(userEntity);

        // Only generate token and send email for non-professor users
        if (!(savedUserEntity instanceof ProfesseursEntity)) {
            // Here we handle the roles based on user type and admin status
            List<String> roles = new ArrayList<>();
            if (savedUserEntity.getAdmin()) {
                roles.add("ROLE_ADMIN");
            } else {
                roles.add("ROLE_USER");
            }

            // Add user type as a role, e.g., "ROLE_PROFESSOR", "ROLE_STUDENT", etc.
            if (savedUserEntity instanceof ProfesseursEntity) {
                roles.add("ROLE_PROFESSOR");
            } else if (savedUserEntity instanceof ElevesEntity) {
                roles.add("ROLE_STUDENT");
            }

            // Generate token with the roles included
            String activationToken = jwtUtil.generateAccessToken(savedUserEntity.getEmail(), roles);
            savedUserEntity.setActivationToken(activationToken);
            savedUserEntity = daoAccessorService.getRepository(UtilisateursRepository.class)
                    .save(savedUserEntity);

            // Send activation email
            Utilisateurs savedUtilisateur = mapUtilisateursEntityToModele(savedUserEntity);
            activationEmailService.sendActivationEmail(savedUtilisateur, activationToken);
            log.info("Activation email process triggered successfully for {}", savedUserEntity.getEmail());
        }

        // Return the saved user model
        return mapUtilisateursEntityToModele(savedUserEntity);
    }

    public Utilisateurs posterGenericUtilisateur(IUtilisateurs utilisateur) {
        log.info("Création d'un nouvel utilisateur");
        return mapUtilisateursEntityToModele(this.daoAccessorService.getRepository(UtilisateursRepository.class)
                .save(mapUtilisateursModeleToEntity(utilisateur)));

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

    private static UtilisateursEntity mapUtilisateursModeleToEntity(IUtilisateurs utilisateur) {
        if(utilisateur instanceof Professeurs)
            return dozerMapperBean.map(utilisateur, ProfesseursEntity.class);
        else if(utilisateur instanceof Eleves)
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

        // Change the state to 'VALIDATED'
        userEntity.setEtat(EtatUtilisateur.VALIDATED);

        // Assign the 'ROLE_PROFESSOR' if not already assigned
        if (!userEntity.getAdmin()) {
            List<String> roles = new ArrayList<>();
            roles.add("ROLE_PROFESSOR");

            // Assuming `generateAccessToken` now handles roles, we pass the email and roles
            String activationToken = jwtUtil.generateAccessToken(userEntity.getEmail(), roles);
            userEntity.setActivationToken(activationToken);
        }

        // Save the validated professor entity
        userEntity = daoAccessorService.getRepository(UtilisateursRepository.class)
                .save(userEntity);

        // Convert the entity to model and send activation email
        Utilisateurs utilisateur = mapUtilisateursEntityToModele(userEntity);
        activationEmailService.sendActivationEmail(utilisateur, userEntity.getActivationToken());

        log.info("Professor {} validated successfully", professorId);

        // Return the updated professor
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
}