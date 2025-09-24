package cmr.notep.business.impl;

import cmr.notep.business.business.ExerciseBusiness;

import cmr.notep.business.business.mappers.ExerciseMapper;
import cmr.notep.interfaces.api.ExerciseApi;
import cmr.notep.interfaces.dto.ExerciseRequestDTO;
import cmr.notep.interfaces.dto.ExerciseResponseDTO;
import cmr.notep.interfaces.modeles.Exercise;
import cmr.notep.modele.ListeNiveau;
import cmr.notep.ressourcesjpa.dao.ExerciseEntity;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequiredArgsConstructor
public class ExerciseService implements ExerciseApi {

    private final ExerciseBusiness exerciseBusiness;
    private final ExerciseMapper exerciseMapper;

    @Override
    public ExerciseResponseDTO creerExercise(@NonNull ExerciseRequestDTO exerciseRequestDTO) {
        log.info("Création d'un nouvel exercice: {}", exerciseRequestDTO.getNom());
        Exercise exercise = exerciseMapper.toEntity(exerciseRequestDTO);
        Exercise createdExercise = exerciseBusiness.creerExercise(exercise);
        ExerciseEntity exerciseEntity = exerciseBusiness.obtenirExerciseEntityParId(createdExercise.getId());
        return exerciseMapper.toResponseDTO(exerciseEntity);
    }

    @Override
    public ExerciseResponseDTO mettreAJourExercise(@NonNull String exerciseId, @NonNull ExerciseRequestDTO exerciseRequestDTO) {
        log.info("Mise à jour de l'exercice: {}", exerciseId);
        Exercise exercise = exerciseMapper.toEntity(exerciseRequestDTO);
        Exercise updatedExercise = exerciseBusiness.mettreAJourExercise(exerciseId, exercise);
        ExerciseEntity exerciseEntity = exerciseBusiness.obtenirExerciseEntityParId(updatedExercise.getId());
        return exerciseMapper.toResponseDTO(exerciseEntity);
    }

    @Override
    public void supprimerExercise(@NonNull String exerciseId) {
        log.info("Suppression de l'exercice: {}", exerciseId);
        exerciseBusiness.supprimerExercise(exerciseId);
    }

    @Override
    public ExerciseResponseDTO obtenirExerciseParId(@NonNull String exerciseId) {
        log.info("Récupération de l'exercice: {}", exerciseId);
        ExerciseEntity exerciseEntity = exerciseBusiness.obtenirExerciseEntityParId(exerciseId);
        return exerciseMapper.toResponseDTO(exerciseEntity);
    }

    @Override
    public List<ExerciseResponseDTO> obtenirExercisesParProfesseur(@NonNull String professeurId) {
        log.info("Récupération des exercices du professeur: {}", professeurId);
        return exerciseBusiness.obtenirExercisesEntityParProfesseur(professeurId).stream()
                .map(exerciseMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ExerciseResponseDTO> obtenirExercisesParNiveau(@NonNull ListeNiveau niveau) {
        log.info("Récupération des exercices de niveau: {}", niveau);
        return exerciseBusiness.obtenirExercisesEntityParNiveau(niveau).stream()
                .map(exerciseMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ExerciseResponseDTO> obtenirExercisesAccessibles(@NonNull String userId) {
        log.info("Récupération des exercices accessibles pour l'utilisateur: {}", userId);
        return exerciseBusiness.obtenirExercisesEntityAccessibles(userId).stream()
                .map(exerciseMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ExerciseResponseDTO> obtenirExercisesParCours(@NonNull String coursId) {
        log.info("Récupération des exercices liés au cours: {}", coursId);
        return exerciseBusiness.obtenirExercisesEntityParCours(coursId).stream()
                .map(exerciseMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ExerciseResponseDTO lierExerciseACours(@NonNull String exerciseId, @NonNull String coursId) {
        log.info("Liaison de l'exercice {} au cours {}", exerciseId, coursId);
        ExerciseEntity exerciseEntity = exerciseBusiness.lierExerciseACours(exerciseId, coursId);
        return exerciseMapper.toResponseDTO(exerciseEntity);
    }

    @Override
    public ExerciseResponseDTO delierExerciseDeCours(@NonNull String exerciseId, @NonNull String coursId) {
        log.info("Déliaison de l'exercice {} du cours {}", exerciseId, coursId);
        ExerciseEntity exerciseEntity = exerciseBusiness.delierExerciseDeCours(exerciseId, coursId);
        return exerciseMapper.toResponseDTO(exerciseEntity);
    }
}
