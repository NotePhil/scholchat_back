package cmr.notep.ressourcesjpa.repository;

import cmr.notep.ressourcesjpa.dao.MessagesEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface MessagesRepository extends JpaRepository<MessagesEntity, String> {
    @Query("SELECT m FROM MessagesEntity m LEFT JOIN FETCH m.expediteurEntity WHERE m.expediteurEntity.id = :utilisateurId AND m.deleted = false")
    List<MessagesEntity> findByExpediteurEntityId(@Param("utilisateurId") String utilisateurId);

    @Query("SELECT DISTINCT m FROM MessagesEntity m JOIN m.destinatairesEntities d LEFT JOIN FETCH m.expediteurEntity WHERE d.id = :utilisateurId AND m.deleted = false")
    List<MessagesEntity> findByDestinatairesEntitiesId(@Param("utilisateurId") String utilisateurId);
    
    List<MessagesEntity> findByExpediteurEntityIdAndDeleted(String utilisateurId, boolean deleted);
    
    @Query("SELECT m FROM MessagesEntity m WHERE m.deleted = true AND m.dateSuppression < :cutoffDate")
    List<MessagesEntity> findDeletedMessagesOlderThan24Hours(@Param("cutoffDate") String cutoffDate);

    @Query("SELECT m FROM MessagesEntity m WHERE m.deleted = true")
    List<MessagesEntity> findAllDeletedMessages();


    // ── Messaging permission helpers (who may message whom) ─────────────────

    /** Classes the user is directly linked to: member (acceder), publication rights, moderator or creator. */
    @Query(value = "SELECT a.classe_id FROM ressources.acceder a WHERE a.utilisateur_id = :uid " +
            "UNION SELECT d.classe_id FROM ressources.droit_publication d WHERE d.utilisateur_id = :uid " +
            "UNION SELECT c.id FROM ressources.classes c WHERE c.moderator_id = :uid OR c.creator_id = :uid",
            nativeQuery = true)
    List<String> findLinkedClasseIds(@Param("uid") String utilisateurId);

    /** Linked classes of the user plus those of the user's children (parent_eleve). */
    @Query(value = "WITH ids AS (SELECT CAST(:uid AS varchar) AS id UNION SELECT pe.eleve_id FROM ressources.parent_eleve pe WHERE pe.parent_id = :uid) " +
            "SELECT a.classe_id FROM ressources.acceder a WHERE a.utilisateur_id IN (SELECT id FROM ids) " +
            "UNION SELECT d.classe_id FROM ressources.droit_publication d WHERE d.utilisateur_id IN (SELECT id FROM ids) " +
            "UNION SELECT c.id FROM ressources.classes c WHERE c.moderator_id IN (SELECT id FROM ids) OR c.creator_id IN (SELECT id FROM ids)",
            nativeQuery = true)
    List<String> findEffectiveClasseIds(@Param("uid") String utilisateurId);

    /** Everyone linked to one of the classes, plus the parents of those people. */
    @Query(value = "WITH m AS (" +
            "SELECT a.utilisateur_id AS id FROM ressources.acceder a WHERE a.classe_id IN (:cids) " +
            "UNION SELECT d.utilisateur_id FROM ressources.droit_publication d WHERE d.classe_id IN (:cids) " +
            "UNION SELECT c.moderator_id FROM ressources.classes c WHERE c.id IN (:cids) AND c.moderator_id IS NOT NULL " +
            "UNION SELECT c.creator_id FROM ressources.classes c WHERE c.id IN (:cids) AND c.creator_id IS NOT NULL) " +
            "SELECT id FROM m UNION SELECT pe.parent_id FROM ressources.parent_eleve pe WHERE pe.eleve_id IN (SELECT id FROM m)",
            nativeQuery = true)
    List<String> findContactIdsForClasses(@Param("cids") List<String> classeIds);

    /** Parents and children of the user. */
    @Query(value = "SELECT pe.eleve_id FROM ressources.parent_eleve pe WHERE pe.parent_id = :uid " +
            "UNION SELECT pe.parent_id FROM ressources.parent_eleve pe WHERE pe.eleve_id = :uid",
            nativeQuery = true)
    List<String> findFamilyIds(@Param("uid") String utilisateurId);

    @Query(value = "SELECT COUNT(*) FROM ressources.interactions i WHERE i.message_id = :mid", nativeQuery = true)
    long countInteractionsByMessageId(@Param("mid") String messageId);

    // ── Conversations (chat) ─────────────────────────────────────────────────

    String VISIBLE_DANS_CONVERSATION = "m.conversationId = :cid AND m.dateEnvoi > :apres " +
            "AND NOT EXISTS (SELECT s FROM MessageStatutEntity s WHERE s.id.messageId = m.id " +
            "AND s.id.utilisateurId = :uid AND s.supprime = true) ";

    /** Latest messages of a conversation visible to the user (newest first). */
    @Query("SELECT m FROM MessagesEntity m WHERE " + VISIBLE_DANS_CONVERSATION +
            "ORDER BY m.dateEnvoi DESC, m.id DESC")
    List<MessagesEntity> findPageConversation(@Param("cid") String conversationId, @Param("uid") String utilisateurId,
                                              @Param("apres") LocalDateTime apres, Pageable pageable);

    /** Messages strictly older than the cursor (date, id), newest first. */
    @Query("SELECT m FROM MessagesEntity m WHERE " + VISIBLE_DANS_CONVERSATION +
            "AND (m.dateEnvoi < :avant OR (m.dateEnvoi = :avant AND m.id < :avantId)) " +
            "ORDER BY m.dateEnvoi DESC, m.id DESC")
    List<MessagesEntity> findPageConversationAvant(@Param("cid") String conversationId, @Param("uid") String utilisateurId,
                                                   @Param("apres") LocalDateTime apres, @Param("avant") LocalDateTime avant,
                                                   @Param("avantId") String avantId, Pageable pageable);

    /** Last message visible to the user in each of the conversations: rows [conversation_id, message_id]. */
    @Query(value = "SELECT x.conversation_id, x.id FROM (" +
            "SELECT m.conversation_id, m.id, ROW_NUMBER() OVER (PARTITION BY m.conversation_id ORDER BY m.date_envoi DESC, m.id DESC) AS rn " +
            "FROM ressources.messages m " +
            "JOIN ressources.conversation_participants cp ON cp.conversation_id = m.conversation_id AND cp.utilisateur_id = :uid " +
            "LEFT JOIN ressources.message_statut s ON s.message_id = m.id AND s.utilisateur_id = :uid " +
            "WHERE m.conversation_id IN (:cids) AND COALESCE(s.supprime, FALSE) = FALSE " +
            "AND (cp.historique_depuis IS NULL OR m.date_envoi > cp.historique_depuis) " +
            "AND (cp.deleted_at IS NULL OR m.date_envoi > cp.deleted_at)) x WHERE x.rn = 1",
            nativeQuery = true)
    List<Object[]> findDerniersMessages(@Param("uid") String utilisateurId, @Param("cids") Collection<String> conversationIds);

    /** Unread messages per conversation for the user: rows [conversation_id, count]. */
    @Query(value = "SELECT m.conversation_id, COUNT(*) FROM ressources.messages m " +
            "JOIN ressources.conversation_participants cp ON cp.conversation_id = m.conversation_id AND cp.utilisateur_id = :uid " +
            "LEFT JOIN ressources.message_statut s ON s.message_id = m.id AND s.utilisateur_id = :uid " +
            "WHERE (m.expediteur_id IS NULL OR m.expediteur_id <> :uid) AND m.deleted = FALSE " +
            "AND COALESCE(s.supprime, FALSE) = FALSE AND COALESCE(s.lu, FALSE) = FALSE " +
            "AND (cp.last_read_at IS NULL OR m.date_envoi > cp.last_read_at) " +
            "AND (cp.historique_depuis IS NULL OR m.date_envoi > cp.historique_depuis) " +
            "AND (cp.deleted_at IS NULL OR m.date_envoi > cp.deleted_at) " +
            "GROUP BY m.conversation_id",
            nativeQuery = true)
    List<Object[]> countNonLusParConversation(@Param("uid") String utilisateurId);

    /** Received messages of a conversation the user has not marked read (message_statut). */
    @Query("SELECT m FROM MessagesEntity m WHERE m.conversationId = :cid AND m.dateEnvoi <= :jusqua " +
            "AND (m.expediteurEntity IS NULL OR m.expediteurEntity.id <> :uid) " +
            "AND NOT EXISTS (SELECT s FROM MessageStatutEntity s WHERE s.id.messageId = m.id " +
            "AND s.id.utilisateurId = :uid AND s.lu = true)")
    List<MessagesEntity> findNonMarquesLus(@Param("cid") String conversationId, @Param("uid") String utilisateurId,
                                           @Param("jusqua") LocalDateTime jusqua);

    /**
     * Last message of each conversation of the user's trash, as it was when it was deleted:
     * hidden for the user only by a permanent delete (historique_depuis). Rows [conversation_id, message_id].
     */
    @Query(value = "SELECT x.conversation_id, x.id FROM (" +
            "SELECT m.conversation_id, m.id, ROW_NUMBER() OVER (PARTITION BY m.conversation_id ORDER BY m.date_envoi DESC, m.id DESC) AS rn " +
            "FROM ressources.messages m " +
            "JOIN ressources.conversation_participants cp ON cp.conversation_id = m.conversation_id AND cp.utilisateur_id = :uid " +
            "LEFT JOIN ressources.message_statut s ON s.message_id = m.id AND s.utilisateur_id = :uid " +
            "WHERE m.conversation_id IN (:cids) AND COALESCE(s.supprime, FALSE) = FALSE " +
            "AND (cp.historique_depuis IS NULL OR m.date_envoi > cp.historique_depuis) " +
            "AND (cp.deleted_at IS NULL OR m.date_envoi <= cp.deleted_at)) x WHERE x.rn = 1",
            nativeQuery = true)
    List<Object[]> findDerniersMessagesCorbeille(@Param("uid") String utilisateurId, @Param("cids") Collection<String> conversationIds);

    /** Messages the user starred, per conversation (visible ones only): rows [conversation_id, count]. */
    @Query(value = "SELECT m.conversation_id, COUNT(*) FROM ressources.message_statut s " +
            "JOIN ressources.messages m ON m.id = s.message_id " +
            "JOIN ressources.conversation_participants cp ON cp.conversation_id = m.conversation_id AND cp.utilisateur_id = :uid " +
            "WHERE s.utilisateur_id = :uid AND s.favori = TRUE AND s.supprime = FALSE AND s.purge = FALSE " +
            "AND m.conversation_id IN (:cids) " +
            "AND (cp.historique_depuis IS NULL OR m.date_envoi > cp.historique_depuis) " +
            "AND (cp.deleted_at IS NULL OR m.date_envoi > cp.deleted_at) " +
            "GROUP BY m.conversation_id",
            nativeQuery = true)
    List<Object[]> countFavorisParConversation(@Param("uid") String utilisateurId, @Param("cids") Collection<String> conversationIds);
}
