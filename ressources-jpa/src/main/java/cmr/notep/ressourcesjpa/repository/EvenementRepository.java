package cmr.notep.ressourcesjpa.repository;

import cmr.notep.ressourcesjpa.dao.EvenementEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EvenementRepository extends JpaRepository<EvenementEntity, String> {
    boolean existsByTitre(String titre);
    @Query("SELECT e FROM EvenementEntity e WHERE e.createur.id = :professeurId")
    List<EvenementEntity> findByCreateurId(@Param("professeurId") String professeurId);

    List<EvenementEntity> findByCreateur_Id(String professeurId);
}