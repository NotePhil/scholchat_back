package cmr.notep.business.business;

import cmr.notep.business.exceptions.SchoolException;
import cmr.notep.business.exceptions.enums.SchoolErrorCode;
import cmr.notep.interfaces.modeles.Repondre;
import cmr.notep.ressourcesjpa.commun.DaoAccessorService;
import cmr.notep.ressourcesjpa.dao.QuestionReponseEntity;
import cmr.notep.ressourcesjpa.dao.RepondreEntity;
import cmr.notep.ressourcesjpa.dao.RepondreId;
import cmr.notep.ressourcesjpa.dao.UtilisateursEntity;
import cmr.notep.ressourcesjpa.repository.QuestionReponseRepository;
import cmr.notep.ressourcesjpa.repository.RepondreRepository;
import cmr.notep.ressourcesjpa.repository.UtilisateursRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static cmr.notep.business.config.BusinessConfig.dozerMapperBean;

@Component
@Slf4j
@RequiredArgsConstructor
public class RepondreBusiness {

    private final DaoAccessorService daoAccessorService;

    public Repondre repondreQuestion(Repondre repondre) {
        log.info("Enregistrement de la réponse pour l'utilisateur {} à la question {}",
                repondre.getUtilisateurId(), repondre.getQuestionId());

        // Vérifier que l'utilisateur existe
        UtilisateursEntity utilisateur = daoAccessorService.getRepository(UtilisateursRepository.class)
                .findById(repondre.getUtilisateurId())
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Utilisateur introuvable"));

        // Vérifier que la question existe
        QuestionReponseEntity question = daoAccessorService.getRepository(QuestionReponseRepository.class)
                .findById(repondre.getQuestionId())
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Question introuvable"));

        // Vérifier si l'utilisateur a déjà répondu à cette question
        RepondreId repondreId = new RepondreId(repondre.getUtilisateurId(), repondre.getQuestionId());
        boolean existeDeja = daoAccessorService.getRepository(RepondreRepository.class)
                .existsById(repondreId);

        if (existeDeja) {
            throw new SchoolException(SchoolErrorCode.DUPLICATE_RESOURCE,
                    "L'utilisateur a déjà répondu à cette question");
        }

        // Créer l'entité de réponse
        RepondreEntity entity = dozerMapperBean.map(repondre, RepondreEntity.class);
        entity.setId(repondreId);
        entity.setUtilisateur(utilisateur);
        entity.setQuestion(question);

        // Sauvegarder
        RepondreEntity savedEntity = daoAccessorService.getRepository(RepondreRepository.class).save(entity);

        log.info("Réponse enregistrée avec succès");
        return dozerMapperBean.map(savedEntity, Repondre.class);
    }

    public Repondre mettreAJourReponse(Repondre repondre) {
        log.info("Mise à jour de la réponse pour l'utilisateur {} à la question {}",
                repondre.getUtilisateurId(), repondre.getQuestionId());

        RepondreId repondreId = new RepondreId(repondre.getUtilisateurId(), repondre.getQuestionId());
        RepondreEntity existingEntity = daoAccessorService.getRepository(RepondreRepository.class)
                .findById(repondreId)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Réponse introuvable"));

        // Mettre à jour les champs
        if (repondre.getNote() != null) {
            existingEntity.setNote(repondre.getNote());
        }
        if (repondre.getAppreciation() != null) {
            existingEntity.setAppreciation(repondre.getAppreciation());
        }
        if (repondre.getReponseUtilisateur() != null) {
            existingEntity.setReponseUtilisateur(repondre.getReponseUtilisateur());
        }
        if (repondre.getEstCorrecte() != null) {
            existingEntity.setEstCorrecte(repondre.getEstCorrecte());
        }

        RepondreEntity updatedEntity = daoAccessorService.getRepository(RepondreRepository.class).save(existingEntity);

        log.info("Réponse mise à jour avec succès");
        return dozerMapperBean.map(updatedEntity, Repondre.class);
    }

    public List<Repondre> obtenirReponsesParUtilisateur(String utilisateurId) {
        return daoAccessorService.getRepository(RepondreRepository.class)
                .findByUtilisateurId(utilisateurId)
                .stream()
                .map(r -> dozerMapperBean.map(r, Repondre.class))
                .collect(Collectors.toList());
    }

    public List<Repondre> obtenirReponsesParQuestion(String questionId) {
        return daoAccessorService.getRepository(RepondreRepository.class)
                .findByQuestionId(questionId)
                .stream()
                .map(r -> dozerMapperBean.map(r, Repondre.class))
                .collect(Collectors.toList());
    }

    public List<Repondre> obtenirReponsesParExercise(String exerciseId) {
        return daoAccessorService.getRepository(RepondreRepository.class)
                .findByExerciseId(exerciseId)
                .stream()
                .map(r -> dozerMapperBean.map(r, Repondre.class))
                .collect(Collectors.toList());
    }

    public Repondre obtenirReponse(String utilisateurId, String questionId) {
        RepondreEntity entity = daoAccessorService.getRepository(RepondreRepository.class)
                .findByUtilisateurIdAndQuestionId(utilisateurId, questionId)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Réponse introuvable"));

        return dozerMapperBean.map(entity, Repondre.class);
    }

    public void supprimerReponse(String utilisateurId, String questionId) {
        RepondreId repondreId = new RepondreId(utilisateurId, questionId);
        if (!daoAccessorService.getRepository(RepondreRepository.class).existsById(repondreId)) {
            throw new SchoolException(SchoolErrorCode.NOT_FOUND, "Réponse introuvable");
        }

        daoAccessorService.getRepository(RepondreRepository.class).deleteById(repondreId);
        log.info("Réponse supprimée avec succès");
    }
}