package cmr.notep.business.business.mappers;

import cmr.notep.interfaces.dto.ChoixReponseDTO;
import cmr.notep.interfaces.dto.QuestionReponseRequestDTO;
import cmr.notep.interfaces.dto.QuestionReponseResponseDTO;
import cmr.notep.interfaces.modeles.ChoixReponse;
import cmr.notep.interfaces.modeles.QuestionReponse;
import cmr.notep.ressourcesjpa.dao.QuestionReponseEntity;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class QuestionReponseMapper {

    public QuestionReponse toModel(QuestionReponseRequestDTO requestDTO) {
        QuestionReponse model = QuestionReponse.builder()
                .intitule(requestDTO.getIntitule())
                .typeQuestion(requestDTO.getTypeQuestion())
                .points(requestDTO.getPoints())
                .reponseAttendueVraiFaux(requestDTO.getReponseAttendueVraiFaux())
                .reponseAttendueCourte(requestDTO.getReponseAttendueCourte())
                .reponseAttendueLongue(requestDTO.getReponseAttendueLongue())
                .build();
        
        if (requestDTO.getChoixReponses() != null) {
            model.setChoixReponses(requestDTO.getChoixReponses().stream()
                .map(dto -> ChoixReponse.builder()
                    .texte(dto.getTexte())
                    .estCorrect(dto.getEstCorrect())
                    .ordreAffichage(dto.getOrdreAffichage())
                    .build())
                .collect(Collectors.toList()));
        }
        
        return model;
    }

    public QuestionReponseResponseDTO toResponseDTO(QuestionReponseEntity entity) {
        QuestionReponseResponseDTO dto = QuestionReponseResponseDTO.builder()
                .id(entity.getId())
                .intitule(entity.getIntitule())
                .typeQuestion(entity.getTypeQuestion())
                .points(entity.getPoints())
                .exerciseId(entity.getExercise() != null ? entity.getExercise().getId() : null)
                .reponseAttendueVraiFaux(entity.getReponseAttendueVraiFaux())
                .reponseAttendueCourte(entity.getReponseAttendueCourte())
                .reponseAttendueLongue(entity.getReponseAttendueLongue())
                .build();
        
        if (entity.getChoixReponses() != null && !entity.getChoixReponses().isEmpty()) {
            dto.setChoixReponses(entity.getChoixReponses().stream()
                .map(choix -> ChoixReponseDTO.builder()
                    .id(choix.getId())
                    .texte(choix.getTexte())
                    .estCorrect(choix.getEstCorrect())
                    .ordreAffichage(choix.getOrdreAffichage())
                    .build())
                .collect(Collectors.toList()));
        }
        
        return dto;
    }

    public QuestionReponseEntity toEntity(QuestionReponse model) {
        QuestionReponseEntity entity = new QuestionReponseEntity();
        entity.setIntitule(model.getIntitule());
        entity.setReponse(model.getReponse());
        entity.setTypeQuestion(model.getTypeQuestion());
        return entity;
    }
}