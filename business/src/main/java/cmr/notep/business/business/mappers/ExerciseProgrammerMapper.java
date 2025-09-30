package cmr.notep.business.business.mappers;

import cmr.notep.interfaces.dto.*;
import cmr.notep.interfaces.modeles.ExerciseProgrammer;
import cmr.notep.ressourcesjpa.dao.ExerciseProgrammerEntity;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import static cmr.notep.business.config.BusinessConfig.dozerMapperBean;

@Component
public class ExerciseProgrammerMapper {
    public ExerciseProgrammer toModel(ExerciseProgrammerRequestDTO requestDTO) {
        ExerciseProgrammer exerciseProgrammer = dozerMapperBean.map(requestDTO, ExerciseProgrammer.class);
        exerciseProgrammer.setProgrammeParId(requestDTO.getProgrammeParId());
        exerciseProgrammer.setDateExoPrevue(requestDTO.getDateExoPrevue());
        exerciseProgrammer.setDateDebutExoEffectif(requestDTO.getDateDebutExoEffectif());
        exerciseProgrammer.setDateFinExoEffectif(requestDTO.getDateFinExoEffectif());
        exerciseProgrammer.setEtat(requestDTO.getEtat());
        return exerciseProgrammer;
    }

    public ExerciseProgrammerResponseDTO toResponseDTO(ExerciseProgrammerEntity entity) {
        List<ClasseSummaryDTO> classesDiffusees = (entity.getClassesDiffusees() != null) ?
                entity.getClassesDiffusees().stream()
                        .map(classe -> ClasseSummaryDTO.builder()
                                .id(classe.getId())
                                .nom(classe.getNom())
                                .niveau(classe.getNiveau())
                                .codeActivation(classe.getCodeActivation())
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

        List<CoursSummaryDTO> coursSummaryList = (entity.getCoursLies() != null) ?
                entity.getCoursLies().stream()
                        .map(cours -> CoursSummaryDTO.builder()
                                .id(cours.getId())
                                .titre(cours.getTitre())
                                .description(cours.getDescription())
                                .build())
                        .collect(Collectors.toList()) :
                new ArrayList<>();

        return ExerciseProgrammerResponseDTO.builder()
                .id(entity.getId())
                .nom(entity.getNom())
                .description(entity.getDescription())
                .dateCreation(entity.getDateCreation())
                .etat(entity.getEtat())
                .restriction(entity.getRestriction())
                .niveau(entity.getNiveau())
                .redacteurId(entity.getRedacteur().getId())
                .programmeParId(entity.getProgrammePar().getId())
                .programmeParNom(entity.getProgrammePar().getNom())
                .programmeParPrenom(entity.getProgrammePar().getPrenom())
                .dateExoPrevue(entity.getDateExoPrevue())
                .dateDebutExoEffectif(entity.getDateDebutExoEffectif())
                .dateFinExoEffectif(entity.getDateFinExoEffectif())
                .coursLies(coursSummaryList)
                .matieres(matiereSummaryList)
                .questions(questionSummaryList)
                .classesDiffusees(classesDiffusees)
                .build();
    }

    public ExerciseProgrammerEntity toEntity(ExerciseProgrammer exerciseProgrammer) {
        return dozerMapperBean.map(exerciseProgrammer, ExerciseProgrammerEntity.class);
    }

    public ExerciseProgrammer toModel(ExerciseProgrammerEntity entity) {
        return dozerMapperBean.map(entity, ExerciseProgrammer.class);
    }
}
