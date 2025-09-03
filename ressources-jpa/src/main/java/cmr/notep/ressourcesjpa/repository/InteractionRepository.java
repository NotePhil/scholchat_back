package cmr.notep.ressourcesjpa.repository;

import cmr.notep.modele.InteractionType;
import cmr.notep.ressourcesjpa.dao.InteractionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InteractionRepository extends JpaRepository<InteractionEntity, String> {
    List<InteractionEntity> findByEventId(String eventId);
    List<InteractionEntity> findByMessageId(String messageId);
    List<InteractionEntity> findByCreatedById(String userId);

    Optional<InteractionEntity> findByCreatedByIdAndEventIdAndType(String userId, String eventId, InteractionType type);
    Optional<InteractionEntity> findByCreatedByIdAndMessageIdAndType(String userId, String messageId, InteractionType type);

    @Query("SELECT i FROM InteractionEntity i WHERE i.event.id = :eventId AND i.type = 'LIKE'")
    List<InteractionEntity> findLikesByEventId(@Param("eventId") String eventId);

    @Query("SELECT i FROM InteractionEntity i WHERE i.message.id = :messageId AND i.type = 'LIKE'")
    List<InteractionEntity> findLikesByMessageId(@Param("messageId") String messageId);

    @Query("SELECT i FROM InteractionEntity i WHERE i.event.id = :eventId AND i.type = 'COMMENT' ORDER BY i.creationDate DESC")
    List<InteractionEntity> findCommentsByEventId(@Param("eventId") String eventId);

    @Query("SELECT i FROM InteractionEntity i WHERE i.message.id = :messageId AND i.type = 'COMMENT' ORDER BY i.creationDate DESC")
    List<InteractionEntity> findCommentsByMessageId(@Param("messageId") String messageId);
}