package cmr.notep.business.business;

import cmr.notep.business.security.UserSubtypeService;

import cmr.notep.business.exceptions.SchoolException;
import cmr.notep.business.exceptions.enums.SchoolErrorCode;
import cmr.notep.business.services.MailServiceInterface;
import cmr.notep.business.services.NotificationService;
import cmr.notep.interfaces.dto.ClasseInfoDto;
import cmr.notep.interfaces.dto.EleveInfoDto;
import cmr.notep.interfaces.dto.ParentAccessRequestDto;
import cmr.notep.interfaces.modeles.*;
import cmr.notep.modele.EtatClasse;
import cmr.notep.modele.EtatDemandeAcces;
import cmr.notep.modele.EtatUtilisateur;
import cmr.notep.ressourcesjpa.commun.DaoAccessorService;
import cmr.notep.ressourcesjpa.dao.*;
import cmr.notep.ressourcesjpa.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

import static cmr.notep.business.config.BusinessConfig.dozerMapperBean;

@Component
@Slf4j
public class ParentAccessBusiness {

    private final UserSubtypeService userSubtypeService;
    private final DaoAccessorService daoAccessorService;
    private final MailServiceInterface mailService;
    private final NotificationService notificationService;
    private final UtilisateursBusiness utilisateursBusiness;

    @org.springframework.beans.factory.annotation.Autowired
    private cmr.notep.business.services.EmailTemplateService emailTemplateService;

    public ParentAccessBusiness(DaoAccessorService daoAccessorService, MailServiceInterface mailService,
                                NotificationService notificationService, UtilisateursBusiness utilisateursBusiness,
            UserSubtypeService userSubtypeService) {
        this.userSubtypeService = userSubtypeService;
        this.daoAccessorService = daoAccessorService;
        this.mailService = mailService;
        this.notificationService = notificationService;
        this.utilisateursBusiness = utilisateursBusiness;
    }

    public ClasseInfoDto validerTokenEtRecupererInfos(String token, String classeId) throws SchoolException {
        ClassesEntity classe = daoAccessorService.getRepository(ClassesRepository.class)
                .findByActivationTokenAndEtat(token, EtatClasse.ACTIF)
                .stream()
                .filter(c -> c.getId().equals(classeId))
                .findFirst()
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.INVALID_TOKEN, "Invalid token or class not found"));

        ClasseInfoDto response = new ClasseInfoDto();
        response.setClasseId(classe.getId());
        response.setNomClasse(classe.getNom());
        response.setNiveau(classe.getNiveau());
        response.setAccesMajeur(classe.isAccesMajeur()); // Still include this field but don't use it for logic

        if (classe.getModerator() != null) {
            response.setModerateurNom(classe.getModerator().getNom());
            response.setModerateurPrenom(classe.getModerator().getPrenom());
        }

        // Get students from classe_eleves table only
        List<EleveInfoDto> eleves = daoAccessorService.getRepository(UtilisateursRepository.class)
                .findByClasseId(classeId)
                .stream()
                .filter(utilisateur -> userSubtypeService.isEleve(utilisateur.getId()))
                .map(eleve -> {
                    EleveInfoDto dto = new EleveInfoDto();
                    dto.setId(eleve.getId());
                    dto.setNom(eleve.getNom());
                    dto.setPrenom(eleve.getPrenom());
                    dto.setEmail(eleve.getEmail());
                    return dto;
                })
                .collect(Collectors.toList());

        response.setElevesAssocies(eleves);
        return response;
    }

    public void traiterDemandeAcces(ParentAccessRequestDto request) throws SchoolException {
        ClassesEntity classe = daoAccessorService.getRepository(ClassesRepository.class)
                .findById(request.getClasseId())
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Classe non trouvée"));

        // Find user - they may be a professor with parent role added via native SQL
        UtilisateursEntity parentUser = daoAccessorService.getRepository(UtilisateursRepository.class)
                .findById(request.getParentId())
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Utilisateur non trouvé"));

        // Règles de combinaison des profils (un élève mineur géré par un parent ne peut pas devenir parent)
        utilisateursBusiness.verifierCompatibiliteRole(parentUser, "PARENT");

        // Ligne parents : vérifiée en natif (compte multi-rôles : ParentsRepository.findById peut
        // renvoyer vide si le compte est déjà chargé sous un autre sous-type), créée si absente.
        if (!userSubtypeService.isParent(request.getParentId())) {
            // Verify parent row exists in DB
            try {
                daoAccessorService.getRepository(UtilisateursRepository.class)
                        .insertParentRole(request.getParentId());
            } catch (Exception e) {
                // Already exists, ignore
            }
            // Use the user entity directly - create demande with user ID
        }

        // If elevesIds are provided (parent selected their children), use majeur flow regardless
        if (request.getElevesIds() != null && !request.getElevesIds().isEmpty()) {
            traiterAccesMajeurWithUser(request, classe, parentUser);
        } else if (request.getElevesNoms() != null && !request.getElevesNoms().isEmpty()) {
            traiterAccesMineurWithUser(request, classe, parentUser);
        } else {
            throw new SchoolException(SchoolErrorCode.INVALID_INPUT,
                    "Veuillez sélectionner au moins un enfant");
        }

        notifierModerateurWithUser(classe, parentUser,
                request.getElevesIds() != null ? request.getElevesIds() : Collections.emptyList(),
                request.getElevesNoms() != null ? request.getElevesNoms() : Collections.emptyList());
    }

    private void traiterAccesMajeur(ParentAccessRequestDto request, ClassesEntity classe, ParentsEntity parent) throws SchoolException {
        if (request.getElevesIds() == null || request.getElevesIds().isEmpty()) {
            throw new SchoolException(SchoolErrorCode.INVALID_INPUT,
                    "Au moins un élève doit être associé pour les classes avec accès majeur");
        }

        for (String eleveId : request.getElevesIds()) {
            // Create access request for the student too (if they don't already have access)
            boolean studentHasAccess = daoAccessorService.getRepository(AccederRepository.class)
                    .existsByUtilisateurIdAndClasseId(eleveId, classe.getId());
            if (!studentHasAccess) {
                // Create access request for the student
                creerDemandeAcces(eleveId, classe.getId(), classe.getCodeActivation(), false, null);
            }

            // Create the access request for the parent (linked to this student)
            creerDemandeAcces(parent.getId(), classe.getId(), classe.getCodeActivation(), true, eleveId);
        }
    }

    private void traiterAccesMineur(ParentAccessRequestDto request, ClassesEntity classe, ParentsEntity parent) throws SchoolException {
        if (request.getElevesNoms() == null || request.getElevesNoms().isEmpty()) {
            throw new SchoolException(SchoolErrorCode.INVALID_INPUT,
                    "Au moins un élève doit être ajouté pour les classes avec accès mineur");
        }

        for (String eleveNom : request.getElevesNoms()) {
            String[] nameParts = eleveNom.trim().split(" ", 2);
            String nom = nameParts.length > 0 ? nameParts[0] : "";
            String prenom = nameParts.length > 1 ? nameParts[1] : "";

            // Créer l'élève
            ElevesEntity eleve = new ElevesEntity();
            eleve.setId(UUID.randomUUID().toString());
            eleve.setNom(nom);
            eleve.setPrenom(prenom);
            eleve.setNiveau(classe.getNiveau());
            eleve.setEtat(EtatUtilisateur.PENDING);
            eleve = daoAccessorService.getRepository(ElevesRepository.class).save(eleve);

            // Créer la demande pour l'élève
            creerDemandeAcces(eleve.getId(), classe.getId(), classe.getCodeActivation(), false, null);

            // Créer la demande pour le parent (avec référence à l'élève)
            creerDemandeAcces(parent.getId(), classe.getId(), classe.getCodeActivation(), true, eleve.getId());
        }
    }
    private void traiterAccesMajeurWithUser(ParentAccessRequestDto request, ClassesEntity classe, UtilisateursEntity parentUser) throws SchoolException {
        if (request.getElevesIds() == null || request.getElevesIds().isEmpty()) {
            throw new SchoolException(SchoolErrorCode.INVALID_INPUT,
                    "Au moins un élève doit être sélectionné");
        }

        for (String eleveId : request.getElevesIds()) {
            boolean studentHasAccess = daoAccessorService.getRepository(AccederRepository.class)
                    .existsByUtilisateurIdAndClasseId(eleveId, classe.getId());
            if (!studentHasAccess) {
                creerDemandeAcces(eleveId, classe.getId(), classe.getCodeActivation(), false, null);
            }
            creerDemandeAcces(parentUser.getId(), classe.getId(), classe.getCodeActivation(), true, eleveId);
        }
    }

    private void traiterAccesMineurWithUser(ParentAccessRequestDto request, ClassesEntity classe, UtilisateursEntity parentUser) throws SchoolException {
        if (request.getElevesNoms() == null || request.getElevesNoms().isEmpty()) {
            throw new SchoolException(SchoolErrorCode.INVALID_INPUT,
                    "Au moins un élève doit être ajouté");
        }

        for (String eleveNom : request.getElevesNoms()) {
            String[] nameParts = eleveNom.trim().split(" ", 2);
            String nom = nameParts.length > 0 ? nameParts[0] : "";
            String prenom = nameParts.length > 1 ? nameParts[1] : "";

            ElevesEntity eleve = new ElevesEntity();
            eleve.setId(UUID.randomUUID().toString());
            eleve.setNom(nom);
            eleve.setPrenom(prenom);
            eleve.setNiveau(classe.getNiveau());
            eleve.setEtat(EtatUtilisateur.PENDING);
            eleve = daoAccessorService.getRepository(ElevesRepository.class).save(eleve);

            creerDemandeAcces(eleve.getId(), classe.getId(), classe.getCodeActivation(), false, null);
            creerDemandeAcces(parentUser.getId(), classe.getId(), classe.getCodeActivation(), true, eleve.getId());
        }
    }

    private void notifierModerateurWithUser(ClassesEntity classe, UtilisateursEntity parentUser,
                                            List<String> elevesIds, List<String> elevesNoms) {
        try {
            if (classe.getModerator() != null) {
                String parentName = parentUser.getPrenom() + " " + parentUser.getNom();
                String moderatorId = classe.getModerator().getId();
                notificationService.createAccessRequestNotification(
                        classe.getId(), classe.getNom(), moderatorId, parentName);
            }
        } catch (Exception e) {
            log.error("Error notifying moderator: {}", e.getMessage());
        }
    }

    private void creerDemandeAcces(String utilisateurId, String classeId, String codeActivation,
                                   boolean estParent, String eleveAssocieId) {
        if (!daoAccessorService.getRepository(DemandeAccesRepository.class)
                .existsByUtilisateurIdAndClasseId(utilisateurId, classeId)) {
            DemandeAccesEntity demande = new DemandeAccesEntity();
            demande.setId(UUID.randomUUID().toString());
            demande.setUtilisateur(daoAccessorService.getRepository(UtilisateursRepository.class)
                    .findById(utilisateurId).orElseThrow());
            demande.setClasse(daoAccessorService.getRepository(ClassesRepository.class)
                    .findById(classeId).orElseThrow());
            demande.setCodeActivation(codeActivation);
            demande.setDateDemande(new Date());
            demande.setEtat(EtatDemandeAcces.EN_ATTENTE);
            demande.setEstParent(estParent);
            demande.setEleveAssocieId(eleveAssocieId);
            daoAccessorService.getRepository(DemandeAccesRepository.class).save(demande);

            // Send in-app notifications
            try {
                UtilisateursEntity user = demande.getUtilisateur();
                String userName = user.getPrenom() + " " + user.getNom();
                ClassesEntity classe = demande.getClasse();
                notificationService.createAccessRequestNotification(classeId, classe.getNom(), utilisateurId, userName);
            } catch (Exception e) {
                log.error("Error sending parent access request notification: {}", e.getMessage());
            }
        }
    }
    private void notifierModerateur(ClassesEntity classe, ParentsEntity parent,
                                    List<String> elevesIds, List<String> elevesNoms) {
        if (classe.getModerator() != null) {
            try {
                String subject = "Nouvelle demande d'accès parent pour la classe " + classe.getNom();
                List<String> eleves = new ArrayList<>();
                if (classe.isAccesMajeur()) {
                    for (String eleveId : elevesIds == null ? List.<String>of() : elevesIds) {
                        ElevesEntity eleve = daoAccessorService.getRepository(ElevesRepository.class)
                                .findById(eleveId).orElse(null);
                        if (eleve != null) {
                            eleves.add(eleve.getPrenom() + " " + eleve.getNom());
                        }
                    }
                } else {
                    if (elevesNoms != null) eleves.addAll(elevesNoms);
                }
                String content = emailTemplateService.generateNotificationGeneriqueEmail(
                        "Nouvelle demande d'accès parent",
                        List.of("Bonjour " + (classe.getModerator().getPrenom() == null ? "" : classe.getModerator().getPrenom()) + ",",
                                "Le parent " + parent.getPrenom() + " " + parent.getNom()
                                        + " a fait une demande d'accès pour la classe " + classe.getNom() + ".",
                                classe.isAccesMajeur() ? "Élèves associés :" : "Nouveaux élèves proposés :"),
                        eleves);

                mailService.sendEmail(classe.getModerator().getEmail(), subject, content);
            } catch (Exception e) {
                log.error("Erreur lors de l'envoi de la notification au modérateur", e);
            }
        }
    }
}