package cmr.notep.ressourcesjpa.repository;

import cmr.notep.ressourcesjpa.dao.ConversationParticipantEntity;
import cmr.notep.ressourcesjpa.dao.ConversationParticipantId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface ConversationParticipantRepository extends JpaRepository<ConversationParticipantEntity, ConversationParticipantId> {

    List<ConversationParticipantEntity> findByIdConversationId(String conversationId);

    @Query("SELECT p FROM ConversationParticipantEntity p LEFT JOIN FETCH p.utilisateur WHERE p.id.conversationId IN :cids")
    List<ConversationParticipantEntity> findByConversationIds(@Param("cids") Collection<String> conversationIds);

    /** Conversations in the user's trash (deleted for them, not permanently deleted). */
    @Query("SELECT p FROM ConversationParticipantEntity p WHERE p.id.utilisateurId = :uid AND p.deletedAt IS NOT NULL")
    List<ConversationParticipantEntity> findCorbeille(@Param("uid") String utilisateurId);

    /** Conversations the user marked as unread. */
    @Query("SELECT p FROM ConversationParticipantEntity p WHERE p.id.utilisateurId = :uid AND p.marqueNonLu = true")
    List<ConversationParticipantEntity> findMarquesNonLus(@Param("uid") String utilisateurId);

    /** Race-safe membership insert. */
    @Modifying
    @Query(value = "INSERT INTO ressources.conversation_participants (conversation_id, utilisateur_id, joined_at, muted) " +
            "VALUES (:cid, :uid, :joinedAt, FALSE) ON CONFLICT DO NOTHING", nativeQuery = true)
    int ajouterSiAbsent(@Param("cid") String conversationId, @Param("uid") String utilisateurId,
                        @Param("joinedAt") LocalDateTime joinedAt);
}
