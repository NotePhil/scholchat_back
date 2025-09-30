package cmr.notep.ressourcesjpa.repository;

import cmr.notep.ressourcesjpa.dao.QuestionReponseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionReponseRepository extends JpaRepository<QuestionReponseEntity, String> {
    List<QuestionReponseEntity> findByExerciseId(String exerciseId);
    List<QuestionReponseEntity> findByTypeQuestion(String typeQuestion);
}