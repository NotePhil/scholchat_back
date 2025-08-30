package cmr.notep.ressourcesjpa.repository;

import cmr.notep.ressourcesjpa.dao.MessagesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessagesRepository extends JpaRepository<MessagesEntity, String> {
    List<MessagesEntity> findByExpediteurEntityId(String utilisateurId);

    @Query("SELECT m FROM MessagesEntity m JOIN m.destinatairesEntities d WHERE d.id = :utilisateurId")
    List<MessagesEntity> findByDestinatairesEntitiesId(@Param("utilisateurId") String utilisateurId);

}