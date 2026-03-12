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

}