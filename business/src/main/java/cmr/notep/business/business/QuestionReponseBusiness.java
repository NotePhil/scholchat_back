package cmr.notep.business.business;

import cmr.notep.business.exceptions.SchoolException;
import cmr.notep.business.exceptions.enums.SchoolErrorCode;
import cmr.notep.interfaces.modeles.ChoixReponse;
import cmr.notep.interfaces.modeles.QuestionReponse;
import cmr.notep.modele.TypeQuestion;
import cmr.notep.ressourcesjpa.commun.DaoAccessorService;
import cmr.notep.ressourcesjpa.dao.ChoixReponseEntity;
import cmr.notep.ressourcesjpa.dao.ExerciseEntity;
import cmr.notep.ressourcesjpa.dao.QuestionReponseEntity;
import cmr.notep.ressourcesjpa.repository.ExerciseRepository;
import cmr.notep.ressourcesjpa.repository.QuestionReponseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
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
        
        validateQuestion(question);
        
        QuestionReponseEntity entity = new QuestionReponseEntity();
        entity.setId(UUID.randomUUID().toString());
        entity.setIntitule(question.getIntitule());
        entity.setTypeQuestion(question.getTypeQuestion());
        entity.setPoints(question.getPoints());
        entity.setExercise(exercise);
        
        // Gérer selon le type de question
        switch (question.getTypeQuestion()) {
            case QCM:
            case ASSOCIATION:
            case CLASSEMENT:
                if (question.getChoixReponses() != null && !question.getChoixReponses().isEmpty()) {
                    List<ChoixReponseEntity> choixEntities = new ArrayList<>();
                    for (ChoixReponse choix : question.getChoixReponses()) {
                        ChoixReponseEntity choixEntity = new ChoixReponseEntity();
                        choixEntity.setId(UUID.randomUUID().toString());
                        choixEntity.setTexte(choix.getTexte());
                        choixEntity.setEstCorrect(choix.getEstCorrect());
                        choixEntity.setOrdreAffichage(choix.getOrdreAffichage());
                        choixEntity.setQuestion(entity);
                        choixEntities.add(choixEntity);
                    }
                    entity.setChoixReponses(choixEntities);
                }
                break;
            case VRAI_FAUX:
                entity.setReponseAttendueVraiFaux(question.getReponseAttendueVraiFaux());
                break;
            case REPONSE_COURTE:
                entity.setReponseAttendueCourte(question.getReponseAttendueCourte());
                break;
            case REPONSE_LONGUE:
            case TROU:
            case DEVELOPPEMENT:
                entity.setReponseAttendueLongue(question.getReponseAttendueLongue());
                break;
        }
        
        QuestionReponseEntity savedEntity = daoAccessorService.getRepository(QuestionReponseRepository.class).save(entity);
        return mapToModel(savedEntity);
    }

    private void validateQuestion(QuestionReponse question) {
        switch (question.getTypeQuestion()) {
            case QCM:
            case ASSOCIATION:
            case CLASSEMENT:
                if (question.getChoixReponses() == null || question.getChoixReponses().size() < 2) {
                    throw new SchoolException(SchoolErrorCode.BAD_REQUEST, "Au moins 2 choix requis pour " + question.getTypeQuestion());
                }
                long correctCount = question.getChoixReponses().stream().filter(ChoixReponse::getEstCorrect).count();
                if (correctCount == 0) {
                    throw new SchoolException(SchoolErrorCode.BAD_REQUEST, "Au moins une réponse correcte requise");
                }
                break;
            case VRAI_FAUX:
                if (question.getReponseAttendueVraiFaux() == null) {
                    throw new SchoolException(SchoolErrorCode.BAD_REQUEST, "Réponse Vrai/Faux requise");
                }
                break;
            case REPONSE_COURTE:
                if (question.getReponseAttendueCourte() == null || question.getReponseAttendueCourte().trim().isEmpty()) {
                    throw new SchoolException(SchoolErrorCode.BAD_REQUEST, "Réponse courte requise");
                }
                break;
        }
    }

    private QuestionReponse mapToModel(QuestionReponseEntity entity) {
        QuestionReponse model = new QuestionReponse();
        model.setId(entity.getId());
        model.setIntitule(entity.getIntitule());
        model.setTypeQuestion(entity.getTypeQuestion());
        model.setPoints(entity.getPoints());
        model.setExerciseId(entity.getExercise().getId());
        
        if (entity.getChoixReponses() != null && !entity.getChoixReponses().isEmpty()) {
            model.setChoixReponses(entity.getChoixReponses().stream()
                .map(c -> ChoixReponse.builder()
                    .id(c.getId())
                    .texte(c.getTexte())
                    .estCorrect(c.getEstCorrect())
                    .ordreAffichage(c.getOrdreAffichage())
                    .build())
                .collect(Collectors.toList()));
        }
        
        model.setReponseAttendueVraiFaux(entity.getReponseAttendueVraiFaux());
        model.setReponseAttendueCourte(entity.getReponseAttendueCourte());
        model.setReponseAttendueLongue(entity.getReponseAttendueLongue());
        
        return model;
    }

    public List<QuestionReponse> obtenirQuestionsParExercise(String exerciseId) {
        return daoAccessorService.getRepository(QuestionReponseRepository.class)
                .findByExerciseId(exerciseId)
                .stream()
                .map(this::mapToModel)
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
        return mapToModel(updatedEntity);
    }

    public void supprimerQuestion(String questionId) {
        QuestionReponseEntity question = daoAccessorService.getRepository(QuestionReponseRepository.class)
                .findById(questionId)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Question introuvable"));

        daoAccessorService.getRepository(QuestionReponseRepository.class).delete(question);
    }
}