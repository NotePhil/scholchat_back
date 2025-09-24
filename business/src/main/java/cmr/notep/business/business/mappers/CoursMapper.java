package cmr.notep.business.business.mappers;


import cmr.notep.interfaces.dto.CoursRequestDTO;
import cmr.notep.interfaces.dto.CoursResponseDTO;
import cmr.notep.interfaces.dto.ExerciseSummaryDTO;
import cmr.notep.interfaces.modeles.Cours;
import cmr.notep.ressourcesjpa.dao.CoursEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CoursMapper {

    public Cours toEntity(CoursRequestDTO requestDTO) {
        return Cours.builder()
                .titre(requestDTO.getTitre())
                .description(requestDTO.getDescription())
                .etat(requestDTO.getEtat())
                .references(requestDTO.getReferences())
                .restriction(requestDTO.getRestriction())
                .redacteurId(requestDTO.getRedacteurId())
                .build();
    }

    public CoursResponseDTO toResponseDTO(CoursEntity entity) {
        List<ExerciseSummaryDTO> exerciseSummaryList = entity.getExercisesLies().stream()
                .map(exercise -> ExerciseSummaryDTO.builder()
                        .id(exercise.getId())
                        .nom(exercise.getNom())
                        .description(exercise.getDescription())
                        .etat(exercise.getEtat())
                        .niveau(exercise.getNiveau())
                        .build())
                .collect(Collectors.toList());

        return CoursResponseDTO.builder()
                .id(entity.getId())
                .titre(entity.getTitre())
                .description(entity.getDescription())
                .dateCreation(entity.getDateCreation())
                .etat(entity.getEtat())
                .references(entity.getReferences())
                .restriction(entity.getRestriction())
                .redacteurId(entity.getRedacteur().getId())
                .exercisesLies(exerciseSummaryList)
                .build();
    }
}