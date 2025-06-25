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

import java.util.Date;
import java.util.List;
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
        MessagesEntity messageEntity = dozerMapperBean.map(message, MessagesEntity.class);
        MessagesEntity savedEntity = daoAccessorService.getRepository(MessagesRepository.class).save(messageEntity);

        // Force loading of recipients
        savedEntity.getDestinatairesEntities().size();

        return dozerMapperBean.map(savedEntity, Messages.class);
    }

    public Messages posterMessageGroupe(GroupMessageDto groupMessageDto) {
        // Get the class
        ClassesEntity classe = daoAccessorService.getRepository(ClassesRepository.class)
                .findById(groupMessageDto.getClassId())
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Classe non trouvée"));

        // Get all users who have access to the class
        List<AccederEntity> accessList = daoAccessorService.getRepository(AccederRepository.class)
                .findByClasseId(groupMessageDto.getClassId());

        // Get sender
        UtilisateursEntity sender = daoAccessorService.getRepository(UtilisateursRepository.class)
                .findById(groupMessageDto.getSenderId())
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Expéditeur non trouvé"));

        // Create message entity
        MessagesEntity messageEntity = new MessagesEntity();
        messageEntity.setContenu(groupMessageDto.getContent());
        messageEntity.setExpediteurEntity(sender);
        messageEntity.setDateCreation(new Date().toString());
        messageEntity.setEtat("envoyé");

        // Set recipients (all users who have access to the class)
        List<UtilisateursEntity> recipients = accessList.stream()
                .map(AccederEntity::getUtilisateur)
                .collect(Collectors.toList());
        messageEntity.setDestinatairesEntities(recipients);

        // Associate with the class
        messageEntity.setClasses(List.of(classe));

        // Save the message
        MessagesEntity savedEntity = daoAccessorService.getRepository(MessagesRepository.class).save(messageEntity);

        // Force loading of recipients
        savedEntity.getDestinatairesEntities().size();

        return dozerMapperBean.map(savedEntity, Messages.class);
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