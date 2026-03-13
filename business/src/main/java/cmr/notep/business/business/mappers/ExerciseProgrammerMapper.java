package cmr.notep.business.business.mappers;

import cmr.notep.interfaces.dto.*;
import cmr.notep.interfaces.modeles.ExerciseProgrammer;
import cmr.notep.modele.EtatExercise;
import cmr.notep.ressourcesjpa.dao.ExerciseEntity;
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
        exerciseProgrammer.setExerciseId(requestDTO.getExerciseId()); // Stocker l'ID de l'exercice source
        exerciseProgrammer.setProgrammeParId(requestDTO.getProgrammeParId());
        exerciseProgrammer.setDateExoPrevue(requestDTO.getDateExoPrevue());
        exerciseProgrammer.setDateDebutExoEffectif(requestDTO.getDateDebutExoEffectif());
        exerciseProgrammer.setDateFinExoEffectif(requestDTO.getDateFinExoEffectif());

        // Ensure etat is never null
        if (requestDTO.getEtat() != null) {
            exerciseProgrammer.setEtat(requestDTO.getEtat());
        } else {
            exerciseProgrammer.setEtat(EtatExercise.BROUILLON); // Default value
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
        ExerciseEntity source = entity.getExercise();

        ExerciseProgrammerResponseDTO responseDTO = ExerciseProgrammerResponseDTO.builder()
                .id(entity.getId())
                .nom(source.getNom())
                .description(source.getDescription())
                .dateCreation(source.getDateCreation())
                .etat(entity.getEtat())
                .restriction(source.getRestriction())
                .niveau(source.getNiveau())
                .redacteurId(source.getRedacteur().getId())
                .programmeParId(entity.getProgrammePar().getId())
                .programmeParNom(entity.getProgrammePar().getNom())
                .programmeParPrenom(entity.getProgrammePar().getPrenom())
                .dateExoPrevue(entity.getDateExoPrevue())
                .dateDebutExoEffectif(entity.getDateDebutExoEffectif())
                .dateFinExoEffectif(entity.getDateFinExoEffectif())
                .build();

        responseDTO.setClassesDiffusees(entity.getClassesDiffusees() != null ?
                entity.getClassesDiffusees().stream()
                        .map(classe -> ClasseSummaryDTO.builder()
                                .id(classe.getId())
                                .nom(classe.getNom())
                                .niveau(classe.getNiveau())
                                .codeActivation(classe.getCodeActivation())
                                .build())
                        .collect(Collectors.toList()) : new ArrayList<>());

        responseDTO.setMatieres(source.getMatieres() != null ?
                source.getMatieres().stream()
                        .map(matiere -> MatiereSummaryDTO.builder()
                                .id(matiere.getId())
                                .nom(matiere.getNom())
                                .description(matiere.getDescription())
                                .build())
                        .collect(Collectors.toList()) : new ArrayList<>());

        responseDTO.setQuestions(source.getQuestions() != null ?
                source.getQuestions().stream()
                        .map(question -> QuestionReponseSummaryDTO.builder()
                                .id(question.getId())
                                .intitule(question.getIntitule())
                                .typeQuestion(question.getTypeQuestion())
                                .build())
                        .collect(Collectors.toList()) : new ArrayList<>());

        responseDTO.setCoursLies(source.getCoursLies() != null ?
                source.getCoursLies().stream()
                        .map(cours -> CoursSummaryDTO.builder()
                                .id(cours.getId())
                                .titre(cours.getTitre())
                                .description(cours.getDescription())
                                .build())
                        .collect(Collectors.toList()) : new ArrayList<>());

        responseDTO.setParticipations(entity.getParticipants() != null ?
                entity.getParticipants().stream()
                        .map(this::mapParticipationToDTO)
                        .collect(Collectors.toList()) : new ArrayList<>());

        return responseDTO;
    }

    public ExerciseProgrammerEntity toEntity(ExerciseProgrammer exerciseProgrammer) {
        return dozerMapperBean.map(exerciseProgrammer, ExerciseProgrammerEntity.class);
    }

    public ExerciseProgrammer toModel(ExerciseProgrammerEntity entity) {
        return dozerMapperBean.map(entity, ExerciseProgrammer.class);
    }
}