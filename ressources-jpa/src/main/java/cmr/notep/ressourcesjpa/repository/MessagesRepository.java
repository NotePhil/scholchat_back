package cmr.notep.ressourcesjpa.repository;

import cmr.notep.ressourcesjpa.dao.MessagesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
}
