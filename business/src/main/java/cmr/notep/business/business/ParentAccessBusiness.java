package cmr.notep.business.business;

import cmr.notep.business.exceptions.SchoolException;
import cmr.notep.business.exceptions.enums.SchoolErrorCode;
import cmr.notep.business.services.MailServiceInterface;
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

    public ParentAccessBusiness(DaoAccessorService daoAccessorService, MailServiceInterface mailService) {
        this.daoAccessorService = daoAccessorService;
        this.mailService = mailService;
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
        response.setAccesMajeur(classe.isAccesMajeur());

        if (classe.getModerator() != null) {
            response.setModerateurNom(classe.getModerator().getNom());
            response.setModerateurPrenom(classe.getModerator().getPrenom());
        }

        List<EleveInfoDto> eleves = new ArrayList<>();

        if (classe.isAccesMajeur()) {
            eleves = daoAccessorService.getRepository(AccederRepository.class)
                    .findByClasseId(classe.getId())
                    .stream()
                    .map(acceder -> {
                        ElevesEntity eleve = daoAccessorService.getRepository(ElevesRepository.class)
                                .findById(acceder.getUtilisateurId())
                                .orElse(null);
                        if (eleve != null) {
                            EleveInfoDto dto = new EleveInfoDto();
                            dto.setId(eleve.getId());
                            dto.setNom(eleve.getNom());
                            dto.setPrenom(eleve.getPrenom());
                            dto.setEmail(eleve.getEmail());
                            return dto;
                        }
                        return null;
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } else {
            eleves = daoAccessorService.getRepository(DemandeAccesRepository.class)
                    .findByClasseIdAndEtat(classe.getId(), EtatDemandeAcces.EN_ATTENTE)
                    .stream()
                    .map(demande -> {
                        UtilisateursEntity utilisateur = demande.getUtilisateur();
                        if (utilisateur instanceof ElevesEntity) {
                            ElevesEntity eleve = (ElevesEntity) utilisateur;
                            EleveInfoDto dto = new EleveInfoDto();
                            dto.setId(eleve.getId());
                            dto.setNom(eleve.getNom());
                            dto.setPrenom(eleve.getPrenom());
                            dto.setEmail("Pending approval");
                            return dto;
                        }
                        return null;
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }

        response.setElevesAssocies(eleves);
        return response;
    }

    public void traiterDemandeAcces(ParentAccessRequestDto request) throws SchoolException {
        ClassesEntity classe = daoAccessorService.getRepository(ClassesRepository.class)
                .findById(request.getClasseId())
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Class not found"));

        ParentsEntity parent = daoAccessorService.getRepository(ParentsRepository.class)
                .findById(request.getParentId())
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Parent not found"));

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
                    "At least one student must be associated for classes with major access");
        }

        for (String eleveId : request.getElevesIds()) {
            if (!daoAccessorService.getRepository(AccederRepository.class)
                    .existsByUtilisateurIdAndClasseId(eleveId, classe.getId())) {
                throw new SchoolException(SchoolErrorCode.INVALID_OPERATION,
                        "Student " + eleveId + " does not have access to this class");
            }

            if (!daoAccessorService.getRepository(ParentEleveRepository.class)
                    .existsByParentIdAndEleveId(parent.getId(), eleveId)) {
                ParentEleveEntity relation = new ParentEleveEntity();
                relation.setParentId(parent.getId());
                relation.setEleveId(eleveId);
                daoAccessorService.getRepository(ParentEleveRepository.class).save(relation);
            }
        }
    }

    private void traiterAccesMineur(ParentAccessRequestDto request, ClassesEntity classe, ParentsEntity parent) throws SchoolException {
        if (request.getElevesNoms() == null || request.getElevesNoms().isEmpty()) {
            throw new SchoolException(SchoolErrorCode.INVALID_INPUT,
                    "At least one student must be added for classes with minor access");
        }

        for (String eleveNom : request.getElevesNoms()) {
            String[] nameParts = eleveNom.trim().split(" ", 2);
            String nom = nameParts.length > 0 ? nameParts[0] : "";
            String prenom = nameParts.length > 1 ? nameParts[1] : "";

            // Create student
            ElevesEntity eleve = new ElevesEntity();
            eleve.setId(UUID.randomUUID().toString());
            eleve.setNom(nom);
            eleve.setPrenom(prenom);
            eleve.setNiveau(classe.getNiveau());
            eleve.setEtat(EtatUtilisateur.PENDING);
            eleve = daoAccessorService.getRepository(ElevesRepository.class).save(eleve);

            // Create parent-student relationship
            ParentEleveEntity relation = new ParentEleveEntity();
            relation.setParentId(parent.getId());
            relation.setEleveId(eleve.getId());
            daoAccessorService.getRepository(ParentEleveRepository.class).save(relation);

            // Create access request
            DemandeAccesEntity demande = new DemandeAccesEntity();
            demande.setId(UUID.randomUUID().toString());
            demande.setUtilisateur(eleve);
            demande.setClasse(classe);
            demande.setCodeActivation(classe.getCodeActivation());
            demande.setDateDemande(new Date());
            demande.setEtat(EtatDemandeAcces.EN_ATTENTE);
            daoAccessorService.getRepository(DemandeAccesRepository.class).save(demande);
        }
    }

    private void notifierModerateur(ClassesEntity classe, ParentsEntity parent, List<String> elevesIds, List<String> elevesNoms) {
        if (classe.getModerator() != null) {
            try {
                Utilisateurs moderateur = dozerMapperBean.map(classe.getModerator(), Utilisateurs.class);
                Classes classeDto = dozerMapperBean.map(classe, Classes.class);
                Utilisateurs parentDto = dozerMapperBean.map(parent, Utilisateurs.class);

                String subject = "New parent access request for the class " + classe.getNom();
                StringBuilder content = new StringBuilder();
                content.append("The parent ").append(parentDto.getPrenom()).append(" ").append(parentDto.getNom())
                        .append(" has requested access to your class ").append(classe.getNom()).append(".\n\n");

                if (classe.isAccesMajeur()) {
                    content.append("Associated students:\n");
                    for (String eleveId : elevesIds) {
                        ElevesEntity eleve = daoAccessorService.getRepository(ElevesRepository.class)
                                .findById(eleveId)
                                .orElse(null);
                        if (eleve != null) {
                            content.append("- ").append(eleve.getPrenom()).append(" ").append(eleve.getNom()).append("\n");
                        }
                    }
                } else {
                    content.append("New students to create (without email):\n");
                    for (String eleveNom : elevesNoms) {
                        content.append("- ").append(eleveNom).append("\n");
                    }
                }

                content.append("\nPlease process this request in your moderator interface.");
                mailService.sendEmail(moderateur.getEmail(), subject, content.toString());
                log.info("Notification sent to moderator {}", moderateur.getEmail());
            } catch (Exception e) {
                log.error("Error sending notification to moderator: {}", e.getMessage());
            }
        }
    }
}