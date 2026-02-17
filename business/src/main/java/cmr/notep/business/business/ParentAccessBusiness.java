package cmr.notep.business.business;

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

    private final DaoAccessorService daoAccessorService;
    private final MailServiceInterface mailService;
    private final NotificationService notificationService;

    public ParentAccessBusiness(DaoAccessorService daoAccessorService, MailServiceInterface mailService,
                                NotificationService notificationService) {
        this.daoAccessorService = daoAccessorService;
        this.mailService = mailService;
        this.notificationService = notificationService;
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
                .filter(utilisateur -> utilisateur instanceof ElevesEntity)
                .map(utilisateur -> {
                    ElevesEntity eleve = (ElevesEntity) utilisateur;
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

        ParentsEntity parent = daoAccessorService.getRepository(ParentsRepository.class)
                .findById(request.getParentId())
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Parent non trouvé"));

        if (classe.isAccesMajeur()) {
            traiterAccesMajeur(request, classe, parent);
        } else {
            traiterAccesMineur(request, classe, parent);
        }

        notifierModerateur(classe, parent,
                request.getElevesIds() != null ? request.getElevesIds() : Collections.emptyList(),
                request.getElevesNoms() != null ? request.getElevesNoms() : Collections.emptyList());
    }

    private void traiterAccesMajeur(ParentAccessRequestDto request, ClassesEntity classe, ParentsEntity parent) throws SchoolException {
        if (request.getElevesIds() == null || request.getElevesIds().isEmpty()) {
            throw new SchoolException(SchoolErrorCode.INVALID_INPUT,
                    "Au moins un élève doit être associé pour les classes avec accès majeur");
        }

        for (String eleveId : request.getElevesIds()) {
            // Vérifier que l'élève a déjà accès à la classe
            if (!daoAccessorService.getRepository(AccederRepository.class)
                    .existsByUtilisateurIdAndClasseId(eleveId, classe.getId())) {
                throw new SchoolException(SchoolErrorCode.INVALID_OPERATION,
                        "L'élève " + eleveId + " n'a pas accès à cette classe");
            }

            // Créer la demande pour le parent (avec référence à l'élève)
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
                StringBuilder content = new StringBuilder();
                content.append("Le parent ").append(parent.getPrenom()).append(" ").append(parent.getNom())
                        .append(" a fait une demande d'accès pour la classe ").append(classe.getNom()).append("\n\n");

                if (classe.isAccesMajeur()) {
                    content.append("Élèves associés :\n");
                    for (String eleveId : elevesIds) {
                        ElevesEntity eleve = daoAccessorService.getRepository(ElevesRepository.class)
                                .findById(eleveId).orElse(null);
                        if (eleve != null) {
                            content.append("- ").append(eleve.getPrenom()).append(" ").append(eleve.getNom()).append("\n");
                        }
                    }
                } else {
                    content.append("Nouveaux élèves proposés :\n");
                    for (String eleveNom : elevesNoms) {
                        content.append("- ").append(eleveNom).append("\n");
                    }
                }

                mailService.sendEmail(classe.getModerator().getEmail(), subject, content.toString());
            } catch (Exception e) {
                log.error("Erreur lors de l'envoi de la notification au modérateur", e);
            }
        }
    }
}