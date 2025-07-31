package cmr.notep.ressourcesjpa.repository;

import cmr.notep.ressourcesjpa.dao.ParentsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParentsRepository extends JpaRepository<ParentsEntity, String> {
    @Query("SELECT p FROM ParentsEntity p JOIN p.enfants e WHERE e.id = :eleveId")
    List<ParentsEntity> findByEnfantId(@Param("eleveId") String eleveId);
}
