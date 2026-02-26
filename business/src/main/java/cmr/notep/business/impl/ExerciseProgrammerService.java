package cmr.notep.business.impl;

import cmr.notep.business.business.ExerciseProgrammerBusiness;
import cmr.notep.business.business.mappers.ExerciseProgrammerMapper;
import cmr.notep.interfaces.api.ExerciseProgrammerApi;
import cmr.notep.interfaces.dto.ExerciseProgrammerRequestDTO;
import cmr.notep.interfaces.dto.ExerciseProgrammerResponseDTO;
import cmr.notep.interfaces.modeles.ExerciseProgrammer;
import cmr.notep.modele.EtatExercise;
import cmr.notep.ressourcesjpa.dao.ExerciseProgrammerEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequiredArgsConstructor
public class ExerciseProgrammerService implements ExerciseProgrammerApi {

    private final ExerciseProgrammerBusiness exerciseProgrammerBusiness;
    private final ExerciseProgrammerMapper exerciseProgrammerMapper;

    @Override
    public ExerciseProgrammerResponseDTO programmerExercise(ExerciseProgrammerRequestDTO requestDTO) {
        log.info("Programmation d'un nouvel exercice à partir de l'exercice ID: {}", requestDTO.getExerciseId());

        ExerciseProgrammer exerciseProgrammer = exerciseProgrammerMapper.toModel(requestDTO);
        ExerciseProgrammer createdExercise = exerciseProgrammerBusiness.programmerExercise(exerciseProgrammer);

        ExerciseProgrammerEntity entity = exerciseProgrammerBusiness.obtenirExerciseProgrammeEntityParId(createdExercise.getId());
        return exerciseProgrammerMapper.toResponseDTO(entity);
    }

    @Override
    public ExerciseProgrammerResponseDTO programmerEtDiffuserExercise(ExerciseProgrammerRequestDTO requestDTO) {
        log.info("Programmation et diffusion d'un exercice à partir de l'exercice ID: {}", requestDTO.getExerciseId());

        ExerciseProgrammer exerciseProgrammer = exerciseProgrammerMapper.toModel(requestDTO);
        ExerciseProgrammer createdExercise = exerciseProgrammerBusiness.programmerEtDiffuserExercise(exerciseProgrammer);

        ExerciseProgrammerEntity entity = exerciseProgrammerBusiness.obtenirExerciseProgrammeEntityParId(createdExercise.getId());
        return exerciseProgrammerMapper.toResponseDTO(entity);
    }

    @Override
    public ExerciseProgrammerResponseDTO diffuserExerciseDansClasse(String exerciseProgrammerId, String classeId) {
        log.info("Diffusion de l'exercice programmé {} dans la classe {}", exerciseProgrammerId, classeId);

        ExerciseProgrammerEntity entity = exerciseProgrammerBusiness.diffuserExerciseDansClasse(exerciseProgrammerId, classeId);
        return exerciseProgrammerMapper.toResponseDTO(entity);
    }

    @Override
    public ExerciseProgrammerResponseDTO retirerExerciseDeClasse(String exerciseProgrammerId, String classeId) {
        log.info("Retrait de l'exercice programmé {} de la classe {}", exerciseProgrammerId, classeId);

        ExerciseProgrammerEntity entity = exerciseProgrammerBusiness.retirerExerciseDeClasse(exerciseProgrammerId, classeId);
        return exerciseProgrammerMapper.toResponseDTO(entity);
    }

    @Override
    public List<ExerciseProgrammerResponseDTO> obtenirExercisesProgrammesParProfesseur(String professeurId) {
        log.info("Récupération des exercices programmés par le professeur: {}", professeurId);

        return exerciseProgrammerBusiness.obtenirExercisesProgrammesParProfesseur(professeurId)
                .stream()
                .map(exercise -> {
                    ExerciseProgrammerEntity entity = exerciseProgrammerBusiness.obtenirExerciseProgrammeEntityParId(exercise.getId());
                    return exerciseProgrammerMapper.toResponseDTO(entity);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ExerciseProgrammerResponseDTO> obtenirExercisesProgrammesParClasse(String classeId) {
        log.info("Récupération des exercices programmés pour la classe: {}", classeId);

        return exerciseProgrammerBusiness.obtenirExercisesProgrammesParClasse(classeId)
                .stream()
                .map(exercise -> {
                    ExerciseProgrammerEntity entity = exerciseProgrammerBusiness.obtenirExerciseProgrammeEntityParId(exercise.getId());
                    return exerciseProgrammerMapper.toResponseDTO(entity);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ExerciseProgrammerResponseDTO> obtenirExercisesProgrammesParExercise(String exerciseId) {
        log.info("Récupération des programmations pour l'exercice: {}", exerciseId);

        return exerciseProgrammerBusiness.obtenirExercisesProgrammesParExercise(exerciseId)
                .stream()
                .map(exercise -> {
                    ExerciseProgrammerEntity entity = exerciseProgrammerBusiness.obtenirExerciseProgrammeEntityParId(exercise.getId());
                    return exerciseProgrammerMapper.toResponseDTO(entity);
                })
                .collect(Collectors.toList());
    }

    @Override
    public ExerciseProgrammerResponseDTO mettreAJourEtatExerciseProgramme(String exerciseProgrammerId, EtatExercise nouvelEtat) {
        log.info("Mise à jour de l'état de l'exercice programmé {} vers {}", exerciseProgrammerId, nouvelEtat);

        ExerciseProgrammer updatedExercise = exerciseProgrammerBusiness.mettreAJourEtatExerciseProgramme(exerciseProgrammerId, nouvelEtat);
        ExerciseProgrammerEntity entity = exerciseProgrammerBusiness.obtenirExerciseProgrammeEntityParId(updatedExercise.getId());
        return exerciseProgrammerMapper.toResponseDTO(entity);
    }

    @Override
    public ExerciseProgrammerResponseDTO obtenirExerciseProgrammeParId(String exerciseProgrammerId) {
        log.info("Récupération de l'exercice programmé: {}", exerciseProgrammerId);

        ExerciseProgrammerEntity entity = exerciseProgrammerBusiness.obtenirExerciseProgrammeEntityParId(exerciseProgrammerId);
        return exerciseProgrammerMapper.toResponseDTO(entity);
    }

    @Override
    public void supprimerExerciseProgramme(String exerciseProgrammerId) {
        log.info("Suppression de l'exercice programmé: {}", exerciseProgrammerId);
        exerciseProgrammerBusiness.supprimerExerciseProgramme(exerciseProgrammerId);
    }
}