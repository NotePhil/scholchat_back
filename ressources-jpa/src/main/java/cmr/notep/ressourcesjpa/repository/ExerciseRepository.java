package cmr.notep.ressourcesjpa.repository;

import cmr.notep.modele.EtatExercise; // Changez de EtatCours à EtatExercise
import cmr.notep.modele.ListeNiveau;
import cmr.notep.ressourcesjpa.dao.ExerciseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExerciseRepository extends JpaRepository<ExerciseEntity, String> {
    
    @Query("SELECT DISTINCT e FROM ExerciseEntity e " +
           "LEFT JOIN FETCH e.questions " +
           "WHERE e.redacteur.id = :professeurId")
    List<ExerciseEntity> findByRedacteurIdWithDetails(@Param("professeurId") String professeurId);
    
    @Query("SELECT DISTINCT e FROM ExerciseEntity e " +
           "LEFT JOIN FETCH e.questions " +
           "WHERE e.id = :id")
    Optional<ExerciseEntity> findByIdWithDetails(@Param("id") String id);
    
    List<ExerciseEntity> findByRedacteurId(String professeurId);
    List<ExerciseEntity> findByNiveau(ListeNiveau niveau);
    List<ExerciseEntity> findByRestriction(String restriction);
    List<ExerciseEntity> findByEtat(EtatExercise etat); // Utilisez EtatExercise ici

    @Query("SELECT e FROM ExerciseEntity e WHERE e.restriction = 'PUBLIC' OR e.redacteur.id = :userId")
    List<ExerciseEntity> findAccessibleExercises(@Param("userId") String userId);

    @Query("SELECT e FROM ExerciseEntity e JOIN e.coursLies c WHERE c.id = :coursId")
    List<ExerciseEntity> findByCoursId(@Param("coursId") String coursId);
}