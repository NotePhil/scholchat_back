package cmr.notep.ressourcesjpa.repository;

import cmr.notep.ressourcesjpa.dao.ParentEleveEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParentEleveRepository extends JpaRepository<ParentEleveEntity, Long> {
    List<ParentEleveEntity> findByParentId(String parentId);
    boolean existsByParentIdAndEleveId(String parentId, String eleveId);
}