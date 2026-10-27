package cmr.notep.ressourcesjpa.repository;

import cmr.notep.ressourcesjpa.dao.ConversationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<ConversationEntity, String> {

    Optional<ConversationEntity> findByDirectKey(String directKey);

    List<ConversationEntity> findByTypeAndParticipantsKeyOrderByCreatedAtDesc(String type, String participantsKey);

    /** Groups of a single-class message (same class, same participants). */
    List<ConversationEntity> findByTypeAndParticipantsKeyAndClasseIdOrderByCreatedAtDesc(String type, String participantsKey, String classeId);

    /** Groups not tied to a class (individual messages to several people). */
    List<ConversationEntity> findByTypeAndParticipantsKeyAndClasseIdIsNullOrderByCreatedAtDesc(String type, String participantsKey);

    /** Conversations of the user that are not hidden (or got a newer message since being hidden). */
    @Query("SELECT c FROM ConversationEntity c, ConversationParticipantEntity p " +
            "WHERE p.id.conversationId = c.id AND p.id.utilisateurId = :uid " +
            "AND (p.deletedAt IS NULL OR (c.lastMessageAt IS NOT NULL AND c.lastMessageAt > p.deletedAt)) " +
            "AND (p.historiqueDepuis IS NULL OR (c.lastMessageAt IS NOT NULL AND c.lastMessageAt > p.historiqueDepuis)) " +
            "AND (c.lastMessageAt IS NOT NULL OR c.createdBy = :uid)")
    List<ConversationEntity> findVisibleFor(@Param("uid") String utilisateurId);

    /**
     * Number of classes the FIRST message of each conversation was sent to ("message à une classe" = 1,
     * general message to several classes > 1): rows [conversation_id, count], only conversations started
     * by a class message.
     */
    @Query(value = "SELECT f.conversation_id, COUNT(mc.classe_id) FROM (" +
            "SELECT x.conversation_id, x.id FROM (" +
            "SELECT m.conversation_id, m.id, ROW_NUMBER() OVER (PARTITION BY m.conversation_id ORDER BY m.date_envoi ASC, m.id ASC) AS rn " +
            "FROM ressources.messages m WHERE m.conversation_id IN (:cids)) x WHERE x.rn = 1) f " +
            "JOIN ressources.message_classes mc ON mc.message_id = f.id GROUP BY f.conversation_id", nativeQuery = true)
    List<Object[]> countClassesParConversation(@Param("cids") java.util.Collection<String> conversationIds);

    /** Race-safe creation of a DIRECT conversation (unique direct_key). */
    @Modifying
    @Query(value = "INSERT INTO ressources.conversations (id, type, created_by, created_at, direct_key, participants_key) " +
            "VALUES (:id, 'DIRECT', :createdBy, :createdAt, :directKey, :participantsKey) ON CONFLICT DO NOTHING",
            nativeQuery = true)
    int insererDirectSiAbsent(@Param("id") String id, @Param("createdBy") String createdBy,
                              @Param("createdAt") LocalDateTime createdAt, @Param("directKey") String directKey,
                              @Param("participantsKey") String participantsKey);
}
