package cmr.notep.business.business;

import cmr.notep.business.exceptions.SchoolException;
import cmr.notep.business.exceptions.enums.SchoolErrorCode;
import cmr.notep.interfaces.modeles.Messages;
import cmr.notep.ressourcesjpa.commun.DaoAccessorService;
import cmr.notep.ressourcesjpa.dao.MessagesEntity;
import cmr.notep.ressourcesjpa.dao.UtilisateursEntity;
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

        // Fetch destinataires by IDs if they are provided
        List<UtilisateursEntity> destinatairesEntities = new ArrayList<>();
        if (message.getDestinataires() != null && !message.getDestinataires().isEmpty()) {
            destinatairesEntities = message.getDestinataires().stream()
                    .map(destinataireId -> daoAccessorService.getRepository(UtilisateursRepository.class)
                            .findById(destinataireId)
                            .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Destinataire introuvable avec l'ID: " + destinataireId)))
                    .collect(Collectors.toList());
        }

        // Create and save the message
        MessagesEntity messageEntity = new MessagesEntity();
        messageEntity.setContenu(message.getContenu());
        messageEntity.setDateCreation(message.getDateCreation());
        messageEntity.setDateModification(message.getDateModification());
        messageEntity.setEtat(message.getEtat());
        messageEntity.setExpediteurEntity(expediteurEntity);
        messageEntity.setDestinatairesEntities(destinatairesEntities);
        messageEntity.setClasseIds(message.getClasseIds());

        // Save the message first to generate ID
        MessagesEntity savedMessageEntity = daoAccessorService.getRepository(MessagesRepository.class).save(messageEntity);

        // Update the recipients' messagesRecusEntities
        for (UtilisateursEntity destinataire : destinatairesEntities) {
            if (destinataire.getMessagesRecusEntities() == null) {
                destinataire.setMessagesRecusEntities(new ArrayList<>());
            }
            destinataire.getMessagesRecusEntities().add(savedMessageEntity);
            daoAccessorService.getRepository(UtilisateursRepository.class).save(destinataire);
        }

        // Map back to Messages
        Messages savedMessage = new Messages();
        savedMessage.setId(savedMessageEntity.getId());
        savedMessage.setContenu(savedMessageEntity.getContenu());
        savedMessage.setDateCreation(savedMessageEntity.getDateCreation());
        savedMessage.setDateModification(savedMessageEntity.getDateModification());
        savedMessage.setEtat(savedMessageEntity.getEtat());
        savedMessage.setExpediteur(savedMessageEntity.getExpediteurEntity().getId());
        savedMessage.setDestinataires(savedMessageEntity.getDestinatairesEntities().stream()
                .map(UtilisateursEntity::getId)
                .collect(Collectors.toList()));
        savedMessage.setClasseIds(savedMessageEntity.getClasseIds());

        return savedMessage;
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
