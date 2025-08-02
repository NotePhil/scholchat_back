package cmr.notep.business.business;

import cmr.notep.business.exceptions.SchoolException;
import cmr.notep.business.exceptions.enums.SchoolErrorCode;
import cmr.notep.interfaces.dto.GroupMessageDto;
import cmr.notep.interfaces.modeles.Messages;
import cmr.notep.ressourcesjpa.commun.DaoAccessorService;
import cmr.notep.ressourcesjpa.dao.*;
import cmr.notep.ressourcesjpa.repository.*;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static cmr.notep.business.config.BusinessConfig.dozerMapperBean;

@Component
@Slf4j
@Transactional
public class MessagesBusiness {
    private final DaoAccessorService daoAccessorService;

    public MessagesBusiness(DaoAccessorService daoAccessorService) {
        this.daoAccessorService = daoAccessorService;
    }

    public Messages avoirMessage(String idMessage) {
        MessagesEntity messageEntity = daoAccessorService.getRepository(MessagesRepository.class)
                .findById(idMessage)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Message introuvable avec l'ID: " + idMessage));

        // Force loading of recipients
        messageEntity.getDestinatairesEntities().size();

        return dozerMapperBean.map(messageEntity, Messages.class);
    }

    public Messages posterMessage(Messages message) {
        if (message.getObjet() == null || message.getObjet().isBlank()) {
            throw new SchoolException(SchoolErrorCode.INVALID_INPUT, "L'objet du message est obligatoire");
        }

        MessagesEntity messageEntity = dozerMapperBean.map(message, MessagesEntity.class);
        MessagesEntity savedEntity = daoAccessorService.getRepository(MessagesRepository.class).save(messageEntity);
        savedEntity.getDestinatairesEntities().size();
        return dozerMapperBean.map(savedEntity, Messages.class);
    }
    public Messages posterMessageGroupe(GroupMessageDto groupMessageDto) {
        if (groupMessageDto.getObjet() == null || groupMessageDto.getObjet().isBlank()) {
            throw new SchoolException(SchoolErrorCode.INVALID_INPUT, "L'objet du message est obligatoire");
        }

        // Get sender
        UtilisateursEntity sender = daoAccessorService.getRepository(UtilisateursRepository.class)
                .findById(groupMessageDto.getSenderId())
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Expéditeur non trouvé"));

        // Create message entity
        MessagesEntity messageEntity = new MessagesEntity();
        messageEntity.setContenu(groupMessageDto.getContent());
        messageEntity.setObjet(groupMessageDto.getObjet());
        messageEntity.setExpediteurEntity(sender);
        messageEntity.setDateCreation(new Date().toString());
        messageEntity.setEtat("envoyé");

        // Get all classes
        List<ClassesEntity> classes = groupMessageDto.getClassIds().stream()
                .map(classId -> daoAccessorService.getRepository(ClassesRepository.class)
                        .findById(classId)
                        .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Classe non trouvée avec l'ID: " + classId)))
                .collect(Collectors.toList());

        // Get all users who have access to the classes (excluding sender)
        List<UtilisateursEntity> recipients = new ArrayList<>();
        for (ClassesEntity classe : classes) {
            List<AccederEntity> accessList = daoAccessorService.getRepository(AccederRepository.class)
                    .findByClasseId(classe.getId().toString());

            // Filter out null users and the sender
            recipients.addAll(accessList.stream()
                    .map(AccederEntity::getUtilisateur)
                    .filter(Objects::nonNull)
                    .filter(user -> !user.getId().equals(sender.getId()))
                    .collect(Collectors.toList()));
        }

        // Remove duplicates (in case a user is in multiple classes)
        recipients = recipients.stream()
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        // Add copie recipients if any
        if (groupMessageDto.getCopieRecipientIds() != null && !groupMessageDto.getCopieRecipientIds().isEmpty()) {
            List<UtilisateursEntity> copieRecipients = groupMessageDto.getCopieRecipientIds().stream()
                    .map(id -> daoAccessorService.getRepository(UtilisateursRepository.class)
                            .findById(id)
                            .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Destinataire copie non trouvé avec l'ID: " + id)))
                    .filter(recipient -> !recipient.getId().equals(sender.getId())) // Exclude sender
                    .collect(Collectors.toList());

            recipients.addAll(copieRecipients);
        }

        messageEntity.setDestinatairesEntities(recipients);
        messageEntity.setClasses(classes);

        // Save the message
        MessagesEntity savedEntity = daoAccessorService.getRepository(MessagesRepository.class).save(messageEntity);

        // Force loading of recipients
        savedEntity.getDestinatairesEntities().size();

        return dozerMapperBean.map(savedEntity, Messages.class);
    }


    public List<Messages> obtenirMessagesParUtilisateur(String utilisateurId) {
        log.info("Obtenir tous les messages pour l'utilisateur {}", utilisateurId);

        // Verify user exists
        if (!daoAccessorService.getRepository(UtilisateursRepository.class).existsById(utilisateurId)) {
            throw new SchoolException(SchoolErrorCode.NOT_FOUND, "Utilisateur introuvable");
        }

        // Get sent messages
        List<MessagesEntity> sentMessages = daoAccessorService.getRepository(MessagesRepository.class)
                .findByExpediteurEntityId(utilisateurId);

        // Get received messages
        List<MessagesEntity> receivedMessages = daoAccessorService.getRepository(MessagesRepository.class)
                .findByDestinatairesEntitiesId(utilisateurId);

        // Combine and map to DTO
        List<MessagesEntity> allMessages = new ArrayList<>();
        allMessages.addAll(sentMessages);
        allMessages.addAll(receivedMessages);

        // Force loading of recipients for each message
        allMessages.forEach(msg -> {
            if (msg.getDestinatairesEntities() != null) {
                msg.getDestinatairesEntities().size();
            }
        });

        return allMessages.stream()
                .map(msg -> dozerMapperBean.map(msg, Messages.class))
                .collect(Collectors.toList());
    }

    public List<Messages> avoirToutMessages() {
        List<MessagesEntity> messageEntities = daoAccessorService.getRepository(MessagesRepository.class).findAll();

        // Force loading of recipients for each message
        messageEntities.forEach(msg -> msg.getDestinatairesEntities().size());

        return messageEntities.stream()
                .map(msg -> dozerMapperBean.map(msg, Messages.class))
                .collect(Collectors.toList());
    }
}