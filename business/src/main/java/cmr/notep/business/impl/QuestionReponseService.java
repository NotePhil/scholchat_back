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
        return mapToResponseDTO(createdQuestion);
    }

    private QuestionReponseResponseDTO mapToResponseDTO(QuestionReponse question) {
        QuestionReponseResponseDTO dto = new QuestionReponseResponseDTO();
        dto.setId(question.getId());
        dto.setIntitule(question.getIntitule());
        dto.setReponse(question.getReponse());
        dto.setTypeQuestion(question.getTypeQuestion());
        dto.setExerciseId(question.getExerciseId());
        dto.setPoints(question.getPoints());
        dto.setChoixReponses(question.getChoixReponses());
        dto.setMedias(question.getMedias());
        return dto;
    }


    @Override
    public List<QuestionReponseResponseDTO> obtenirQuestionsParExercise(String exerciseId) {
        log.info("Récupération des questions pour l'exercice: {}", exerciseId);
        return questionReponseBusiness.obtenirQuestionsParExercise(exerciseId)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public QuestionReponseResponseDTO mettreAJourQuestion(String questionId, QuestionReponseRequestDTO questionRequestDTO) {
        log.info("Mise à jour de la question: {}", questionId);
        QuestionReponse question = questionReponseMapper.toModel(questionRequestDTO);
        QuestionReponse updatedQuestion = questionReponseBusiness.mettreAJourQuestion(questionId, question);
        return mapToResponseDTO(updatedQuestion);
    }

    @Override
    public void supprimerQuestion(String questionId) {
        log.info("Suppression de la question: {}", questionId);
        questionReponseBusiness.supprimerQuestion(questionId);
    }
}