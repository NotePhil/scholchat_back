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

    private final RepondreBusiness repondreBusiness;
    private final RepondreMapper repondreMapper;

    @Override
    public RepondreResponseDTO repondreQuestion(RepondreRequestDTO repondreRequestDTO) {
        log.info("Enregistrement d'une nouvelle réponse");
        Repondre repondre = repondreMapper.toModel(repondreRequestDTO);
        Repondre createdRepondre = repondreBusiness.repondreQuestion(repondre);
        return repondreMapper.toResponseDTO(
                dozerMapperBean.map(createdRepondre, cmr.notep.ressourcesjpa.dao.RepondreEntity.class)
        );
    }

    @Override
    public RepondreResponseDTO mettreAJourReponse(RepondreRequestDTO repondreRequestDTO) {
        log.info("Mise à jour d'une réponse");
        Repondre repondre = repondreMapper.toModel(repondreRequestDTO);
        Repondre updatedRepondre = repondreBusiness.mettreAJourReponse(repondre);
        return repondreMapper.toResponseDTO(
                dozerMapperBean.map(updatedRepondre, cmr.notep.ressourcesjpa.dao.RepondreEntity.class)
        );
    }

    @Override
    public List<RepondreResponseDTO> obtenirReponsesParUtilisateur(String utilisateurId) {
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
        log.info("Récupération de la réponse de l'utilisateur {} à la question {}", utilisateurId, questionId);
        Repondre repondre = repondreBusiness.obtenirReponse(utilisateurId, questionId);
        return repondreMapper.toResponseDTO(
                dozerMapperBean.map(repondre, cmr.notep.ressourcesjpa.dao.RepondreEntity.class)
        );
    }

    @Override
    public void supprimerReponse(String utilisateurId, String questionId) {
        log.info("Suppression de la réponse de l'utilisateur {} à la question {}", utilisateurId, questionId);
        repondreBusiness.supprimerReponse(utilisateurId, questionId);
    }
}