package cmr.notep.business.impl;
import static cmr.notep.business.config.BusinessConfig.dozerMapperBean;

import cmr.notep.business.business.QuestionReponseBusiness;
import cmr.notep.business.business.mappers.QuestionReponseMapper;
import cmr.notep.interfaces.api.QuestionReponseApi;
import cmr.notep.interfaces.dto.QuestionReponseRequestDTO;
import cmr.notep.interfaces.dto.QuestionReponseResponseDTO;
import cmr.notep.interfaces.modeles.QuestionReponse;
import cmr.notep.ressourcesjpa.dao.QuestionReponseEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequiredArgsConstructor
public class QuestionReponseService implements QuestionReponseApi {

    private final QuestionReponseBusiness questionReponseBusiness;
    private final QuestionReponseMapper questionReponseMapper;

    @Override
    public QuestionReponseResponseDTO creerQuestion(String exerciseId, QuestionReponseRequestDTO questionRequestDTO) {
        log.info("Création d'une nouvelle question pour l'exercice: {}", exerciseId);
        QuestionReponse question = questionReponseMapper.toModel(questionRequestDTO);
        QuestionReponse createdQuestion = questionReponseBusiness.creerQuestion(exerciseId, question);
        // Pas besoin de refaire un mapping ici, car `createdQuestion` est déjà un modèle.
        // On récupère l'entité depuis la base pour être sûr que tout est bien lié.
        QuestionReponseEntity savedEntity = dozerMapperBean.map(createdQuestion, QuestionReponseEntity.class);
        return questionReponseMapper.toResponseDTO(savedEntity);
    }


    @Override
    public List<QuestionReponseResponseDTO> obtenirQuestionsParExercise(String exerciseId) {
        log.info("Récupération des questions pour l'exercice: {}", exerciseId);
        return questionReponseBusiness.obtenirQuestionsParExercise(exerciseId)
                .stream()
                .map(q -> questionReponseMapper.toResponseDTO(
                        dozerMapperBean.map(q, cmr.notep.ressourcesjpa.dao.QuestionReponseEntity.class)
                ))
                .collect(Collectors.toList());
    }

    @Override
    public QuestionReponseResponseDTO mettreAJourQuestion(String questionId, QuestionReponseRequestDTO questionRequestDTO) {
        log.info("Mise à jour de la question: {}", questionId);
        QuestionReponse question = questionReponseMapper.toModel(questionRequestDTO);
        QuestionReponse updatedQuestion = questionReponseBusiness.mettreAJourQuestion(questionId, question);
        return questionReponseMapper.toResponseDTO(
                dozerMapperBean.map(updatedQuestion, cmr.notep.ressourcesjpa.dao.QuestionReponseEntity.class)
        );
    }

    @Override
    public void supprimerQuestion(String questionId) {
        log.info("Suppression de la question: {}", questionId);
        questionReponseBusiness.supprimerQuestion(questionId);
    }
}