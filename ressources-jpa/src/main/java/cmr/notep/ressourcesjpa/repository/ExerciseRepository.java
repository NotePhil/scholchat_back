package cmr.notep.ressourcesjpa.repository;

import cmr.notep.modele.EtatCours;
import cmr.notep.modele.ListeNiveau;
import cmr.notep.ressourcesjpa.dao.ExerciseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExerciseRepository extends JpaRepository<ExerciseEntity, String> {
    List<ExerciseEntity> findByRedacteurId(String professeurId);
    List<ExerciseEntity> findByNiveau(ListeNiveau niveau);
    List<ExerciseEntity> findByRestriction(String restriction);
    List<ExerciseEntity> findByEtat(EtatCours etat);

    @Query("SELECT e FROM ExerciseEntity e WHERE e.restriction = 'PUBLIC' OR e.redacteur.id = :userId")
    List<ExerciseEntity> findAccessibleExercises(@Param("userId") String userId);

    @Query("SELECT e FROM ExerciseEntity e JOIN e.coursLies c WHERE c.id = :coursId")
    List<ExerciseEntity> findByCoursId(@Param("coursId") String coursId);
}