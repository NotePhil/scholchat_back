package cmr.notep.ressourcesjpa.repository;

import cmr.notep.ressourcesjpa.dao.QuestionReponseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionReponseRepository extends JpaRepository<QuestionReponseEntity, String> {
    @Query("SELECT DISTINCT q FROM QuestionReponseEntity q LEFT JOIN FETCH q.choixReponses WHERE q.exercise.id = :exerciseId")
    List<QuestionReponseEntity> findByExerciseId(@Param("exerciseId") String exerciseId);
    
    List<QuestionReponseEntity> findByTypeQuestion(String typeQuestion);
}