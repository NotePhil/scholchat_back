package cmr.notep.business.business.mappers;

import cmr.notep.interfaces.dto.CoursSummaryDTO;
import cmr.notep.interfaces.dto.ExerciseRequestDTO;
import cmr.notep.interfaces.dto.ExerciseResponseDTO;
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

    // Convertit un ExerciseRequestDTO en Exercise
    public Exercise toEntity(ExerciseRequestDTO requestDTO) {
        return Exercise.builder()
                .nom(requestDTO.getNom())
                .description(requestDTO.getDescription())
                .niveau(requestDTO.getNiveau())
                .restriction(requestDTO.getRestriction())
                .redacteurId(requestDTO.getRedacteurId())
                .build();
    }

    // Convertit un ExerciseEntity en ExerciseResponseDTO
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
                .build();
    }

    // Convertit un Exercise en ExerciseEntity
    public ExerciseEntity toEntity(Exercise exercise) {
        return dozerMapperBean.map(exercise, ExerciseEntity.class);
    }

    // Convertit un ExerciseEntity en Exercise
    public Exercise toModel(ExerciseEntity entity) {
        return dozerMapperBean.map(entity, Exercise.class);
    }
}
