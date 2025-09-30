package cmr.notep.ressourcesjpa.repository;

import cmr.notep.ressourcesjpa.dao.RepondreEntity;
import cmr.notep.ressourcesjpa.dao.RepondreId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RepondreRepository extends JpaRepository<RepondreEntity, RepondreId> {

    List<RepondreEntity> findByUtilisateurId(String utilisateurId);

    List<RepondreEntity> findByQuestionId(String questionId);

    Optional<RepondreEntity> findByUtilisateurIdAndQuestionId(String utilisateurId, String questionId);

    @Query("SELECT r FROM RepondreEntity r WHERE r.question.exercise.id = :exerciseId")
    List<RepondreEntity> findByExerciseId(@Param("exerciseId") String exerciseId);

    @Query("SELECT r FROM RepondreEntity r WHERE r.utilisateur.id = :utilisateurId AND r.question.exercise.id = :exerciseId")
    List<RepondreEntity> findByUtilisateurIdAndExerciseId(@Param("utilisateurId") String utilisateurId,
                                                          @Param("exerciseId") String exerciseId);

    boolean existsByUtilisateurIdAndQuestionId(String utilisateurId, String questionId);
}