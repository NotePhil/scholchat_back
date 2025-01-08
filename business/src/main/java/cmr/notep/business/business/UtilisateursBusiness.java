package cmr.notep.business.business;

import cmr.notep.business.exceptions.SchoolException;
import cmr.notep.business.exceptions.enums.SchoolErrorCode;
import cmr.notep.business.services.MailService;
import cmr.notep.interfaces.modeles.*;
import cmr.notep.modele.EtatUtilisateur;
import cmr.notep.ressourcesjpa.commun.DaoAccessorService;
import cmr.notep.ressourcesjpa.dao.*;
import cmr.notep.ressourcesjpa.repository.UtilisateursRepository;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static cmr.notep.business.config.BusinessConfig.dozerMapperBean;

@Component
@Slf4j
public class UtilisateursBusiness {
    private final DaoAccessorService daoAccessorService ;
    private final MailService mailService;

    public UtilisateursBusiness(DaoAccessorService daoAccessorService, MailService mailService) {
        this.daoAccessorService = daoAccessorService;
        this.mailService = mailService;
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

        // Generate activation token
        String activationToken = UUID.randomUUID().toString();
        utilisateur.setActivationToken(activationToken);

        // Save the user with PENDING state and activation token
        UtilisateursEntity savedUserEntity = daoAccessorService.getRepository(UtilisateursRepository.class)
                .save(mapUtilisateursModeleToEntity(utilisateur));

        // Send activation email after successful user creation
        try {
            mailService.sendWelcomeEmail(savedUserEntity.getEmail(), savedUserEntity.getNom(), savedUserEntity.getActivationToken());
            log.info("Activation email sent successfully to {}", savedUserEntity.getEmail());
        } catch (MessagingException e) {
            log.error("Failed to send activation email to {}: {}", savedUserEntity.getEmail(), e.getMessage());
        }

        // Return the created user model
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

    private static UtilisateursEntity mapUtilisateursModeleToEntity(Utilisateurs utilisateur) {
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

    public Utilisateurs avoirUtilisateurParActivationToken(String activationToken) {
        return mapUtilisateursEntityToModele(
                daoAccessorService.getRepository(UtilisateursRepository.class)
                        .findByActivationToken(activationToken)
                        .orElseThrow(() -> new SchoolException(SchoolErrorCode.INVALID_TOKEN, "Token d'activation invalide"))
        );
    }

}