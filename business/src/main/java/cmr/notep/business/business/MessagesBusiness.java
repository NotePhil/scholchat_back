package cmr.notep.business.business;

import cmr.notep.business.exceptions.SchoolException;
import cmr.notep.business.exceptions.enums.SchoolErrorCode;
import cmr.notep.interfaces.modeles.Messages;
import cmr.notep.ressourcesjpa.commun.DaoAccessorService;
import cmr.notep.ressourcesjpa.dao.AccederEntity;
import cmr.notep.ressourcesjpa.dao.ClassesEntity;
import cmr.notep.ressourcesjpa.dao.MessagesEntity;
import cmr.notep.ressourcesjpa.dao.UtilisateursEntity;
import cmr.notep.ressourcesjpa.repository.AccederRepository;
import cmr.notep.ressourcesjpa.repository.ClassesRepository;
import cmr.notep.ressourcesjpa.repository.MessagesRepository;
import cmr.notep.ressourcesjpa.repository.UtilisateursRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

import static cmr.notep.business.config.BusinessConfig.dozerMapperBean;

@Component
@Slf4j
public class MessagesBusiness {
    private final DaoAccessorService daoAccessorService;

    public MessagesBusiness(DaoAccessorService daoAccessorService) {
        this.daoAccessorService = daoAccessorService;
    }

    // Get single message
    public Messages getMessage(String idMessage) {
        MessagesEntity entity = daoAccessorService.getRepository(MessagesRepository.class)
                .findById(idMessage)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND,
                        "Message not found with ID: " + idMessage));

        return dozerMapperBean.map(entity, Messages.class);
    }

    // Send message to individual users
    public Messages sendIndividualMessage(Messages message) throws SchoolException {
        log.info("Sending individual message from user {}", message.getExpediteur());

        // Validate sender exists
        UtilisateursEntity sender = validateUserExists(message.getExpediteur());

        // Validate all recipients exist
        List<UtilisateursEntity> recipients = validateRecipientsExist(message.getDestinataires());

        // Create and save message
        MessagesEntity messageEntity = createMessageEntity(message, sender, recipients);
        MessagesEntity savedMessage = saveMessage(messageEntity);

        // Update recipients
        updateRecipients(recipients, savedMessage);

        log.info("Individual message sent successfully with ID: {}", savedMessage.getId());
        return dozerMapperBean.map(savedMessage, Messages.class);
    }

    // Send message to class groups
    public Messages sendClassGroupMessage(Messages message) throws SchoolException {
        log.info("Sending class group message from user {}", message.getExpediteur());

        // Validate sender exists
        UtilisateursEntity sender = validateUserExists(message.getExpediteur());

        // Verify sender has access to all classes they're trying to message
        verifySenderHasAccessToClasses(message.getExpediteur(), message.getClasseIds());

        // Get all recipients from classes
        List<UtilisateursEntity> recipients = getClassRecipients(message.getExpediteur(), message.getClasseIds());

        // Create and save message
        MessagesEntity messageEntity = createMessageEntity(message, sender, recipients);
        MessagesEntity savedMessage = saveMessage(messageEntity);

        // Update recipients
        updateRecipients(recipients, savedMessage);

        log.info("Class group message sent successfully with ID: {}", savedMessage.getId());
        return dozerMapperBean.map(savedMessage, Messages.class);
    }

    // Get all messages
    public List<Messages> getAllMessages() {
        return daoAccessorService.getRepository(MessagesRepository.class).findAll()
                .stream()
                .map(entity -> dozerMapperBean.map(entity, Messages.class))
                .collect(Collectors.toList());
    }

    // Helper methods
    private UtilisateursEntity validateUserExists(String userId) throws SchoolException {
        return daoAccessorService.getRepository(UtilisateursRepository.class)
                .findById(userId)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND,
                        "User not found with ID: " + userId));
    }

    private List<UtilisateursEntity> validateRecipientsExist(List<String> recipientIds) throws SchoolException {
        if (recipientIds == null || recipientIds.isEmpty()) {
            throw new SchoolException(SchoolErrorCode.NOT_FOUND,
                    "At least one recipient is required for individual messaging");
        }

        List<UtilisateursEntity> recipients = new ArrayList<>();
        for (String recipientId : recipientIds) {
            recipients.add(validateUserExists(recipientId));
        }
        return recipients;
    }

    private void verifySenderHasAccessToClasses(String senderId, List<String> classeIds) throws SchoolException {
        if (classeIds == null || classeIds.isEmpty()) {
            throw new SchoolException(SchoolErrorCode.NOT_FOUND,
                    "At least one class ID is required for group messaging");
        }

        AccederRepository accederRepository = daoAccessorService.getRepository(AccederRepository.class);
        for (String classeId : classeIds) {
            if (!accederRepository.existsByUtilisateurIdAndClasseId(senderId, classeId)) {
                throw new SchoolException(SchoolErrorCode.FORBIDDEN,
                        "Sender doesn't have access to class " + classeId);
            }
        }
    }

    private List<UtilisateursEntity> getClassRecipients(String senderId, List<String> classeIds) {
        Set<UtilisateursEntity> recipients = new HashSet<>();
        AccederRepository accederRepository = daoAccessorService.getRepository(AccederRepository.class);

        for (String classeId : classeIds) {
            List<AccederEntity> accessList = accederRepository.findByClasseId(classeId);
            accessList.forEach(access -> {
                if (!access.getUtilisateurId().equals(senderId)) {
                    recipients.add(access.getUtilisateur());
                }
            });
        }

        return new ArrayList<>(recipients);
    }

    private MessagesEntity createMessageEntity(Messages message, UtilisateursEntity sender, List<UtilisateursEntity> recipients) {
        MessagesEntity entity = new MessagesEntity();
        entity.setId(message.getId());
        entity.setContenu(message.getContenu());
        entity.setDateCreation(message.getDateCreation());
        entity.setDateModification(message.getDateModification());
        entity.setEtat(message.getEtat());
        entity.setExpediteurEntity(sender);
        entity.setDestinatairesEntities(recipients);

        // Map the list of String IDs to a list of ClassesEntity
        List<ClassesEntity> classesEntities = message.getClasseIds().stream()
                .map(classeId -> {
                    ClassesEntity classe = new ClassesEntity();
                    classe.setId(classeId);
                    return classe;
                })
                .collect(Collectors.toList());

        entity.setClasseIds(classesEntities);

        return entity;
    }



    private MessagesEntity saveMessage(MessagesEntity messageEntity) {
        return daoAccessorService.getRepository(MessagesRepository.class).save(messageEntity);
    }

    private void updateRecipients(List<UtilisateursEntity> recipients, MessagesEntity message) {
        UtilisateursRepository utilisateursRepository = daoAccessorService.getRepository(UtilisateursRepository.class);

        for (UtilisateursEntity recipient : recipients) {
            if (recipient.getMessagesRecusEntities() == null) {
                recipient.setMessagesRecusEntities(new ArrayList<>());
            }
            recipient.getMessagesRecusEntities().add(message);
            utilisateursRepository.save(recipient);
        }
    }
}