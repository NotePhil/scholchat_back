package cmr.notep.business.business;

import cmr.notep.business.exceptions.SchoolException;
import cmr.notep.business.exceptions.enums.SchoolErrorCode;
import cmr.notep.interfaces.dto.GroupMessageDto;
import cmr.notep.interfaces.modeles.*;
import cmr.notep.ressourcesjpa.commun.DaoAccessorService;
import cmr.notep.ressourcesjpa.dao.*;
import cmr.notep.ressourcesjpa.repository.*;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
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

        return mapMessageEntityToDto(messageEntity);
    }

    public Messages posterMessage(Messages message) {
        if (message.getObjet() == null || message.getObjet().isBlank()) {
            throw new SchoolException(SchoolErrorCode.INVALID_INPUT, "L'objet du message est obligatoire");
        }

        MessagesEntity messageEntity = dozerMapperBean.map(message, MessagesEntity.class);
        messageEntity.setId(UUID.randomUUID().toString());
        
        // Set required fields if not already set
        if (messageEntity.getDateCreation() == null) {
            messageEntity.setDateCreation(new Date().toString());
        }
        if (messageEntity.getEtat() == null) {
            messageEntity.setEtat("envoyé");
        }
        
        MessagesEntity savedEntity = daoAccessorService.getRepository(MessagesRepository.class).save(messageEntity);
        savedEntity.getDestinatairesEntities().size();
        return mapMessageEntityToDto(savedEntity);
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
        messageEntity.setId(UUID.randomUUID().toString());
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

        return mapMessageEntityToDto(savedEntity);
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
                .map(this::mapMessageEntityToDto)
                .collect(Collectors.toList());
    }

    public List<Messages> avoirToutMessages() {
        List<MessagesEntity> messageEntities = daoAccessorService.getRepository(MessagesRepository.class).findAll();

        // Force loading of recipients for each message
        messageEntities.forEach(msg -> msg.getDestinatairesEntities().size());

        return messageEntities.stream()
                .map(this::mapMessageEntityToDto)
                .collect(Collectors.toList());
    }

    public List<MessageDto> obtenirMessagesEnvoyes(String utilisateurId) {
        log.info("Obtenir les messages envoyés par l'utilisateur {}", utilisateurId);

        if (!daoAccessorService.getRepository(UtilisateursRepository.class).existsById(utilisateurId)) {
            throw new SchoolException(SchoolErrorCode.NOT_FOUND, "Utilisateur introuvable");
        }

        List<MessagesEntity> sentMessages = daoAccessorService.getRepository(MessagesRepository.class)
                .findByExpediteurEntityId(utilisateurId);

        return sentMessages.stream()
                .map(this::mapToMessageDto)
                .collect(Collectors.toList());
    }

    public List<MessageDto> obtenirMessagesRecus(String utilisateurId) {
        log.info("Obtenir les messages reçus par l'utilisateur {}", utilisateurId);

        if (!daoAccessorService.getRepository(UtilisateursRepository.class).existsById(utilisateurId)) {
            throw new SchoolException(SchoolErrorCode.NOT_FOUND, "Utilisateur introuvable");
        }

        List<MessagesEntity> receivedMessages = daoAccessorService.getRepository(MessagesRepository.class)
                .findByDestinatairesEntitiesId(utilisateurId);

        return receivedMessages.stream()
                .map(this::mapToMessageDto)
                .collect(Collectors.toList());
    }

    private Messages mapMessageEntityToDto(MessagesEntity entity) {
        Messages message = dozerMapperBean.map(entity, Messages.class);
        
        // Manually map destinataires to ensure proper serialization
        if (entity.getDestinatairesEntities() != null) {
            List<Utilisateurs> destinataires = entity.getDestinatairesEntities().stream()
                    .map(this::mapUtilisateursEntityToModele)
                    .collect(Collectors.toList());
            message.setDestinataires(destinataires);
        }
        
        // Manually map expediteur to ensure proper type
        if (entity.getExpediteurEntity() != null) {
            message.setExpediteur(mapUtilisateursEntityToModele(entity.getExpediteurEntity()));
        }
        
        return message;
    }
    
    private Messages mapMessageEntityForSent(MessagesEntity entity) {
        Messages message = dozerMapperBean.map(entity, Messages.class);
        message.setEtat("envoyé");
        
        // For sent messages, include destinataires but exclude expediteur details
        if (entity.getDestinatairesEntities() != null) {
            List<Utilisateurs> destinataires = entity.getDestinatairesEntities().stream()
                    .map(this::mapUtilisateursEntityToModele)
                    .collect(Collectors.toList());
            message.setDestinataires(destinataires);
        }
        
        // Set expediteur but don't include full details to avoid circular references
        if (entity.getExpediteurEntity() != null) {
            message.setExpediteur(mapUtilisateursEntityToModele(entity.getExpediteurEntity()));
        }
        
        return message;
    }
    
    private Messages mapMessageEntityForReceived(MessagesEntity entity) {
        Messages message = dozerMapperBean.map(entity, Messages.class);
        message.setEtat("reçu");
        
        // For received messages, include expediteur but exclude destinataires
        message.setDestinataires(null);
        
        if (entity.getExpediteurEntity() != null) {
            message.setExpediteur(mapUtilisateursEntityToModele(entity.getExpediteurEntity()));
        }
        
        return message;
    }
    
    private MessageDto mapToMessageDto(MessagesEntity entity) {
        MessageDto dto = new MessageDto();
        dto.setId(entity.getId());
        dto.setObjet(entity.getObjet());
        dto.setContenu(entity.getContenu());
        dto.setDateCreation(entity.getDateCreation());
        dto.setDateModification(entity.getDateModification());
        dto.setEtat(entity.getEtat());

        if (entity.getExpediteurEntity() != null) {
            dto.setExpediteur(mapToUtilisateurSimpleDto(entity.getExpediteurEntity()));
        }

        if (entity.getDestinatairesEntities() != null) {
            dto.setDestinataires(entity.getDestinatairesEntities().stream()
                    .map(this::mapToUtilisateurSimpleDto)
                    .collect(Collectors.toList()));
        }

        // Safely access lazy-loaded classes collection
        try {
            if (entity.getClasses() != null) {
                dto.setClasseIds(entity.getClasses().stream()
                        .map(ClassesEntity::getId)
                        .collect(Collectors.toList()));
            }
        } catch (Exception e) {
            log.debug("Could not load classes for message {}: {}", entity.getId(), e.getMessage());
            dto.setClasseIds(new ArrayList<>());
        }

        return dto;
    }
    
    private UtilisateurSimpleDto mapToUtilisateurSimpleDto(UtilisateursEntity entity) {
        UtilisateurSimpleDto dto = new UtilisateurSimpleDto();
        dto.setId(entity.getId());
        dto.setNom(entity.getNom());
        dto.setPrenom(entity.getPrenom());
        dto.setEmail(entity.getEmail());
        return dto;
    }
    
    public void supprimerMessage(String messageId) {
        log.info("Suppression (soft delete) du message avec ID: {}", messageId);
        
        MessagesEntity messageEntity = daoAccessorService.getRepository(MessagesRepository.class)
                .findById(messageId)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Message introuvable avec l'ID: " + messageId));
        
        messageEntity.setEtatOriginal(messageEntity.getEtat());
        messageEntity.setDeleted(true);
        messageEntity.setDateSuppression(new Date().toString());
        messageEntity.setEtat("supprimé");
        
        daoAccessorService.getRepository(MessagesRepository.class).save(messageEntity);
        log.info("Message déplacé vers la corbeille avec succès");
    }
    
    public List<MessageDto> obtenirMessagesCorbeille(String utilisateurId) {
        log.info("Obtenir les messages dans la corbeille pour l'utilisateur {}", utilisateurId);
        
        List<MessagesEntity> deletedMessages = daoAccessorService.getRepository(MessagesRepository.class)
                .findByExpediteurEntityIdAndDeleted(utilisateurId, true);
        
        return deletedMessages.stream()
                .map(this::mapToMessageDto)
                .collect(Collectors.toList());
    }
    
    public void viderCorbeille() {
        log.info("Suppression définitive des messages de plus de 24h dans la corbeille");

        // Calculate cutoff date (24 hours ago)
        String cutoffDate = new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000).toString();

        List<MessagesEntity> oldDeletedMessages = daoAccessorService.getRepository(MessagesRepository.class)
                .findAllDeletedMessages();

        daoAccessorService.getRepository(MessagesRepository.class).deleteAll(oldDeletedMessages);
        log.info("{} messages supprimés définitivement", oldDeletedMessages.size());
    }
    
    public void restaurerMessage(String messageId) {
        log.info("Restauration du message avec ID: {}", messageId);
        
        MessagesEntity messageEntity = daoAccessorService.getRepository(MessagesRepository.class)
                .findById(messageId)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Message introuvable avec l'ID: " + messageId));
        
        if (!messageEntity.isDeleted()) {
            throw new SchoolException(SchoolErrorCode.INVALID_STATE, "Le message n'est pas dans la corbeille");
        }
        
        messageEntity.setDeleted(false);
        messageEntity.setDateSuppression(null);
        messageEntity.setEtat(messageEntity.getEtatOriginal() != null ? messageEntity.getEtatOriginal() : "envoyé");
        messageEntity.setEtatOriginal(null);
        
        daoAccessorService.getRepository(MessagesRepository.class).save(messageEntity);
        log.info("Message restauré avec succès");
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
}