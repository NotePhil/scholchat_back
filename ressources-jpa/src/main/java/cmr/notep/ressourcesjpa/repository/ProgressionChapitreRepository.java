package cmr.notep.ressourcesjpa.repository;

import cmr.notep.ressourcesjpa.dao.ProgressionChapitreEntity;
import cmr.notep.ressourcesjpa.dao.ProgressionChapitreId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProgressionChapitreRepository extends JpaRepository<ProgressionChapitreEntity, ProgressionChapitreId> {

    List<ProgressionChapitreEntity> findByIdUtilisateurId(String utilisateurId);

    @Query("SELECT p FROM ProgressionChapitreEntity p WHERE p.id.utilisateurId = :userId AND p.chapitre.cours.id = :coursId")
    List<ProgressionChapitreEntity> findByUtilisateurIdAndCoursId(@Param("userId") String userId, @Param("coursId") String coursId);

    boolean existsById(ProgressionChapitreId id);
}
