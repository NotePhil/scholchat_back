package cmr.notep.ressourcesjpa.repository;

import cmr.notep.ressourcesjpa.dao.CanalEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CanalRepository extends JpaRepository<CanalEntity, String> {
    List<CanalEntity> findByClasseId(String classeId);
    List<CanalEntity> findByProfesseurId(String professeurId);
}
