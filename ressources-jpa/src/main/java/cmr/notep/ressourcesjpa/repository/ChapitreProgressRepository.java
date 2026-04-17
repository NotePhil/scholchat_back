package cmr.notep.ressourcesjpa.repository;

import cmr.notep.ressourcesjpa.dao.ChapitreProgressEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChapitreProgressRepository extends JpaRepository<ChapitreProgressEntity, String> {
    List<ChapitreProgressEntity> findByUserIdAndCoursId(String userId, String coursId);
    Optional<ChapitreProgressEntity> findByUserIdAndChapitreId(String userId, String chapitreId);
}
