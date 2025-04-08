package cmr.notep.business.business;

import cmr.notep.business.exceptions.SchoolErrorEmail;
import cmr.notep.business.exceptions.SchoolException;
import cmr.notep.business.exceptions.enums.SchoolErrorCode;
import cmr.notep.business.services.ActivationEmailService;
import cmr.notep.business.services.RoleService;
import cmr.notep.business.services.UserValidationService;
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
@Transactional(noRollbackFor = SchoolErrorEmail.class)
public class UtilisateursBusiness {
    private final DaoAccessorService daoAccessorService ;
    private final ActivationEmailService activationEmailService;
    private final JwtUtil jwtUtil;
    private final RoleService roleService;
    private final UserValidationService userValidationService;

    public UtilisateursBusiness(DaoAccessorService daoAccessorService, ActivationEmailService activationEmailService, JwtUtil jwtUtil, RoleService roleService,  UserValidationService userValidationService) {
        this.daoAccessorService = daoAccessorService;
        this.activationEmailService = activationEmailService;
        this.jwtUtil = jwtUtil;
        this.roleService = roleService;
        this.userValidationService = userValidationService;
    }

    public Utilisateurs avoirUtilisateur(String idUtilisateur) {
        log.info("Récupération de l'utilisateur avec ID: {}", idUtilisateur);
        return mapUtilisateursEntityToModele(daoAccessorService.getRepository(UtilisateursRepository.class)
                .findById(idUtilisateur)
                .orElseThrow(()-> new SchoolException(SchoolErrorCode.NOT_FOUND, "Utilisateur introuvable avec l'ID: " + idUtilisateur)));
    }

    @Transactional
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

        UtilisateursEntity userEntity = daoAccessorService.getRepository(UtilisateursRepository.class)
                .findById(professorId)
                .orElseThrow(() -> new SchoolException(
                        SchoolErrorCode.NOT_FOUND,
                        "Professeur introuvable avec l'ID: " + professorId
                ));

        if (!(userEntity instanceof ProfesseursEntity)) {
            throw new SchoolException(
                    SchoolErrorCode.INVALID_OPERATION,
                    "L'utilisateur n'est pas un professeur"
            );
        }

        if (userEntity.getEtat() != EtatUtilisateur.AWAITING_VALIDATION) {
            throw new SchoolException(
                    SchoolErrorCode.INVALID_STATE,
                    "Le professeur n'est pas en attente de validation"
            );
        }

        userEntity.setEtat(EtatUtilisateur.VALIDATED);
        userEntity = daoAccessorService.getRepository(UtilisateursRepository.class).save(userEntity);

        Utilisateurs utilisateur = mapUtilisateursEntityToModele(userEntity);

        // Générer un nouveau token avec les rôles mis à jour
        List<String> roles = roleService.determineUserRoles(utilisateur);
        String activationToken = jwtUtil.generateAccessToken(userEntity.getEmail(), roles);
        userEntity.setActivationToken(activationToken);
        daoAccessorService.getRepository(UtilisateursRepository.class).save(userEntity);

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
}