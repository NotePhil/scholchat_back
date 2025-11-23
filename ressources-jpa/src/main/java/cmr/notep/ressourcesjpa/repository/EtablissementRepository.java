package cmr.notep.ressourcesjpa.repository;

import cmr.notep.ressourcesjpa.dao.EtablissementEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EtablissementRepository extends JpaRepository<EtablissementEntity, String> {
    
    @Query("SELECT e FROM EtablissementEntity e WHERE e.gestionnaire.id = :gestionnaireId")
    List<EtablissementEntity> findByGestionnaireId(@Param("gestionnaireId") String gestionnaireId);
}
