package cmr.notep.business.impl;

import cmr.notep.business.business.RepondreBusiness;
import cmr.notep.business.business.mappers.RepondreMapper;
import cmr.notep.interfaces.api.RepondreApi;
import cmr.notep.interfaces.dto.RepondreRequestDTO;
import cmr.notep.interfaces.dto.RepondreResponseDTO;
import cmr.notep.interfaces.modeles.Repondre;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

import static cmr.notep.business.config.BusinessConfig.dozerMapperBean;

@RestController
@Slf4j
@RequiredArgsConstructor
public class RepondreService implements RepondreApi {
    @org.springframework.beans.factory.annotation.Autowired
    private cmr.notep.business.security.CurrentUserService currentUser;
    @org.springframework.beans.factory.annotation.Autowired
    private cmr.notep.business.security.AccessControlService accessControl;


    private final RepondreBusiness repondreBusiness;
    private final RepondreMapper repondreMapper;

    @Override
    public RepondreResponseDTO repondreQuestion(RepondreRequestDTO repondreRequestDTO) {
        if (!accessControl.canActForMinorChild(repondreRequestDTO.getUtilisateurId())) {
            currentUser.requireSelfOrAdmin(repondreRequestDTO.getUtilisateurId());
        }
        log.info("Enregistrement d'une nouvelle réponse");
        Repondre repondre = repondreMapper.toModel(repondreRequestDTO);
        Repondre createdRepondre = repondreBusiness.repondreQuestion(repondre);
        return repondreMapper.toResponseDTO(
                dozerMapperBean.map(createdRepondre, cmr.notep.ressourcesjpa.dao.RepondreEntity.class)
        );
    }

    @Override
    public RepondreResponseDTO mettreAJourReponse(RepondreRequestDTO repondreRequestDTO) {
        currentUser.requireAuthenticated();
        if (!currentUser.isAdmin()) {
            boolean notation = repondreRequestDTO.getNote() != null || repondreRequestDTO.getAppreciation() != null;
            boolean pourSoi = currentUser.isSelf(repondreRequestDTO.getUtilisateurId())
                    || accessControl.canActForMinorChild(repondreRequestDTO.getUtilisateurId());
            if (notation || !pourSoi) {
                // corriger (note / appréciation) ou modifier la réponse d'un autre : professeur responsable uniquement
                accessControl.requireTeacherOfQuestion(repondreRequestDTO.getQuestionId());
            }
        }
        log.info("Mise à jour d'une réponse");
        Repondre repondre = repondreMapper.toModel(repondreRequestDTO);
        Repondre updatedRepondre = repondreBusiness.mettreAJourReponse(repondre);
        return repondreMapper.toResponseDTO(
                dozerMapperBean.map(updatedRepondre, cmr.notep.ressourcesjpa.dao.RepondreEntity.class)
        );
    }

    @Override
    public List<RepondreResponseDTO> obtenirReponsesParUtilisateur(String utilisateurId) {
        accessControl.requireSelfParentOrTeacher(utilisateurId);
        log.info("Récupération des réponses de l'utilisateur: {}", utilisateurId);
        return repondreBusiness.obtenirReponsesParUtilisateur(utilisateurId)
                .stream()
                .map(r -> repondreMapper.toResponseDTO(
                        dozerMapperBean.map(r, cmr.notep.ressourcesjpa.dao.RepondreEntity.class)
                ))
                .collect(Collectors.toList());
    }

    @Override
    public List<RepondreResponseDTO> obtenirReponsesParQuestion(String questionId) {
        accessControl.requireTeacherOfQuestion(questionId);
        log.info("Récupération des réponses pour la question: {}", questionId);
        return repondreBusiness.obtenirReponsesParQuestion(questionId)
                .stream()
                .map(r -> repondreMapper.toResponseDTO(
                        dozerMapperBean.map(r, cmr.notep.ressourcesjpa.dao.RepondreEntity.class)
                ))
                .collect(Collectors.toList());
    }

    @Override
    public List<RepondreResponseDTO> obtenirReponsesParExercise(String exerciseId) {
        currentUser.requireAuthenticated();
        if (!currentUser.isAdmin() && !accessControl.isTeacherOfExercise(exerciseId, currentUser.requireUserId())) throw cmr.notep.business.security.CurrentUserService.forbidden("Seul le professeur responsable de cet exercice peut consulter les copies.");
        log.info("Récupération des réponses pour l'exercice: {}", exerciseId);
        return repondreBusiness.obtenirReponsesParExercise(exerciseId)
                .stream()
                .map(r -> repondreMapper.toResponseDTO(
                        dozerMapperBean.map(r, cmr.notep.ressourcesjpa.dao.RepondreEntity.class)
                ))
                .collect(Collectors.toList());
    }

    @Override
    public RepondreResponseDTO obtenirReponse(String utilisateurId, String questionId) {
        if (!currentUser.isSelf(utilisateurId)) { if (!currentUser.isAdmin() && !accessControl.isParentOf(currentUser.requireUserId(), utilisateurId)) accessControl.requireTeacherOfQuestion(questionId); }
        log.info("Récupération de la réponse de l'utilisateur {} à la question {}", utilisateurId, questionId);
        Repondre repondre = repondreBusiness.obtenirReponse(utilisateurId, questionId);
        return repondreMapper.toResponseDTO(
                dozerMapperBean.map(repondre, cmr.notep.ressourcesjpa.dao.RepondreEntity.class)
        );
    }

    @Override
    public void supprimerReponse(String utilisateurId, String questionId) {
        if (!currentUser.isSelf(utilisateurId)) accessControl.requireTeacherOfQuestion(questionId);
        log.info("Suppression de la réponse de l'utilisateur {} à la question {}", utilisateurId, questionId);
        repondreBusiness.supprimerReponse(utilisateurId, questionId);
    }
}