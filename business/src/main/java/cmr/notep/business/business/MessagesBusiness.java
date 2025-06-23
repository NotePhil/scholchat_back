package cmr.notep.business.business;

import cmr.notep.business.exceptions.SchoolException;
import cmr.notep.business.exceptions.enums.SchoolErrorCode;
import cmr.notep.interfaces.dto.ClassMessageDto;
import cmr.notep.interfaces.modeles.Messages;
import cmr.notep.ressourcesjpa.commun.DaoAccessorService;
import cmr.notep.ressourcesjpa.dao.*;
import cmr.notep.ressourcesjpa.repository.*;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
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
        return dozerMapperBean.map(daoAccessorService.getRepository(MessagesRepository.class)
                .findById(idMessage)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Message introuvable avec l'ID: " + idMessage)), Messages.class);
    }

    public Messages posterMessage(Messages message) {
        MessagesEntity messageEntity = dozerMapperBean.map(message, MessagesEntity.class);
        messageEntity.setDateCreation(new Date().toString());
        messageEntity.setDateModification(new Date().toString());
        messageEntity.setEtat("ENVOYE");

        MessagesEntity savedEntity = daoAccessorService.getRepository(MessagesRepository.class)
                .save(messageEntity);

        return dozerMapperBean.map(savedEntity, Messages.class);
    }

    public Messages posterMessageClasse(ClassMessageDto classMessageDto, String senderId) throws SchoolException {
        // Verify sender has access to the class
        if (classMessageDto.getClassIds() == null || classMessageDto.getClassIds().isEmpty()) {
            throw new SchoolException(SchoolErrorCode.INVALID_INPUT, "No class IDs provided");
        }

        String classId = classMessageDto.getClassIds().get(0); // Get the first class ID

        if (!daoAccessorService.getRepository(AccederRepository.class)
                .existsByUtilisateurIdAndClasseId(senderId, classId)) {
            throw new SchoolException(SchoolErrorCode.FORBIDDEN,
                    "L'utilisateur n'a pas accès à cette classe");
        }

        // Get all users with access to the class
        List<UtilisateursEntity> recipients = daoAccessorService.getRepository(AccederRepository.class)
                .findByClasseId(classId)
                .stream()
                .map(AccederEntity::getUtilisateur)
                .collect(Collectors.toList());

        // Create message
        MessagesEntity message = new MessagesEntity();
        message.setContenu(classMessageDto.getContent());
        message.setDateCreation(new Date().toString());
        message.setDateModification(new Date().toString());
        message.setEtat("ENVOYE");

        // Set sender
        message.setExpediteurEntity(daoAccessorService.getRepository(UtilisateursRepository.class)
                .findById(senderId)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Utilisateur introuvable")));

        // Set class
        message.setClasse(daoAccessorService.getRepository(ClassesRepository.class)
                .findById(classId)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Classe introuvable")));

        // Set recipients (excluding sender)
        message.setDestinatairesEntities(recipients.stream()
                .filter(r -> !r.getId().equals(senderId))
                .collect(Collectors.toList()));

        MessagesEntity savedMessage = daoAccessorService.getRepository(MessagesRepository.class)
                .save(message);

        log.info("Message de classe envoyé avec succès à {} destinataires", message.getDestinatairesEntities().size());
        return dozerMapperBean.map(savedMessage, Messages.class);
    }



    public Messages posterMessageMultiClasses(ClassMessageDto classMessageDto, String senderId) throws SchoolException {
        // Verify sender has access to all classes
        for (String classId : classMessageDto.getClassIds()) {
            if (!daoAccessorService.getRepository(AccederRepository.class)
                    .existsByUtilisateurIdAndClasseId(senderId, classId)) {
                throw new SchoolException(SchoolErrorCode.FORBIDDEN,
                        "L'utilisateur n'a pas accès à la classe " + classId);
            }
        }

        // Get all unique recipients from all classes (excluding sender)
        Set<UtilisateursEntity> allRecipients = new HashSet<>();
        List<ClassesEntity> targetClasses = new ArrayList<>();

        for (String classId : classMessageDto.getClassIds()) {
            // Get class entity
            ClassesEntity classe = daoAccessorService.getRepository(ClassesRepository.class)
                    .findById(classId)
                    .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Classe introuvable: " + classId));
            targetClasses.add(classe);

            // Get recipients for this class
            List<UtilisateursEntity> classRecipients = daoAccessorService.getRepository(AccederRepository.class)
                    .findByClasseId(classId)
                    .stream()
                    .map(AccederEntity::getUtilisateur)
                    .filter(user -> !user.getId().equals(senderId))
                    .collect(Collectors.toList());

            allRecipients.addAll(classRecipients);
        }

        // Create message
        MessagesEntity message = new MessagesEntity();
        message.setContenu(classMessageDto.getContent());
        message.setDateCreation(new Date().toString());
        message.setDateModification(new Date().toString());
        message.setEtat("ENVOYE");

        // Set sender
        message.setExpediteurEntity(daoAccessorService.getRepository(UtilisateursRepository.class)
                .findById(senderId)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Utilisateur introuvable")));

        // Set recipients
        message.setDestinatairesEntities(new ArrayList<>(allRecipients));

        // Save the message
        MessagesEntity savedMessage = daoAccessorService.getRepository(MessagesRepository.class)
                .save(message);

        // Create message-class associations (in a separate table)
        for (ClassesEntity classe : targetClasses) {
            MessageClasseIdsEntity association = new MessageClasseIdsEntity();
            association.setMessageId(savedMessage.getId());
            association.setClasseId(classe.getId());
            daoAccessorService.getRepository(MessageClasseIdsRepository.class).save(association);
        }

        log.info("Message envoyé à {} classes et {} destinataires",
                targetClasses.size(), allRecipients.size());
        return dozerMapperBean.map(savedMessage, Messages.class);
    }
    public List<Messages> avoirToutMessages() {
        return daoAccessorService.getRepository(MessagesRepository.class).findAll()
                .stream().map(msg -> dozerMapperBean.map(msg, Messages.class))
                .collect(Collectors.toList());
    }
}