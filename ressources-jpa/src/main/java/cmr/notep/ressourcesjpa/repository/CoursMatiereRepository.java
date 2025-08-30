package cmr.notep.ressourcesjpa.repository;

import cmr.notep.ressourcesjpa.dao.CoursMatiereEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CoursMatiereRepository extends JpaRepository<CoursMatiereEntity, String> {
    List<CoursMatiereEntity> findByCoursId(String coursId);
}