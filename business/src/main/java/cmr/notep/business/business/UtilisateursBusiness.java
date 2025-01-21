package cmr.notep.business.business;

import cmr.notep.business.exceptions.SchoolException;
import cmr.notep.business.exceptions.enums.SchoolErrorCode;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static cmr.notep.business.config.BusinessConfig.dozerMapperBean;

@Component
@Slf4j
public class UtilisateursBusiness {
    private final DaoAccessorService daoAccessorService ;
    private final MailService mailService;
    private final JwtUtil jwtUtil;

    public UtilisateursBusiness(DaoAccessorService daoAccessorService, MailService mailService, JwtUtil jwtUtil) {
        this.daoAccessorService = daoAccessorService;
        this.mailService = mailService;
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

        // Automatically set etat to PENDING
        utilisateur.setEtat(EtatUtilisateur.PENDING);

        // Map the user model to an entity
        UtilisateursEntity userEntity = mapUtilisateursModeleToEntity(utilisateur);
        if (userEntity == null) {
            throw new SchoolException(SchoolErrorCode.MAPPING_FAILED, "User entity mapping failed.");
        }

        // Save the user entity first to generate the ID
        UtilisateursEntity savedUserEntity = daoAccessorService.getRepository(UtilisateursRepository.class)
                .save(userEntity);

        // Now that the ID is available, generate JWT token
        String activationToken = jwtUtil.generateToken(
                savedUserEntity.getEmail(),
                Map.of("userId", savedUserEntity.getId())
        );
        savedUserEntity.setActivationToken(activationToken);

        // Send the activation email after the user has been saved and the token is generated
        try {
            mailService.sendWelcomeEmail(savedUserEntity.getEmail(), savedUserEntity.getNom(), activationToken);
            log.info("Activation email sent successfully to {}", savedUserEntity.getEmail());
        } catch (MessagingException e) {
            log.error("Failed to send activation email to {}: {}", savedUserEntity.getEmail(), e.getMessage());
            // Throw a custom exception to handle this error uniformly
            throw new SchoolException(SchoolErrorCode.EMAIL_NOT_SENT, "Failed to send activation email.");
        }

        // Return the created user model
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
        UtilisateursEntity utilisateur = daoAccessorService.getRepository(UtilisateursRepository.class)
                .findByEmail(email)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Utilisateur introuvable avec l'email: " + email));

        if (utilisateur.getEtat() != EtatUtilisateur.PENDING) {
            throw new SchoolException(SchoolErrorCode.INVALID_STATE, "Activation email can only be regenerated for users in PENDING state");
        }

        // Generate a new activation token
        String activationToken = jwtUtil.generateToken(
                utilisateur.getEmail(),
                Map.of("userId", utilisateur.getId())
        );
        utilisateur.setActivationToken(activationToken);

        // Save updated user
        daoAccessorService.getRepository(UtilisateursRepository.class).save(utilisateur);

        // Send the activation email
        try {
            mailService.sendWelcomeEmail(utilisateur.getEmail(), utilisateur.getNom(), activationToken);
            log.info("New activation email sent successfully to {}", utilisateur.getEmail());
        } catch (MessagingException e) {
            log.error("Failed to send activation email to {}: {}", utilisateur.getEmail(), e.getMessage());
            throw new SchoolException(SchoolErrorCode.EMAIL_NOT_SENT, "L'email d'activation n'a pas pu être envoyé.");
        }

        return mapUtilisateursEntityToModele(utilisateur);
    }

}