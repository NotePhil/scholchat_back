package cmr.notep.ressourcesjpa.repository;

import cmr.notep.ressourcesjpa.dao.ParticiperExoEntity;
import cmr.notep.ressourcesjpa.dao.ParticiperExoId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParticiperExoRepository extends JpaRepository<ParticiperExoEntity, ParticiperExoId> {

    List<ParticiperExoEntity> findByUtilisateurId(String utilisateurId);

    List<ParticiperExoEntity> findByExerciseProgrammerId(String exerciseProgrammerId);

    Optional<ParticiperExoEntity> findByUtilisateurIdAndExerciseProgrammerId(String utilisateurId, String exerciseProgrammerId);

    @Query("SELECT p FROM ParticiperExoEntity p WHERE p.exerciseProgrammer.id = :exerciseProgrammerId AND p.note IS NOT NULL")
    List<ParticiperExoEntity> findEvaluationsByExerciseProgrammerId(@Param("exerciseProgrammerId") String exerciseProgrammerId);

    boolean existsByUtilisateurIdAndExerciseProgrammerId(String utilisateurId, String exerciseProgrammerId);

    @Query("SELECT COUNT(p) FROM ParticiperExoEntity p WHERE p.exerciseProgrammer.id = :exerciseProgrammerId")
    long countParticipantsByExerciseProgrammerId(@Param("exerciseProgrammerId") String exerciseProgrammerId);
}