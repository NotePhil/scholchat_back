package cmr.notep.business.business;

import cmr.notep.business.exceptions.SchoolException;
import cmr.notep.business.exceptions.enums.SchoolErrorCode;
import cmr.notep.interfaces.modeles.Messages;
import cmr.notep.ressourcesjpa.commun.DaoAccessorService;
import cmr.notep.ressourcesjpa.dao.ClassesEntity;
import cmr.notep.ressourcesjpa.dao.MessagesEntity;
import cmr.notep.ressourcesjpa.dao.UtilisateursEntity;
import cmr.notep.ressourcesjpa.repository.ClassesRepository;
import cmr.notep.ressourcesjpa.repository.MessagesRepository;
import cmr.notep.ressourcesjpa.repository.UtilisateursRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static cmr.notep.business.config.BusinessConfig.dozerMapperBean;

@Component
@Slf4j
public class MessagesBusiness {
    private final DaoAccessorService daoAccessorService;

    public MessagesBusiness(DaoAccessorService daoAccessorService) {
        this.daoAccessorService = daoAccessorService;
    }

    public Messages avoirMessage(String idMessage) {
        return dozerMapperBean.map(daoAccessorService.getRepository(MessagesRepository.class)
                .findById(idMessage)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Message introuvable avec l'ID: " + idMessage)), Messages.class);
    }

    public Messages posterMessage(Messages message) {
        // Fetch expediteur by ID
        UtilisateursEntity expediteurEntity = daoAccessorService.getRepository(UtilisateursRepository.class)
                .findById(message.getExpediteur())
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Expediteur introuvable avec l'ID: " + message.getExpediteur()));

        // Create message entity
        MessagesEntity messageEntity = new MessagesEntity();
        messageEntity.setContenu(message.getContenu());
        messageEntity.setDateCreation(message.getDateCreation());
        messageEntity.setDateModification(message.getDateModification());
        messageEntity.setEtat(message.getEtat());
        messageEntity.setExpediteurEntity(expediteurEntity);
        messageEntity.setClasseIds(message.getClasseIds());

        // Get all users from the classes (students, parents, moderator)
        List<UtilisateursEntity> destinatairesEntities = new ArrayList<>();
        if (message.getClasseIds() != null && !message.getClasseIds().isEmpty()) {
            ClassesRepository classesRepository = daoAccessorService.getRepository(ClassesRepository.class);
            for (String classeId : message.getClasseIds()) {
                ClassesEntity classe = classesRepository.findById(classeId)
                        .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Classe introuvable avec l'ID: " + classeId));

                // Add students
                if (classe.getElevesEntities() != null) {
                    destinatairesEntities.addAll(classe.getElevesEntities());
                }

                // Add parents
                if (classe.getParentsEntities() != null) {
                    destinatairesEntities.addAll(classe.getParentsEntities());
                }

                // Add moderator if exists
                if (classe.getModerator() != null) {
                    destinatairesEntities.add(classe.getModerator());
                }
            }
        }

        // Add explicit recipients if provided
        if (message.getDestinataires() != null && !message.getDestinataires().isEmpty()) {
            for (String destinataireId : message.getDestinataires()) {
                UtilisateursEntity destinataire = daoAccessorService.getRepository(UtilisateursRepository.class)
                        .findById(destinataireId)
                        .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Destinataire introuvable avec l'ID: " + destinataireId));
                if (!destinatairesEntities.contains(destinataire)) {
                    destinatairesEntities.add(destinataire);
                }
            }
        }

        // Set recipients and save message
        messageEntity.setDestinatairesEntities(destinatairesEntities);
        MessagesEntity savedMessageEntity = daoAccessorService.getRepository(MessagesRepository.class).save(messageEntity);

        // Update recipients' messagesRecusEntities
        for (UtilisateursEntity destinataire : destinatairesEntities) {
            if (destinataire.getMessagesRecusEntities() == null) {
                destinataire.setMessagesRecusEntities(new ArrayList<>());
            }
            destinataire.getMessagesRecusEntities().add(savedMessageEntity);
            daoAccessorService.getRepository(UtilisateursRepository.class).save(destinataire);
        }

        // Map back to Messages
        return mapMessageEntityToModel(savedMessageEntity);
    }

    private Messages mapMessageEntityToModel(MessagesEntity entity) {
        Messages message = new Messages();
        message.setId(entity.getId());
        message.setContenu(entity.getContenu());
        message.setDateCreation(entity.getDateCreation());
        message.setDateModification(entity.getDateModification());
        message.setEtat(entity.getEtat());
        message.setExpediteur(entity.getExpediteurEntity().getId());
        message.setDestinataires(entity.getDestinatairesEntities().stream()
                .map(UtilisateursEntity::getId)
                .collect(Collectors.toList()));
        message.setClasseIds(entity.getClasseIds());
        return message;
    }

    public List<Messages> avoirToutMessages() {
        return daoAccessorService.getRepository(MessagesRepository.class).findAll()
                .stream()
                .map(msg -> {
                    Messages message = new Messages();
                    message.setId(msg.getId());
                    message.setContenu(msg.getContenu());
                    message.setDateCreation(msg.getDateCreation());
                    message.setDateModification(msg.getDateModification());
                    message.setEtat(msg.getEtat());
                    message.setExpediteur(msg.getExpediteurEntity().getId());

                    // Log destinatairesEntities
                    List<String> destinatairesIds = msg.getDestinatairesEntities() != null ?
                            msg.getDestinatairesEntities().stream()
                                    .map(UtilisateursEntity::getId)
                                    .collect(Collectors.toList()) : Collections.emptyList();
                    log.info("Destinataires IDs for message {}: {}", msg.getId(), destinatairesIds);
                    message.setDestinataires(destinatairesIds);

                    // Log classeIds
                    List<String> classeIds = msg.getClasseIds() != null ?
                            msg.getClasseIds() : Collections.emptyList();
                    log.info("Classe IDs for message {}: {}", msg.getId(), classeIds);
                    message.setClasseIds(classeIds);

                    return message;
                })
                .collect(Collectors.toList());
    }

}
