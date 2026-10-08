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
    @org.springframework.beans.factory.annotation.Autowired
    private cmr.notep.business.security.CurrentUserService currentUser;
    @org.springframework.beans.factory.annotation.Autowired
    private cmr.notep.business.security.AccessControlService accessControl;


    private final ExerciseProgrammerBusiness exerciseProgrammerBusiness;
    private final ExerciseProgrammerMapper exerciseProgrammerMapper;

    @Override
    public ExerciseProgrammerResponseDTO programmerExercise(ExerciseProgrammerRequestDTO requestDTO) {
        ExerciseProgrammerMapper.normaliserClasses(requestDTO);
        currentUser.requireAuthenticated();
        if (!currentUser.isAdmin()) {
            if (!currentUser.hasRole("PROFESSOR") && !currentUser.hasRole("TUTOR")) {
                throw cmr.notep.business.security.CurrentUserService.forbidden("Seuls les professeurs peuvent créer du contenu pédagogique.");
            }
            requestDTO.setProgrammeParId(currentUser.requireUserId());
        if (!accessControl.isTeacherOfExercise(requestDTO.getExerciseId(), currentUser.requireUserId()) && !accessControl.isExercisePublicOrAuthor(requestDTO.getExerciseId(), currentUser.requireUserId())) accessControl.requireExerciseAuthor(requestDTO.getExerciseId());
        if (!currentUser.isAdmin() && requestDTO.getClasseIds() != null) requestDTO.getClasseIds().forEach(accessControl::requireClassTeacher);
        }
        log.info("Programmation d'un nouvel exercice à partir de l'exercice ID: {}", requestDTO.getExerciseId());

        ExerciseProgrammer exerciseProgrammer = exerciseProgrammerMapper.toModel(requestDTO);
        return reponseCreation(exerciseProgrammerBusiness.programmer(exerciseProgrammer, false));
    }

    @Override
    public ExerciseProgrammerResponseDTO programmerEtDiffuserExercise(ExerciseProgrammerRequestDTO requestDTO) {
        ExerciseProgrammerMapper.normaliserClasses(requestDTO);
        currentUser.requireAuthenticated();
        if (!currentUser.isAdmin()) {
            if (!currentUser.hasRole("PROFESSOR") && !currentUser.hasRole("TUTOR")) {
                throw cmr.notep.business.security.CurrentUserService.forbidden("Seuls les professeurs peuvent créer du contenu pédagogique.");
            }
            requestDTO.setProgrammeParId(currentUser.requireUserId());
        if (!accessControl.isTeacherOfExercise(requestDTO.getExerciseId(), currentUser.requireUserId()) && !accessControl.isExercisePublicOrAuthor(requestDTO.getExerciseId(), currentUser.requireUserId())) accessControl.requireExerciseAuthor(requestDTO.getExerciseId());
        if (!currentUser.isAdmin() && requestDTO.getClasseIds() != null) requestDTO.getClasseIds().forEach(accessControl::requireClassTeacher);
        }
        log.info("Programmation et diffusion d'un exercice à partir de l'exercice ID: {}", requestDTO.getExerciseId());

        ExerciseProgrammer exerciseProgrammer = exerciseProgrammerMapper.toModel(requestDTO);
        return reponseCreation(exerciseProgrammerBusiness.programmer(exerciseProgrammer, true));
    }

    /**
     * Réponse des POST de programmation : la première programmation créée (forme historique) + la liste complète
     * dans {@code programmations} et leur nombre dans {@code nombreProgrammations}.
     */
    private ExerciseProgrammerResponseDTO reponseCreation(List<ExerciseProgrammer> crees) {
        List<ExerciseProgrammerResponseDTO> dtos = crees.stream()
                .map(c -> exerciseProgrammerMapper.toResponseDTO(
                        exerciseProgrammerBusiness.obtenirExerciseProgrammeEntityParId(c.getId())))
                .collect(Collectors.toList());
        ExerciseProgrammerResponseDTO premiere = exerciseProgrammerMapper.toResponseDTO(
                exerciseProgrammerBusiness.obtenirExerciseProgrammeEntityParId(crees.get(0).getId()));
        premiere.setProgrammations(dtos);
        premiere.setNombreProgrammations(dtos.size());
        return premiere;
    }

    @Override
    public ExerciseProgrammerResponseDTO diffuserExerciseDansClasse(String exerciseProgrammerId, String classeId) {
        accessControl.requireExerciseProgrammeTeacher(exerciseProgrammerId);
        accessControl.requireClassTeacher(classeId);
        log.info("Diffusion de l'exercice programmé {} dans la classe {}", exerciseProgrammerId, classeId);

        ExerciseProgrammerEntity entity = exerciseProgrammerBusiness.diffuserExerciseDansClasse(exerciseProgrammerId, classeId);
        return exerciseProgrammerMapper.toResponseDTO(entity);
    }

    @Override
    public ExerciseProgrammerResponseDTO retirerExerciseDeClasse(String exerciseProgrammerId, String classeId) {
        accessControl.requireExerciseProgrammeTeacher(exerciseProgrammerId);
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
        accessControl.requireClassMember(classeId);
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
    public ExerciseProgrammerResponseDTO mettreAJourEtatExerciseProgramme(String exerciseProgrammerId, EtatExercise nouvelEtat) {
        accessControl.requireExerciseProgrammeTeacher(exerciseProgrammerId);
        log.info("Mise à jour de l'état de l'exercice programmé {} vers {}", exerciseProgrammerId, nouvelEtat);

        ExerciseProgrammer updatedExercise = exerciseProgrammerBusiness.mettreAJourEtatExerciseProgramme(exerciseProgrammerId, nouvelEtat);
        ExerciseProgrammerEntity entity = exerciseProgrammerBusiness.obtenirExerciseProgrammeEntityParId(updatedExercise.getId());
        return exerciseProgrammerMapper.toResponseDTO(entity);
    }

    @Override
    public ExerciseProgrammerResponseDTO changerCoursExerciseProgramme(String exerciseProgrammerId,
                                                                       java.util.Map<String, String> body) {
        accessControl.requireExerciseProgrammeTeacher(exerciseProgrammerId);
        String coursId = body == null ? null : body.get("coursId");
        log.info("Rattachement de l'exercice programmé {} au cours {}", exerciseProgrammerId, coursId);
        ExerciseProgrammerEntity entity = exerciseProgrammerBusiness.changerCours(exerciseProgrammerId, coursId);
        return exerciseProgrammerMapper.toResponseDTO(entity);
    }

    @Override
    public ExerciseProgrammerResponseDTO obtenirExerciseProgrammeParId(String exerciseProgrammerId) {
        log.info("Récupération de l'exercice programmé: {}", exerciseProgrammerId);

        ExerciseProgrammerEntity entity = exerciseProgrammerBusiness.obtenirExerciseProgrammeEntityParId(exerciseProgrammerId);
        return exerciseProgrammerMapper.toResponseDTO(entity);
    }

    @Override
    public List<ExerciseProgrammerResponseDTO> obtenirExercisesProgrammesParExerciseId(String exerciseId) {
        log.info("Récupération des exercices programmés pour l'exercice source: {}", exerciseId);

        return exerciseProgrammerBusiness.obtenirExercisesProgrammesParExerciseId(exerciseId)
                .stream()
                .map(exercise -> {
                    ExerciseProgrammerEntity entity = exerciseProgrammerBusiness.obtenirExerciseProgrammeEntityParId(exercise.getId());
                    return exerciseProgrammerMapper.toResponseDTO(entity);
                })
                .collect(Collectors.toList());
    }

    @Override
    public void supprimerExerciseProgramme(String exerciseProgrammerId) {
        accessControl.requireExerciseProgrammeTeacher(exerciseProgrammerId);
        log.info("Suppression de l'exercice programmé: {}", exerciseProgrammerId);
        exerciseProgrammerBusiness.supprimerExerciseProgramme(exerciseProgrammerId);
    }
}