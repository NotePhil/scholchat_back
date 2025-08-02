package cmr.notep.ressourcesjpa.repository;

import cmr.notep.ressourcesjpa.dao.ElevesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ElevesRepository extends JpaRepository<ElevesEntity, String> {
    @Query("SELECT e FROM ElevesEntity e JOIN e.parents p WHERE p.id = :parentId")
    List<ElevesEntity> findByParentId(@Param("parentId") String parentId);
}