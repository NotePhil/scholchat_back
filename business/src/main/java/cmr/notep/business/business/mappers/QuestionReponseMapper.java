package cmr.notep.business.business.mappers;

import cmr.notep.interfaces.dto.QuestionReponseRequestDTO;
import cmr.notep.interfaces.dto.QuestionReponseResponseDTO;
import cmr.notep.interfaces.modeles.QuestionReponse;
import cmr.notep.ressourcesjpa.dao.QuestionReponseEntity;
import org.springframework.stereotype.Component;

@Component
public class QuestionReponseMapper {

    public QuestionReponse toModel(QuestionReponseRequestDTO requestDTO) {
        return QuestionReponse.builder()
                .intitule(requestDTO.getIntitule())
                .reponse(requestDTO.getReponse())
                .typeQuestion(requestDTO.getTypeQuestion())
                .points(requestDTO.getPoints())
                .choixReponses(requestDTO.getChoixReponses())
                .medias(requestDTO.getMedias())
                .build();
    }

    public QuestionReponseResponseDTO toResponseDTO(QuestionReponseEntity entity) {
        return QuestionReponseResponseDTO.builder()
                .id(entity.getId())
                .intitule(entity.getIntitule())
                .reponse(entity.getReponse())
                .typeQuestion(entity.getTypeQuestion())
                .exerciseId(entity.getExercise() != null ? entity.getExercise().getId() : null)
                .build();
    }



    public QuestionReponseEntity toEntity(QuestionReponse model) {
        QuestionReponseEntity entity = new QuestionReponseEntity();
        entity.setIntitule(model.getIntitule());
        entity.setReponse(model.getReponse());
        entity.setTypeQuestion(model.getTypeQuestion());
        return entity;
    }
}