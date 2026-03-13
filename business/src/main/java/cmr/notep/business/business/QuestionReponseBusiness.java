package cmr.notep.business.business;

import cmr.notep.business.exceptions.SchoolException;
import cmr.notep.business.exceptions.enums.SchoolErrorCode;
import cmr.notep.interfaces.modeles.QuestionReponse;
import cmr.notep.ressourcesjpa.commun.DaoAccessorService;
import cmr.notep.ressourcesjpa.dao.ChoixReponseEntity;
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
        
        QuestionReponseEntity entity = new QuestionReponseEntity();
        entity.setId(UUID.randomUUID().toString());
        entity.setIntitule(question.getIntitule());
        entity.setReponse(question.getReponse());
        entity.setTypeQuestion(question.getTypeQuestion());
        entity.setPoints(question.getPoints());
        entity.setExercise(exercise);
        
        // Gérer les choix de réponses pour les QCM
        if (question.getChoixReponses() != null && !question.getChoixReponses().isEmpty()) {
            for (cmr.notep.interfaces.dto.ChoixReponseDTO choixDTO : question.getChoixReponses()) {
                ChoixReponseEntity choix = new ChoixReponseEntity();
                choix.setId(UUID.randomUUID().toString());
                choix.setTexte(choixDTO.getTexte());
                choix.setEstCorrect(choixDTO.getEstCorrect());
                choix.setOrdreAffichage(choixDTO.getOrdreAffichage());
                choix.setQuestion(entity);
                entity.getChoixReponses().add(choix);
            }
        }
        
        QuestionReponseEntity savedEntity = daoAccessorService.getRepository(QuestionReponseRepository.class).save(entity);
        return mapToQuestionReponse(savedEntity);
    }

    private QuestionReponse mapToQuestionReponse(QuestionReponseEntity entity) {
        QuestionReponse question = new QuestionReponse();
        question.setId(entity.getId());
        question.setIntitule(entity.getIntitule());
        question.setReponse(entity.getReponse());
        question.setTypeQuestion(entity.getTypeQuestion());
        question.setPoints(entity.getPoints());
        if (entity.getExercise() != null) {
            question.setExerciseId(entity.getExercise().getId());
        }
        
        if (entity.getChoixReponses() != null && !entity.getChoixReponses().isEmpty()) {
            question.setChoixReponses(entity.getChoixReponses().stream()
                .map(choix -> cmr.notep.interfaces.dto.ChoixReponseDTO.builder()
                    .id(choix.getId())
                    .texte(choix.getTexte())
                    .estCorrect(choix.getEstCorrect())
                    .ordreAffichage(choix.getOrdreAffichage())
                    .build())
                .collect(Collectors.toList()));
        }
        
        return question;
    }


    public List<QuestionReponse> obtenirQuestionsParExercise(String exerciseId) {
        List<QuestionReponseEntity> entities = daoAccessorService.getRepository(QuestionReponseRepository.class)
                .findByExerciseId(exerciseId);
        
        log.info("Nombre de questions trouvées: {}", entities.size());
        
        return entities.stream()
                .map(entity -> {
                    log.info("Question ID: {}, Nombre de choix: {}", 
                        entity.getId(), 
                        entity.getChoixReponses() != null ? entity.getChoixReponses().size() : 0);
                    return mapToQuestionReponse(entity);
                })
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