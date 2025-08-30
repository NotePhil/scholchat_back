package cmr.notep.ressourcesjpa.repository;

import cmr.notep.ressourcesjpa.dao.ChapitreEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChapitreRepository extends JpaRepository<ChapitreEntity, String> {
    List<ChapitreEntity> findByCoursIdOrderByOrdre(String coursId);


    void deleteByCoursId(String coursId);
}