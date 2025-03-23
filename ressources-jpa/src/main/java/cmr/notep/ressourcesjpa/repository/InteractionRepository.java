package cmr.notep.ressourcesjpa.repository;

import cmr.notep.modele.TypeInteraction;
import cmr.notep.ressourcesjpa.dao.InteractionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InteractionRepository extends JpaRepository<InteractionEntity, String> {

    // Find all interactions for a specific event
    List<InteractionEntity> findByEvenementId(String evenementId);

    // Find all interactions of a specific type (optional, if needed)
    List<InteractionEntity> findByType(TypeInteraction type);

    // Find all interactions within a specific time range (optional, if needed)
    List<InteractionEntity> findByDateCreationBetween(LocalDateTime start, LocalDateTime end);
}