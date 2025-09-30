package cmr.notep.business.business.mappers;

import cmr.notep.interfaces.dto.RepondreRequestDTO;
import cmr.notep.interfaces.dto.RepondreResponseDTO;
import cmr.notep.interfaces.modeles.Repondre;
import cmr.notep.ressourcesjpa.dao.RepondreEntity;
import org.springframework.stereotype.Component;

@Component
public class RepondreMapper {

    public Repondre toModel(RepondreRequestDTO requestDTO) {
        return Repondre.builder()
                .utilisateurId(requestDTO.getUtilisateurId())
                .questionId(requestDTO.getQuestionId())
                .note(requestDTO.getNote())
                .appreciation(requestDTO.getAppreciation())
                .reponseUtilisateur(requestDTO.getReponseUtilisateur())
                .estCorrecte(requestDTO.getEstCorrecte())
                .build();
    }

    public RepondreResponseDTO toResponseDTO(RepondreEntity entity) {
        return RepondreResponseDTO.builder()
                .utilisateurId(entity.getUtilisateur().getId())
                .questionId(entity.getQuestion().getId())
                .note(entity.getNote())
                .appreciation(entity.getAppreciation())
                .reponseUtilisateur(entity.getReponseUtilisateur())
                .dateReponse(entity.getDateReponse())
                .estCorrecte(entity.getEstCorrecte())
                .utilisateurNom(entity.getUtilisateur().getNom())
                .utilisateurPrenom(entity.getUtilisateur().getPrenom())
                .questionIntitule(entity.getQuestion().getIntitule())
                .exerciseNom(entity.getQuestion().getExercise() != null ?
                        entity.getQuestion().getExercise().getNom() : null)
                .build();
    }

    public RepondreEntity toEntity(Repondre model) {
        RepondreEntity entity = new RepondreEntity();
        entity.setNote(model.getNote());
        entity.setAppreciation(model.getAppreciation());
        entity.setReponseUtilisateur(model.getReponseUtilisateur());
        entity.setEstCorrecte(model.getEstCorrecte());
        entity.setDateReponse(model.getDateReponse());
        return entity;
    }
}