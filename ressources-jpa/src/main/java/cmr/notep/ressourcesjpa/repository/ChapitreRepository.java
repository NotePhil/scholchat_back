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

    @Query("SELECT c FROM ChapitreEntity c WHERE c.cours.id = :coursId AND c.matiere.id = :matiereId ORDER BY c.ordre")
    List<ChapitreEntity> findByCoursIdAndMatiereIdOrderByOrdre(
            @Param("coursId") String coursId,
            @Param("matiereId") String matiereId);

    void deleteByCoursId(String coursId);
}