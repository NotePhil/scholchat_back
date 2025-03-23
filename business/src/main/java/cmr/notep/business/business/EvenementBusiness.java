package cmr.notep.business.business;

import cmr.notep.business.exceptions.SchoolException;
import cmr.notep.business.exceptions.enums.SchoolErrorCode;
import cmr.notep.interfaces.modeles.Evenement;
import cmr.notep.ressourcesjpa.commun.DaoAccessorService;
import cmr.notep.ressourcesjpa.dao.CanalEntity;
import cmr.notep.ressourcesjpa.dao.EvenementEntity;
import cmr.notep.ressourcesjpa.repository.CanalRepository;
import cmr.notep.ressourcesjpa.repository.EvenementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import static cmr.notep.business.config.BusinessConfig.dozerMapperBean;

@Component
@Slf4j
@RequiredArgsConstructor
public class EvenementBusiness {

    private final DaoAccessorService daoAccessorService;

    // Create a new event
    public Evenement creerEvenement(Evenement evenement) throws SchoolException {
        // Validate the associated canal exists
        CanalEntity canal = daoAccessorService.getRepository(CanalRepository.class)
                .findById(evenement.getCanalId())
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Canal non trouvé avec l'ID: " + evenement.getCanalId()));

        // Map the DTO to an entity
        EvenementEntity evenementEntity = dozerMapperBean.map(evenement, EvenementEntity.class);
        evenementEntity.setCanal(canal);

        // Save the entity
        EvenementEntity savedEntity = daoAccessorService.getRepository(EvenementRepository.class).save(evenementEntity);
        log.info("Evenement créé avec succès: {}", savedEntity.getId());

        // Map the saved entity back to a DTO
        return dozerMapperBean.map(savedEntity, Evenement.class);
    }

    // Update an existing event
    public Evenement modifierEvenement(String id, Evenement evenement) throws SchoolException {
        EvenementRepository evenementRepository = daoAccessorService.getRepository(EvenementRepository.class);

        // Find the existing event
        EvenementEntity existingEntity = evenementRepository.findById(id)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Evenement non trouvé avec l'ID: " + id));

        // Update the fields
        existingEntity.setTitre(evenement.getTitre());
        existingEntity.setDescription(evenement.getDescription());
        existingEntity.setLieu(evenement.getLieu());
        existingEntity.setHeureDebut(evenement.getHeureDebut());
        existingEntity.setHeureFin(evenement.getHeureFin());
        existingEntity.setEtat(evenement.getEtat());

        // Save the updated entity
        EvenementEntity updatedEntity = evenementRepository.save(existingEntity);
        log.info("Evenement modifié avec succès: {}", updatedEntity.getId());

        // Map the updated entity back to a DTO
        return dozerMapperBean.map(updatedEntity, Evenement.class);
    }

    // Delete an event
    public void supprimerEvenement(String id) throws SchoolException {
        EvenementRepository evenementRepository = daoAccessorService.getRepository(EvenementRepository.class);

        // Check if the event exists
        if (!evenementRepository.existsById(id)) {
            throw new SchoolException(SchoolErrorCode.NOT_FOUND, "Evenement non trouvé avec l'ID: " + id);
        }

        // Delete the event
        evenementRepository.deleteById(id);
        log.info("Evenement supprimé avec succès: {}", id);
    }

    // Get an event by ID
    public Evenement obtenirEvenementParId(String id) throws SchoolException {
        EvenementRepository evenementRepository = daoAccessorService.getRepository(EvenementRepository.class);

        // Find the event
        EvenementEntity evenementEntity = evenementRepository.findById(id)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Evenement non trouvé avec l'ID: " + id));

        // Map the entity to a DTO
        return dozerMapperBean.map(evenementEntity, Evenement.class);
    }

    // Get all events for a specific canal
    public List<Evenement> obtenirEvenementsParCanal(String canalId) throws SchoolException {
        EvenementRepository evenementRepository = daoAccessorService.getRepository(EvenementRepository.class);

        // Find all events for the canal
        List<EvenementEntity> evenementEntities = evenementRepository.findByCanalId(canalId);

        // Map the entities to DTOs
        return evenementEntities.stream()
                .map(entity -> dozerMapperBean.map(entity, Evenement.class))
                .collect(Collectors.toList());
    }
}