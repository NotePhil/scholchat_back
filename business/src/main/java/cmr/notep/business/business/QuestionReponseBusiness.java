package cmr.notep.business.business;

import cmr.notep.business.exceptions.SchoolException;
import cmr.notep.business.exceptions.enums.SchoolErrorCode;
import cmr.notep.interfaces.modeles.QuestionReponse;
import cmr.notep.ressourcesjpa.commun.DaoAccessorService;
import cmr.notep.ressourcesjpa.dao.ExerciseEntity;
import cmr.notep.ressourcesjpa.dao.QuestionReponseEntity;
import cmr.notep.ressourcesjpa.repository.ExerciseRepository;
import cmr.notep.ressourcesjpa.repository.QuestionReponseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static cmr.notep.business.config.BusinessConfig.dozerMapperBean;

@Component
@Slf4j
@RequiredArgsConstructor
public class QuestionReponseBusiness {

    private final DaoAccessorService daoAccessorService;

    public QuestionReponse creerQuestion(String exerciseId, QuestionReponse question) {
        ExerciseEntity exercise = daoAccessorService.getRepository(ExerciseRepository.class)
                .findById(exerciseId)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Exercice introuvable"));
        QuestionReponseEntity entity = dozerMapperBean.map(question, QuestionReponseEntity.class);
        entity.setExercise(exercise);
        QuestionReponseEntity savedEntity = daoAccessorService.getRepository(QuestionReponseRepository.class).save(entity);
        return dozerMapperBean.map(savedEntity, QuestionReponse.class);
    }


    public List<QuestionReponse> obtenirQuestionsParExercise(String exerciseId) {
        return daoAccessorService.getRepository(QuestionReponseRepository.class)
                .findByExerciseId(exerciseId)
                .stream()
                .map(q -> dozerMapperBean.map(q, QuestionReponse.class))
                .collect(Collectors.toList());
    }

    public QuestionReponse mettreAJourQuestion(String questionId, QuestionReponse question) {
        QuestionReponseEntity existingEntity = daoAccessorService.getRepository(QuestionReponseRepository.class)
                .findById(questionId)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Question introuvable"));

        if (question.getIntitule() != null) {
            existingEntity.setIntitule(question.getIntitule());
        }
        if (question.getReponse() != null) {
            existingEntity.setReponse(question.getReponse());
        }
        if (question.getTypeQuestion() != null) {
            existingEntity.setTypeQuestion(question.getTypeQuestion());
        }

        QuestionReponseEntity updatedEntity = daoAccessorService.getRepository(QuestionReponseRepository.class).save(existingEntity);
        return dozerMapperBean.map(updatedEntity, QuestionReponse.class);
    }

    public void supprimerQuestion(String questionId) {
        QuestionReponseEntity question = daoAccessorService.getRepository(QuestionReponseRepository.class)
                .findById(questionId)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Question introuvable"));

        daoAccessorService.getRepository(QuestionReponseRepository.class).delete(question);
    }
}