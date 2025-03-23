package cmr.notep.business.business;

import cmr.notep.business.exceptions.SchoolException;
import cmr.notep.business.exceptions.enums.SchoolErrorCode;
import cmr.notep.interfaces.modeles.Interaction;
import cmr.notep.ressourcesjpa.commun.DaoAccessorService;
import cmr.notep.ressourcesjpa.dao.EvenementEntity;
import cmr.notep.ressourcesjpa.dao.InteractionEntity;
import cmr.notep.ressourcesjpa.repository.EvenementRepository;
import cmr.notep.ressourcesjpa.repository.InteractionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import static cmr.notep.business.config.BusinessConfig.dozerMapperBean;

@Component
@Slf4j
@RequiredArgsConstructor
public class InteractionBusiness {

    private final DaoAccessorService daoAccessorService;

    // Create a new interaction
    public Interaction creerInteraction(Interaction interaction) throws SchoolException {
        // Validate the associated event exists
        EvenementEntity evenement = daoAccessorService.getRepository(EvenementRepository.class)
                .findById(interaction.getEvenementId())
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Evenement non trouvé avec l'ID: " + interaction.getEvenementId()));

        // Map the DTO to an entity
        InteractionEntity interactionEntity = dozerMapperBean.map(interaction, InteractionEntity.class);
        interactionEntity.setEvenement(evenement);

        // Save the entity
        InteractionEntity savedEntity = daoAccessorService.getRepository(InteractionRepository.class).save(interactionEntity);
        log.info("Interaction créée avec succès: {}", savedEntity.getId());

        // Map the saved entity back to a DTO
        return dozerMapperBean.map(savedEntity, Interaction.class);
    }

    // Update an existing interaction
    public Interaction modifierInteraction(String id, Interaction interaction) throws SchoolException {
        InteractionRepository interactionRepository = daoAccessorService.getRepository(InteractionRepository.class);

        // Find the existing interaction
        InteractionEntity existingEntity = interactionRepository.findById(id)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Interaction non trouvée avec l'ID: " + id));

        // Update the fields
        existingEntity.setType(interaction.getType());
        existingEntity.setContenu(interaction.getContenu());
        existingEntity.setNiveau(interaction.getNiveau());

        // Save the updated entity
        InteractionEntity updatedEntity = interactionRepository.save(existingEntity);
        log.info("Interaction modifiée avec succès: {}", updatedEntity.getId());

        // Map the updated entity back to a DTO
        return dozerMapperBean.map(updatedEntity, Interaction.class);
    }

    // Delete an interaction
    public void supprimerInteraction(String id) throws SchoolException {
        InteractionRepository interactionRepository = daoAccessorService.getRepository(InteractionRepository.class);

        // Check if the interaction exists
        if (!interactionRepository.existsById(id)) {
            throw new SchoolException(SchoolErrorCode.NOT_FOUND, "Interaction non trouvée avec l'ID: " + id);
        }

        // Delete the interaction
        interactionRepository.deleteById(id);
        log.info("Interaction supprimée avec succès: {}", id);
    }

    // Get an interaction by ID
    public Interaction obtenirInteractionParId(String id) throws SchoolException {
        InteractionRepository interactionRepository = daoAccessorService.getRepository(InteractionRepository.class);

        // Find the interaction
        InteractionEntity interactionEntity = interactionRepository.findById(id)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Interaction non trouvée avec l'ID: " + id));

        // Map the entity to a DTO
        return dozerMapperBean.map(interactionEntity, Interaction.class);
    }

    // Get all interactions for a specific event
    public List<Interaction> obtenirInteractionsParEvenement(String evenementId) throws SchoolException {
        InteractionRepository interactionRepository = daoAccessorService.getRepository(InteractionRepository.class);

        // Find all interactions for the event
        List<InteractionEntity> interactionEntities = interactionRepository.findByEvenementId(evenementId);

        // Map the entities to DTOs
        return interactionEntities.stream()
                .map(entity -> dozerMapperBean.map(entity, Interaction.class))
                .collect(Collectors.toList());
    }
}