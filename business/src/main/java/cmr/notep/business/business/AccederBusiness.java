package cmr.notep.business.business;

import cmr.notep.business.exceptions.SchoolException;
import cmr.notep.business.exceptions.enums.SchoolErrorCode;
import cmr.notep.business.services.AccessConfirmationEmailService;
import cmr.notep.business.services.AccessRejectionEmailService;
import cmr.notep.business.services.MailServiceInterface;
import cmr.notep.interfaces.modeles.*;
import cmr.notep.modele.EtatClasse;
import cmr.notep.modele.EtatDemandeAcces;
import cmr.notep.ressourcesjpa.commun.DaoAccessorService;
import cmr.notep.ressourcesjpa.dao.*;
import cmr.notep.ressourcesjpa.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static cmr.notep.business.config.BusinessConfig.dozerMapperBean;

@Component
@Slf4j
@Transactional
public class AccederBusiness {

    private final DaoAccessorService daoAccessorService;
    private final AccessConfirmationEmailService accessConfirmationEmailService;
    private final AccessRejectionEmailService accessRejectionEmailService;
    private final MailServiceInterface mailService; // Add this

    public AccederBusiness(DaoAccessorService daoAccessorService,
                           AccessConfirmationEmailService accessConfirmationEmailService,
                           AccessRejectionEmailService accessRejectionEmailService,
                           MailServiceInterface mailService) { // Add this parameter
        this.daoAccessorService = daoAccessorService;
        this.accessConfirmationEmailService = accessConfirmationEmailService;
        this.accessRejectionEmailService = accessRejectionEmailService;
        this.mailService = mailService; // Initialize it
    }

    public void demanderAcces(String utilisateurId, String classeId, String codeActivation) throws SchoolException {
        log.info("Demande d'accès de l'utilisateur {} à la classe {}", utilisateurId, classeId);

        // Check if class exists and is active
        ClassesEntity classe = daoAccessorService.getRepository(ClassesRepository.class)
                .findById(classeId)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Classe introuvable"));

        if (classe.getEtat() != EtatClasse.ACTIF) {
            throw new SchoolException(SchoolErrorCode.INVALID_STATE,
                    "Seules les classes ACTIF peuvent être accessibles");
        }

        // Check if activation code matches
        if (!classe.getCodeActivation().equals(codeActivation)) {
            throw new SchoolException(SchoolErrorCode.INVALID_CODE,
                    "Code d'activation invalide");
        }

        // Check if user exists
        UtilisateursEntity utilisateur = daoAccessorService.getRepository(UtilisateursRepository.class)
                .findById(utilisateurId)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Utilisateur introuvable"));

        // Empêcher les professeurs de faire des demandes d'accès
        if (utilisateur instanceof ProfesseursEntity) {
            throw new SchoolException(SchoolErrorCode.INVALID_OPERATION,
                    "Les professeurs ne peuvent pas faire de demandes d'accès. Utilisez les droits de publication.");
        }

        // Check if access already exists
        if (daoAccessorService.getRepository(AccederRepository.class)
                .existsByUtilisateurIdAndClasseId(utilisateurId, classeId)) {
            throw new SchoolException(SchoolErrorCode.ALREADY_EXISTS,
                    "L'utilisateur a déjà accès à cette classe");
        }

        // Check if there's already a pending request
        Optional<DemandeAccesEntity> existingRequest = daoAccessorService.getRepository(DemandeAccesRepository.class)
                .findByUtilisateurIdAndClasseId(utilisateurId, classeId);

        if (existingRequest.isPresent() && existingRequest.get().getEtat() == EtatDemandeAcces.EN_ATTENTE) {
            throw new SchoolException(SchoolErrorCode.ALREADY_EXISTS,
                    "Une demande d'accès est déjà en attente pour cette classe");
        }

        // Create new access request
        DemandeAccesEntity demande = new DemandeAccesEntity();
        demande.setUtilisateur(utilisateur);
        demande.setClasse(classe);
        demande.setCodeActivation(codeActivation);
        demande.setDateDemande(new Date());
        demande.setEtat(EtatDemandeAcces.EN_ATTENTE);

        daoAccessorService.getRepository(DemandeAccesRepository.class).save(demande);
        log.info("Demande d'accès créée avec succès");

        // Envoyer une notification au modérateur
        if (classe.getModerator() != null) {
            try {
                Utilisateurs moderateur = dozerMapperBean.map(classe.getModerator(), Utilisateurs.class);
                Classes classeDto = dozerMapperBean.map(classe, Classes.class);
                Utilisateurs demandeur = dozerMapperBean.map(utilisateur, Utilisateurs.class);

                // Créer le contenu de l'email
                String subject = "Nouvelle demande d'accès à votre classe " + classe.getNom();
                String content = "L'utilisateur " + demandeur.getPrenom() + " " + demandeur.getNom() +
                        " a demandé l'accès à votre classe " + classe.getNom() +
                        ". Veuillez traiter cette demande dans votre interface modérateur.";

                mailService.sendEmail(moderateur.getEmail(), subject, content);
                log.info("Notification envoyée au modérateur {}", moderateur.getEmail());
            } catch (Exception e) {
                log.error("Erreur lors de l'envoi de la notification au modérateur: {}", e.getMessage());
            }
        }
    }
    public List<DemandeAccesDto> obtenirDemandesAccesPourModerateur(String moderateurId) throws SchoolException {
        log.info("Obtenir toutes les demandes d'accès pour les classes modérées par {}", moderateurId);

        // Vérifier si l'utilisateur est un professeur
        UtilisateursEntity utilisateur = daoAccessorService.getRepository(UtilisateursRepository.class)
                .findById(moderateurId)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Utilisateur introuvable"));

        if (!(utilisateur instanceof ProfesseursEntity)) {
            throw new SchoolException(SchoolErrorCode.INVALID_OPERATION,
                    "Seuls les professeurs peuvent être modérateurs de classe");
        }

        // Récupérer les classes modérées par ce professeur
        List<ClassesEntity> classesModerees = ((ProfesseursEntity) utilisateur).getModeratedClasses();
        if (classesModerees == null || classesModerees.isEmpty()) {
            return Collections.emptyList();
        }

        // Récupérer les demandes pour ces classes
        List<String> classeIds = classesModerees.stream().map(ClassesEntity::getId).collect(Collectors.toList());

        return daoAccessorService.getRepository(DemandeAccesRepository.class)
                .findByClasseIdInAndEtat(classeIds, EtatDemandeAcces.EN_ATTENTE)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }


    public void validerDemandeAcces(String demandeId) throws SchoolException {
        log.info("Validation de la demande d'accès {}", demandeId);

        DemandeAccesEntity demande = daoAccessorService.getRepository(DemandeAccesRepository.class)
                .findById(demandeId)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Demande d'accès introuvable"));

        if (demande.getEtat() != EtatDemandeAcces.EN_ATTENTE) {
            throw new SchoolException(SchoolErrorCode.INVALID_STATE,
                    "Seules les demandes EN_ATTENTE peuvent être validées");
        }

        // Vérifier si l'utilisateur est un professeur
        boolean isProfesseur = demande.getUtilisateur() instanceof ProfesseursEntity;

        if (!isProfesseur) {
            // Grant access seulement pour les non-professeurs
            AccederEntity acceder = new AccederEntity();
            acceder.setUtilisateurId(demande.getUtilisateur().getId());
            acceder.setClasseId(demande.getClasse().getId());
            acceder.setUtilisateur(demande.getUtilisateur());
            acceder.setClasse(demande.getClasse());
            acceder.setDateAcces(new Date());

            daoAccessorService.getRepository(AccederRepository.class).save(acceder);
        }

        // Supprimer la demande d'accès
        daoAccessorService.getRepository(DemandeAccesRepository.class).delete(demande);

        // Send confirmation email
        accessConfirmationEmailService.sendConfirmationEmail(
                dozerMapperBean.map(demande.getUtilisateur(), Utilisateurs.class),
                dozerMapperBean.map(demande.getClasse(), Classes.class)
        );

        log.info("Accès accordé avec succès");
    }

    public void rejeterDemandeAcces(String demandeId, String motifRejet) throws SchoolException {
        log.info("Rejet de la demande d'accès {}", demandeId);

        DemandeAccesEntity demande = daoAccessorService.getRepository(DemandeAccesRepository.class)
                .findById(demandeId)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Demande d'accès introuvable"));

        if (demande.getEtat() != EtatDemandeAcces.EN_ATTENTE) {
            throw new SchoolException(SchoolErrorCode.INVALID_STATE,
                    "Seules les demandes EN_ATTENTE peuvent être rejetées");
        }

        // Vérifier si l'utilisateur a déjà accès (uniquement pour les non-professeurs)
        boolean isProfesseur = demande.getUtilisateur() instanceof ProfesseursEntity;
        boolean hasAccess = false;

        if (!isProfesseur) {
            hasAccess = daoAccessorService.getRepository(AccederRepository.class)
                    .existsByUtilisateurIdAndClasseId(demande.getUtilisateur().getId(), demande.getClasse().getId());
        } else {
            // Pour les professeurs, vérifier s'ils ont des droits de publication
            hasAccess = daoAccessorService.getRepository(DroitPublicationRepository.class)
                    .existsByUtilisateurIdAndClasseId(demande.getUtilisateur().getId(), demande.getClasse().getId());
        }

        if (hasAccess) {
            throw new SchoolException(SchoolErrorCode.ALREADY_EXISTS,
                    "L'utilisateur a déjà accès à cette classe, vous ne pouvez pas rejeter la demande");
        }

        // Update request status
        demande.setEtat(EtatDemandeAcces.REJETEE);
        demande.setDateTraitement(new Date());
        demande.setMotifRejet(motifRejet);
        daoAccessorService.getRepository(DemandeAccesRepository.class).save(demande);

        // Send rejection email
        accessRejectionEmailService.sendRejectionEmail(
                dozerMapperBean.map(demande.getUtilisateur(), Utilisateurs.class),
                dozerMapperBean.map(demande.getClasse(), Classes.class),
                motifRejet
        );

        log.info("Demande d'accès rejetée avec succès");
    }

    public void retirerAcces(String utilisateurId, String classeId) throws SchoolException {
        log.info("Retirer l'accès de l'utilisateur {} à la classe {}", utilisateurId, classeId);

        // Find the access record
        AccederEntity acceder = daoAccessorService.getRepository(AccederRepository.class)
                .findByUtilisateurIdAndClasseId(utilisateurId, classeId)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND,
                        "L'utilisateur n'a pas accès à cette classe"));

        // Delete access
        daoAccessorService.getRepository(AccederRepository.class).delete(acceder);
        log.info("Accès retiré avec succès");
    }

//    public List<Utilisateurs> obtenirUtilisateursAvecAcces(String classeId) throws SchoolException {
//        log.info("Obtenir tous les utilisateurs ayant accès à la classe {}", classeId);
//
//        // Verify class exists
//        if (!daoAccessorService.getRepository(ClassesRepository.class).existsById(classeId)) {
//            throw new SchoolException(SchoolErrorCode.NOT_FOUND, "Classe introuvable");
//        }
//
//        // Récupérer les utilisateurs avec accès direct
//        List<Utilisateurs> utilisateursAcces = daoAccessorService.getRepository(AccederRepository.class)
//                .findByClasseId(classeId)
//                .stream()
//                .map(acceder -> dozerMapperBean.map(acceder.getUtilisateur(), Utilisateurs.class))
//                .collect(Collectors.toList());
//
//        // Récupérer les professeurs avec droits de publication
//        List<Utilisateurs> professeursAvecDroits = daoAccessorService.getRepository(DroitPublicationRepository.class)
//                .findByClasseId(classeId)
//                .stream()
//                .map(droit -> dozerMapperBean.map(droit.getUtilisateur(), Utilisateurs.class))
//                .collect(Collectors.toList());
//
//        // Fusionner les listes et supprimer les doublons
//        List<Utilisateurs> result = new ArrayList<>();
//        result.addAll(utilisateursAcces);
//        result.addAll(professeursAvecDroits);
//
//        return result.stream()
//                .distinct()
//                .collect(Collectors.toList());
//    }


    public List<Utilisateurs> obtenirUtilisateursAvecAcces(String classeId) throws SchoolException {
        log.info("Obtenir tous les utilisateurs ayant accès à la classe {}", classeId);

        // Verify class exists
        if (!daoAccessorService.getRepository(ClassesRepository.class).existsById(classeId)) {
            throw new SchoolException(SchoolErrorCode.NOT_FOUND, "Classe introuvable");
        }

        return daoAccessorService.getRepository(AccederRepository.class)
                .findByClasseId(classeId)
                .stream()
                .map(acceder -> dozerMapperBean.map(acceder.getUtilisateur(), Utilisateurs.class))
                .collect(Collectors.toList());
    }
    public List<Classes> obtenirClassesAccessibles(String utilisateurId) throws SchoolException {
        log.info("Obtenir toutes les classes accessibles par l'utilisateur {}", utilisateurId);

        // Verify user exists
        if (!daoAccessorService.getRepository(UtilisateursRepository.class).existsById(utilisateurId)) {
            throw new SchoolException(SchoolErrorCode.NOT_FOUND, "Utilisateur introuvable");
        }

        return daoAccessorService.getRepository(AccederRepository.class)
                .findByUtilisateurId(utilisateurId)
                .stream()
                .map(acceder -> dozerMapperBean.map(acceder.getClasse(), Classes.class))
                .collect(Collectors.toList());
    }

    public List<Utilisateurs> obtenirUtilisateursAvecAcces(List<String> classeIds) throws SchoolException {
        log.info("Obtenir les utilisateurs ayant accès aux classes {}", classeIds);

        if (classeIds == null || classeIds.isEmpty()) {
            throw new SchoolException(SchoolErrorCode.INVALID_INPUT, "Au moins un ID de classe doit être fourni");
        }

        // Verify all classes exist
        for (String classeId : classeIds) {
            if (!daoAccessorService.getRepository(ClassesRepository.class).existsById(classeId)) {
                throw new SchoolException(SchoolErrorCode.NOT_FOUND, "Classe introuvable avec l'ID: " + classeId);
            }
        }

        // Get users with access to any of the classes
        List<AccederEntity> accesList = daoAccessorService.getRepository(AccederRepository.class)
                .findByClasseIdIn(classeIds);

        // Map to Utilisateurs and remove duplicates
        return accesList.stream()
                .map(acceder -> {
                    if (acceder.getUtilisateur() == null) {
                        log.warn("Utilisateur non trouvé pour l'accès: {}", acceder);
                        return null;
                    }
                    // Use your existing mapping method that preserves types
                    return mapUtilisateursEntityToModele(acceder.getUtilisateur());
                })
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }


    private Utilisateurs mapUtilisateursEntityToModele(UtilisateursEntity entity) {
        if (entity instanceof ProfesseursEntity) {
            return dozerMapperBean.map(entity, Professeurs.class);
        } else if (entity instanceof ElevesEntity) {
            return dozerMapperBean.map(entity, Eleves.class);
        } else if (entity instanceof RepetiteursEntity) {
            return dozerMapperBean.map(entity, Repetiteurs.class);
        } else if (entity instanceof ParentsEntity) {
            return dozerMapperBean.map(entity, Parents.class);
        } else {
            return dozerMapperBean.map(entity, Utilisateurs.class);
        }
    }
    public List<DemandeAccesDto> obtenirDemandesAccesPourClasse(String classeId) throws SchoolException {
        log.info("Obtenir toutes les demandes d'accès pour la classe {}", classeId);

        // Verify class exists
        if (!daoAccessorService.getRepository(ClassesRepository.class).existsById(classeId)) {
            throw new SchoolException(SchoolErrorCode.NOT_FOUND, "Classe introuvable");
        }

        return daoAccessorService.getRepository(DemandeAccesRepository.class)
                .findByClasseId(classeId)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private DemandeAccesDto convertToDto(DemandeAccesEntity entity) {
        return DemandeAccesDto.builder()
                .id(entity.getId())
                .utilisateurId(entity.getUtilisateur().getId())
                .utilisateurNom(entity.getUtilisateur().getNom())
                .utilisateurPrenom(entity.getUtilisateur().getPrenom())
                .utilisateurEmail(entity.getUtilisateur().getEmail())
                .classeId(entity.getClasse().getId())
                .classeNom(entity.getClasse().getNom())
                .codeActivation(entity.getCodeActivation())
                .etat(entity.getEtat().name())
                .dateDemande(entity.getDateDemande())
                .dateTraitement(entity.getDateTraitement())
                .motifRejet(entity.getMotifRejet())
                .build();
    }

    public List<DemandeAccesDto> obtenirDemandesAccesEnAttentePourClasse(String classeId) throws SchoolException {
        log.info("Obtenir les demandes d'accès en attente pour la classe {}", classeId);

        // Verify class exists
        if (!daoAccessorService.getRepository(ClassesRepository.class).existsById(classeId)) {
            throw new SchoolException(SchoolErrorCode.NOT_FOUND, "Classe introuvable");
        }

        return daoAccessorService.getRepository(DemandeAccesRepository.class)
                .findByClasseIdAndEtat(classeId, EtatDemandeAcces.EN_ATTENTE)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
}