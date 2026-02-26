package cmr.notep.business.business.mappers;

import cmr.notep.interfaces.dto.*;
import cmr.notep.interfaces.modeles.Exercise;
import cmr.notep.ressourcesjpa.dao.CoursEntity;
import cmr.notep.ressourcesjpa.dao.ExerciseEntity;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import static cmr.notep.business.config.BusinessConfig.dozerMapperBean;

@Component
public class ExerciseMapper {
    public Exercise toEntity(ExerciseRequestDTO requestDTO) {
        return Exercise.builder()
                .nom(requestDTO.getNom())
                .description(requestDTO.getDescription())
                .niveau(requestDTO.getNiveau())
                .restriction(requestDTO.getRestriction())
                .redacteurId(requestDTO.getRedacteurId())
                .etat(requestDTO.getEtat())
                .build();
    }

    public ExerciseResponseDTO toResponseDTO(Exercise exercise) {
        return ExerciseResponseDTO.builder()
                .id(exercise.getId())
                .nom(exercise.getNom())
                .description(exercise.getDescription())
                .dateCreation(exercise.getDateCreation())
                .etat(exercise.getEtat())
                .restriction(exercise.getRestriction())
                .niveau(exercise.getNiveau())
                .redacteurId(exercise.getRedacteurId())
                .coursLies(new ArrayList<>())
                .matieres(new ArrayList<>())
                .questions(new ArrayList<>())
                .build();
    }

    public ExerciseResponseDTO toResponseDTO(ExerciseEntity entity) {
        List<CoursSummaryDTO> coursSummaryList = (entity.getCoursLies() != null) ?
                entity.getCoursLies().stream()
                        .map(cours -> CoursSummaryDTO.builder()
                                .id(cours.getId())
                                .titre(cours.getTitre())
                                .description(cours.getDescription())
                                .build())
                        .collect(Collectors.toList()) :
                new ArrayList<>();

        List<MatiereSummaryDTO> matiereSummaryList = (entity.getMatieres() != null) ?
                entity.getMatieres().stream()
                        .map(matiere -> MatiereSummaryDTO.builder()
                                .id(matiere.getId())
                                .nom(matiere.getNom())
                                .description(matiere.getDescription())
                                .build())
                        .collect(Collectors.toList()) :
                new ArrayList<>();

        List<QuestionReponseSummaryDTO> questionSummaryList = (entity.getQuestions() != null) ?
                entity.getQuestions().stream()
                        .map(question -> QuestionReponseSummaryDTO.builder()
                                .id(question.getId())
                                .intitule(question.getIntitule())
                                .typeQuestion(question.getTypeQuestion())
                                .build())
                        .collect(Collectors.toList()) :
                new ArrayList<>();

        return ExerciseResponseDTO.builder()
                .id(entity.getId())
                .nom(entity.getNom())
                .description(entity.getDescription())
                .dateCreation(entity.getDateCreation())
                .etat(entity.getEtat())
                .restriction(entity.getRestriction())
                .niveau(entity.getNiveau())
                .redacteurId(entity.getRedacteur().getId())
                .coursLies(coursSummaryList)
                .matieres(matiereSummaryList)
                .questions(questionSummaryList)
                .build();
    }

    public ExerciseEntity toEntity(Exercise exercise) {
        return dozerMapperBean.map(exercise, ExerciseEntity.class);
    }

    public Exercise toModel(ExerciseEntity entity) {
        return dozerMapperBean.map(entity, Exercise.class);
    }
}
