package cmr.notep.business.business.mappers;

import cmr.notep.interfaces.dto.*;
import cmr.notep.interfaces.modeles.ExerciseProgrammer;
import cmr.notep.modele.EtatExercise;
import cmr.notep.ressourcesjpa.dao.ExerciseProgrammerEntity;
import cmr.notep.ressourcesjpa.dao.ParticiperExoEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static cmr.notep.business.config.BusinessConfig.dozerMapperBean;

@Component
public class ExerciseProgrammerMapper {

    public ExerciseProgrammer toModel(ExerciseProgrammerRequestDTO requestDTO) {
        ExerciseProgrammer exerciseProgrammer = new ExerciseProgrammer();
        exerciseProgrammer.setExerciseId(requestDTO.getExerciseId());
        exerciseProgrammer.setProgrammeParId(requestDTO.getProgrammeParId());
        exerciseProgrammer.setDateExoPrevue(requestDTO.getDateExoPrevue());
        exerciseProgrammer.setDateDebutExoEffectif(requestDTO.getDateDebutExoEffectif());
        exerciseProgrammer.setDateFinExoEffectif(requestDTO.getDateFinExoEffectif());

        if (requestDTO.getEtat() != null) {
            exerciseProgrammer.setEtat(requestDTO.getEtat());
        } else {
            exerciseProgrammer.setEtat(EtatExercise.BROUILLON);
        }

        // Convertir classeIds en classesDiffusees
        if (requestDTO.getClasseIds() != null && !requestDTO.getClasseIds().isEmpty()) {
            List<cmr.notep.interfaces.modeles.Classes> classes = requestDTO.getClasseIds().stream()
                .map(id -> {
                    cmr.notep.interfaces.modeles.Classes classe = new cmr.notep.interfaces.modeles.Classes();
                    classe.setId(id);
                    return classe;
                })
                .collect(Collectors.toList());
            exerciseProgrammer.setClassesDiffusees(classes);
        }

        return exerciseProgrammer;
    }
    private ParticipationExerciseResponseDTO mapParticipationToDTO(ParticiperExoEntity participation) {
        return ParticipationExerciseResponseDTO.builder()
                .utilisateurId(participation.getUtilisateur().getId())
                .utilisateurNom(participation.getUtilisateur().getNom())
                .utilisateurPrenom(participation.getUtilisateur().getPrenom())
                .exerciseProgrammerId(participation.getExerciseProgrammer().getId())
                .exerciseProgrammerNom(participation.getExerciseProgrammer().getExercise().getNom())
                .note(participation.getNote())
                .appreciation(participation.getAppreciation())
                .dateDebut(participation.getDateDebut())
                .dateFin(participation.getDateFin())
                .dateSoumission(participation.getDateSoumission())
                .build();
    }
    public ExerciseProgrammerResponseDTO toResponseDTO(ExerciseProgrammerEntity entity) {
        // Récupérer les données de l'exercice parent
        ExerciseProgrammerResponseDTO responseDTO = ExerciseProgrammerResponseDTO.builder()
                .id(entity.getId())
                .nom(entity.getExercise().getNom())
                .description(entity.getExercise().getDescription())
                .dateCreation(entity.getExercise().getDateCreation())
                .etat(entity.getEtat())
                .restriction(entity.getExercise().getRestriction())
                .niveau(entity.getExercise().getNiveau())
                .redacteurId(entity.getExercise().getRedacteur().getId())
                .programmeParId(entity.getProgrammePar().getId())
                .programmeParNom(entity.getProgrammePar().getNom())
                .programmeParPrenom(entity.getProgrammePar().getPrenom())
                .dateExoPrevue(entity.getDateExoPrevue())
                .dateDebutExoEffectif(entity.getDateDebutExoEffectif())
                .dateFinExoEffectif(entity.getDateFinExoEffectif())
                .build();

        // Mapper les relations
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

        List<MatiereSummaryDTO> matiereSummaryList = (entity.getExercise().getMatieres() != null) ?
                entity.getExercise().getMatieres().stream()
                        .map(matiere -> MatiereSummaryDTO.builder()
                                .id(matiere.getId())
                                .nom(matiere.getNom())
                                .description(matiere.getDescription())
                                .build())
                        .collect(Collectors.toList()) :
                new ArrayList<>();

        List<QuestionReponseSummaryDTO> questionSummaryList = (entity.getExercise().getQuestions() != null) ?
                entity.getExercise().getQuestions().stream()
                        .map(question -> QuestionReponseSummaryDTO.builder()
                                .id(question.getId())
                                .intitule(question.getIntitule())
                                .typeQuestion(question.getTypeQuestion())
                                .build())
                        .collect(Collectors.toList()) :
                new ArrayList<>();

        List<CoursSummaryDTO> coursSummaryList = (entity.getExercise().getCoursLies() != null) ?
                entity.getExercise().getCoursLies().stream()
                        .map(cours -> CoursSummaryDTO.builder()
                                .id(cours.getId())
                                .titre(cours.getTitre())
                                .description(cours.getDescription())
                                .build())
                        .collect(Collectors.toList()) :
                new ArrayList<>();

        List<ParticipationExerciseResponseDTO> participations = (entity.getParticipants() != null) ?
                entity.getParticipants().stream()
                        .map(this::mapParticipationToDTO)
                        .collect(Collectors.toList()) :
                new ArrayList<>();

        responseDTO.setClassesDiffusees(classesDiffusees);
        responseDTO.setMatieres(matiereSummaryList);
        responseDTO.setQuestions(questionSummaryList);
        responseDTO.setCoursLies(coursSummaryList);
        responseDTO.setParticipations(participations);

        return responseDTO;
    }

    public ExerciseProgrammerEntity toEntity(ExerciseProgrammer exerciseProgrammer) {
        return dozerMapperBean.map(exerciseProgrammer, ExerciseProgrammerEntity.class);
    }

    public ExerciseProgrammer toModel(ExerciseProgrammerEntity entity) {
        return dozerMapperBean.map(entity, ExerciseProgrammer.class);
    }
}