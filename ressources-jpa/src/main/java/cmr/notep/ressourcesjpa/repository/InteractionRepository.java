package cmr.notep.ressourcesjpa.repository;

import cmr.notep.ressourcesjpa.dao.InteractionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface InteractionRepository extends JpaRepository<InteractionEntity, String> {
    List<InteractionEntity> findByEventId(String eventId);
    List<InteractionEntity> findByMessageId(String messageId);
    List<InteractionEntity> findByCreatedById(String userId);
}