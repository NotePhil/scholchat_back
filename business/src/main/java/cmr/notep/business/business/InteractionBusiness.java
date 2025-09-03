package cmr.notep.business.business;

import cmr.notep.business.exceptions.SchoolException;
import cmr.notep.business.exceptions.enums.SchoolErrorCode;
import cmr.notep.interfaces.modeles.Interaction;
import cmr.notep.ressourcesjpa.commun.DaoAccessorService;
import cmr.notep.ressourcesjpa.dao.EvenementEntity;
import cmr.notep.ressourcesjpa.dao.InteractionEntity;
import cmr.notep.ressourcesjpa.dao.MessagesEntity;
import cmr.notep.ressourcesjpa.dao.UtilisateursEntity;
import cmr.notep.ressourcesjpa.repository.EvenementRepository;
import cmr.notep.ressourcesjpa.repository.InteractionRepository;
import cmr.notep.ressourcesjpa.repository.MessagesRepository;
import cmr.notep.ressourcesjpa.repository.UtilisateursRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static cmr.notep.business.config.BusinessConfig.dozerMapperBean;

@Component
@Slf4j
public class InteractionBusiness {
    private final DaoAccessorService daoAccessorService;

    public InteractionBusiness(DaoAccessorService daoAccessorService) {
        this.daoAccessorService = daoAccessorService;
    }

    public Interaction createInteraction(Interaction interaction) {
        // Check if user already has an interaction of this type on the same target
        if (interaction.getType().name().equals("LIKE")) {
            Optional<InteractionEntity> existingLike = findExistingLike(
                    interaction.getCreatedById(),
                    interaction.getEventId(),
                    interaction.getMessageId()
            );

            if (existingLike.isPresent()) {
                // Remove the existing like (dislike)
                daoAccessorService.getRepository(InteractionRepository.class).delete(existingLike.get());
                return null; // Return null to indicate removal
            }
        }

        InteractionEntity entity = dozerMapperBean.map(interaction, InteractionEntity.class);

        // Ensure creation date is set
        if (entity.getCreationDate() == null) {
            entity.setCreationDate(LocalDateTime.now());
        }

        // Fetch and set the createdBy user
        UtilisateursEntity user = daoAccessorService.getRepository(UtilisateursRepository.class)
                .findById(interaction.getCreatedById())
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND,
                        "User not found with id: " + interaction.getCreatedById()));
        entity.setCreatedBy(user);

        // If eventId is provided, fetch and set the event
        if (interaction.getEventId() != null) {
            EvenementEntity event = daoAccessorService.getRepository(EvenementRepository.class)
                    .findById(interaction.getEventId())
                    .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND,
                            "Event not found with id: " + interaction.getEventId()));
            entity.setEvent(event);
        }

        // If messageId is provided, fetch and set the message
        if (interaction.getMessageId() != null) {
            MessagesEntity message = daoAccessorService.getRepository(MessagesRepository.class)
                    .findById(interaction.getMessageId())
                    .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND,
                            "Message not found with id: " + interaction.getMessageId()));
            entity.setMessage(message);
        }

        InteractionEntity savedEntity = daoAccessorService.getRepository(InteractionRepository.class).save(entity);

        // Map back to Interaction with just IDs
        return mapEntityToInteraction(savedEntity);
    }

    private Optional<InteractionEntity> findExistingLike(String userId, String eventId, String messageId) {
        InteractionRepository repository = daoAccessorService.getRepository(InteractionRepository.class);

        if (eventId != null) {
            return repository.findByCreatedByIdAndEventIdAndType(userId, eventId, cmr.notep.modele.InteractionType.LIKE);
        } else if (messageId != null) {
            return repository.findByCreatedByIdAndMessageIdAndType(userId, messageId, cmr.notep.modele.InteractionType.LIKE);
        }

        return Optional.empty();
    }

    private Interaction mapEntityToInteraction(InteractionEntity entity) {
        Interaction interaction = dozerMapperBean.map(entity, Interaction.class);

        // Explicitly set the IDs from the relationships
        interaction.setCreatedById(entity.getCreatedBy().getId());
        interaction.setEventId(entity.getEvent() != null ? entity.getEvent().getId() : null);
        interaction.setMessageId(entity.getMessage() != null ? entity.getMessage().getId() : null);

        return interaction;
    }

    public List<Interaction> getInteractionsByEvent(String eventId) {
        List<InteractionEntity> interactions = daoAccessorService.getRepository(InteractionRepository.class)
                .findByEventId(eventId);

        return interactions.stream()
                .map(this::mapEntityToInteraction)
                .collect(Collectors.toList());
    }

    public List<Interaction> getInteractionsByMessage(String messageId) {
        List<InteractionEntity> interactions = daoAccessorService.getRepository(InteractionRepository.class)
                .findByMessageId(messageId);

        return interactions.stream()
                .map(this::mapEntityToInteraction)
                .collect(Collectors.toList());
    }

    public List<Interaction> getInteractionsByUser(String userId) {
        List<InteractionEntity> interactions = daoAccessorService.getRepository(InteractionRepository.class)
                .findByCreatedById(userId);

        return interactions.stream()
                .map(this::mapEntityToInteraction)
                .collect(Collectors.toList());
    }

    public boolean hasUserLikedEvent(String userId, String eventId) {
        return daoAccessorService.getRepository(InteractionRepository.class)
                .findByCreatedByIdAndEventIdAndType(userId, eventId, cmr.notep.modele.InteractionType.LIKE)
                .isPresent();
    }

    public boolean hasUserLikedMessage(String userId, String messageId) {
        return daoAccessorService.getRepository(InteractionRepository.class)
                .findByCreatedByIdAndMessageIdAndType(userId, messageId, cmr.notep.modele.InteractionType.LIKE)
                .isPresent();
    }
}