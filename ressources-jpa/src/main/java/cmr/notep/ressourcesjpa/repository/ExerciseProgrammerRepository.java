package cmr.notep.ressourcesjpa.repository;

import cmr.notep.modele.EtatExercise;
import cmr.notep.ressourcesjpa.dao.ExerciseProgrammerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface ExerciseProgrammerRepository extends JpaRepository<ExerciseProgrammerEntity, String> {
    List<ExerciseProgrammerEntity> findByProgrammeParId(String professeurId);
    List<ExerciseProgrammerEntity> findByEtat(EtatExercise etat);  // Utilise EtatExercise
    @Query("SELECT ep FROM ExerciseProgrammerEntity ep WHERE ep.programmePar.id = :professeurId AND ep.dateExoPrevue BETWEEN :startDate AND :endDate")
    List<ExerciseProgrammerEntity> findByProgrammeParAndDateRange(
            @Param("professeurId") String professeurId,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate);
    @Query("SELECT ep FROM ExerciseProgrammerEntity ep JOIN ep.classesDiffusees c WHERE c.id = :classeId")
    List<ExerciseProgrammerEntity> findByClasseId(@Param("classeId") String classeId);
    
    @Query("SELECT ep FROM ExerciseProgrammerEntity ep WHERE ep.exercise.id = :exerciseId")
    List<ExerciseProgrammerEntity> findByExerciseId(@Param("exerciseId") String exerciseId);
    
    @Query("SELECT ep FROM ExerciseProgrammerEntity ep WHERE ep.dateExoPrevue < :currentDate AND ep.etat = 'PUBLIE'")
    List<ExerciseProgrammerEntity> findExercisesToStart(@Param("currentDate") Date currentDate);
}