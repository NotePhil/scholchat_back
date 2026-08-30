package cmr.notep.business.business;

import cmr.notep.business.exceptions.SchoolException;
import cmr.notep.business.exceptions.enums.SchoolErrorCode;
import cmr.notep.business.services.MediaService;
import cmr.notep.interfaces.modeles.Media;
import cmr.notep.interfaces.modeles.QuestionReponse;
import cmr.notep.ressourcesjpa.commun.DaoAccessorService;
import cmr.notep.ressourcesjpa.dao.ChoixReponseEntity;
import cmr.notep.ressourcesjpa.dao.ExerciseEntity;
import cmr.notep.ressourcesjpa.dao.MediaEntity;
import cmr.notep.ressourcesjpa.dao.QuestionReponseEntity;
import cmr.notep.ressourcesjpa.repository.ExerciseRepository;
import cmr.notep.ressourcesjpa.repository.QuestionReponseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class QuestionReponseBusiness {

    private final DaoAccessorService daoAccessorService;
    private final MediaService mediaService;

    // A question attachment is either an image or a PDF document — no video, no arbitrary files.
    private static final Set<String> ALLOWED_QUESTION_MEDIA_CONTENT_TYPES = Set.of("application/pdf");

    private boolean isAllowedQuestionMediaContentType(String contentType) {
        if (contentType == null) return false;
        return contentType.startsWith("image/") || ALLOWED_QUESTION_MEDIA_CONTENT_TYPES.contains(contentType);
    }

    public QuestionReponse creerQuestion(String exerciseId, QuestionReponse question) {
        ExerciseEntity exercise = daoAccessorService.getRepository(ExerciseRepository.class)
                .findById(exerciseId)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Exercice introuvable"));

        QuestionReponseEntity entity = new QuestionReponseEntity();
        entity.setId(UUID.randomUUID().toString());
        entity.setIntitule(question.getIntitule());
        entity.setReponse(question.getReponse());
        entity.setTypeQuestion(question.getTypeQuestion());
        entity.setPoints(question.getPoints());
        entity.setExercise(exercise);

        // Gérer les choix de réponses pour les QCM
        if (question.getChoixReponses() != null && !question.getChoixReponses().isEmpty()) {
            for (cmr.notep.interfaces.dto.ChoixReponseDTO choixDTO : question.getChoixReponses()) {
                ChoixReponseEntity choix = new ChoixReponseEntity();
                choix.setId(UUID.randomUUID().toString());
                choix.setTexte(choixDTO.getTexte());
                choix.setEstCorrect(choixDTO.getEstCorrect());
                choix.setOrdreAffichage(choixDTO.getOrdreAffichage());
                choix.setQuestion(entity);
                entity.getChoixReponses().add(choix);
            }
        }

        // Gérer les médias (image ou PDF) joints à la question
        if (question.getMedias() != null && !question.getMedias().isEmpty()) {
            for (Media media : question.getMedias()) {
                MediaEntity mediaEntity = buildQuestionMediaEntity(media, entity, exercise.getRedacteur().getId());
                entity.getMedias().add(mediaEntity);
            }
        }

        QuestionReponseEntity savedEntity = daoAccessorService.getRepository(QuestionReponseRepository.class).save(entity);
        return embedPresignedUrls(mapToQuestionReponse(savedEntity));
    }

    /**
     * Builds a MediaEntity for a question attachment, validating that only images
     * or PDFs are accepted (videos/arbitrary files are rejected outright).
     */
    private MediaEntity buildQuestionMediaEntity(Media media, QuestionReponseEntity question, String ownerId) {
        String contentType = media.getContentType() != null ? media.getContentType() : "application/octet-stream";
        if (!isAllowedQuestionMediaContentType(contentType)) {
            throw new SchoolException(SchoolErrorCode.INVALID_INPUT,
                    "Seules les images et les fichiers PDF peuvent être joints à une question");
        }

        MediaEntity mediaEntity = new MediaEntity();
        mediaEntity.setId(UUID.randomUUID().toString());
        mediaEntity.setQuestion(question);
        mediaEntity.setBucketName(media.getBucketName() != null ? media.getBucketName() : "scholchat");
        mediaEntity.setContentType(contentType);
        mediaEntity.setMediaType(contentType.startsWith("image/") ? "IMAGE" : "DOCUMENT");

        String fileName = media.getFileName();
        if (fileName == null || fileName.trim().isEmpty()) {
            fileName = "question-file-" + System.currentTimeMillis();
        }
        mediaEntity.setFileName(fileName);
        mediaEntity.setFileSize(media.getFileSize() != null ? media.getFileSize() : 0L);

        String filePath = media.getFilePath();
        if (filePath == null || filePath.trim().isEmpty()) {
            filePath = "questions/" + fileName;
        }
        mediaEntity.setFilePath(filePath);
        mediaEntity.setOwnerId(ownerId);
        mediaEntity.setUploadedDate(LocalDateTime.now());
        return mediaEntity;
    }

    /** Embeds a presigned S3 URL in each media of the question — eliminates N download-url round trips. */
    private QuestionReponse embedPresignedUrls(QuestionReponse question) {
        if (question.getMedias() != null) {
            question.getMedias().forEach(media -> {
                if (media.getFilePath() != null) {
                    try {
                        media.setPresignedUrl(mediaService.generateDownloadPresignedUrl(media.getFilePath()));
                    } catch (Exception ex) {
                        log.warn("Could not generate presigned URL for question media {}: {}", media.getId(), ex.getMessage());
                    }
                }
            });
        }
        return question;
    }

    private QuestionReponse mapToQuestionReponse(QuestionReponseEntity entity) {
        QuestionReponse question = new QuestionReponse();
        question.setId(entity.getId());
        question.setIntitule(entity.getIntitule());
        question.setReponse(entity.getReponse());
        question.setTypeQuestion(entity.getTypeQuestion());
        question.setPoints(entity.getPoints());
        if (entity.getExercise() != null) {
            question.setExerciseId(entity.getExercise().getId());
        }

        if (entity.getChoixReponses() != null && !entity.getChoixReponses().isEmpty()) {
            question.setChoixReponses(entity.getChoixReponses().stream()
                .map(choix -> cmr.notep.interfaces.dto.ChoixReponseDTO.builder()
                    .id(choix.getId())
                    .texte(choix.getTexte())
                    .estCorrect(choix.getEstCorrect())
                    .ordreAffichage(choix.getOrdreAffichage())
                    .build())
                .collect(Collectors.toList()));
        }

        if (entity.getMedias() != null && !entity.getMedias().isEmpty()) {
            question.setMedias(entity.getMedias().stream()
                .map(m -> Media.builder()
                    .id(m.getId())
                    .fileName(m.getFileName())
                    .filePath(m.getFilePath())
                    .fileType(m.getFileType())
                    .fileSize(m.getFileSize())
                    .ownerId(m.getOwnerId())
                    .uploadedDate(m.getUploadedDate())
                    .mediaType(m.getMediaType())
                    .contentType(m.getContentType())
                    .bucketName(m.getBucketName())
                    .build())
                .collect(Collectors.toList()));
        }

        return question;
    }


    public List<QuestionReponse> obtenirQuestionsParExercise(String exerciseId) {
        List<QuestionReponseEntity> entities = daoAccessorService.getRepository(QuestionReponseRepository.class)
                .findByExerciseId(exerciseId);

        log.info("Nombre de questions trouvées: {}", entities.size());

        return entities.stream()
                .map(entity -> {
                    log.info("Question ID: {}, Nombre de choix: {}",
                        entity.getId(),
                        entity.getChoixReponses() != null ? entity.getChoixReponses().size() : 0);
                    // Embed presigned URLs directly in the response — the frontend never
                    // needs a follow-up /media/{id}/download-url round trip per attachment.
                    return embedPresignedUrls(mapToQuestionReponse(entity));
                })
                .collect(Collectors.toList());
    }

    public QuestionReponse mettreAJourQuestion(String questionId, QuestionReponse question) {
        QuestionReponseEntity existingEntity = daoAccessorService.getRepository(QuestionReponseRepository.class)
                .findById(questionId)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Question introuvable"));

        if (question.getIntitule() != null) {
            existingEntity.setIntitule(question.getIntitule());
        }
        if (question.getReponse() != null) {
            existingEntity.setReponse(question.getReponse());
        }
        if (question.getTypeQuestion() != null) {
            existingEntity.setTypeQuestion(question.getTypeQuestion());
        }
        if (question.getPoints() != null) {
            existingEntity.setPoints(question.getPoints());
        }
        if (question.getChoixReponses() != null) {
            existingEntity.getChoixReponses().clear();
            for (cmr.notep.interfaces.dto.ChoixReponseDTO choixDTO : question.getChoixReponses()) {
                ChoixReponseEntity choix = new ChoixReponseEntity();
                choix.setId(UUID.randomUUID().toString());
                choix.setTexte(choixDTO.getTexte());
                choix.setEstCorrect(choixDTO.getEstCorrect());
                choix.setOrdreAffichage(choixDTO.getOrdreAffichage());
                choix.setQuestion(existingEntity);
                existingEntity.getChoixReponses().add(choix);
            }
        }

        // Merge medias: keep existing (by id), add new ones, drop removed ones (orphanRemoval deletes them)
        if (question.getMedias() != null) {
            Set<String> keepIds = question.getMedias().stream()
                    .map(Media::getId)
                    .filter(mid -> mid != null && !mid.isBlank())
                    .collect(Collectors.toSet());
            existingEntity.getMedias().removeIf(me -> !keepIds.contains(me.getId()));

            String ownerId = existingEntity.getExercise() != null ? existingEntity.getExercise().getRedacteur().getId() : null;
            question.getMedias().stream()
                    .filter(media -> media.getId() == null || media.getId().isBlank())
                    .forEach(media -> existingEntity.getMedias().add(buildQuestionMediaEntity(media, existingEntity, ownerId)));
        }

        QuestionReponseEntity updatedEntity = daoAccessorService.getRepository(QuestionReponseRepository.class).save(existingEntity);
        return embedPresignedUrls(mapToQuestionReponse(updatedEntity));
    }

    public void supprimerQuestion(String questionId) {
        QuestionReponseEntity question = daoAccessorService.getRepository(QuestionReponseRepository.class)
                .findById(questionId)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Question introuvable"));

        // Clean up the underlying S3 objects before the cascade delete removes the media rows
        if (question.getMedias() != null) {
            question.getMedias().forEach(media -> {
                try {
                    mediaService.deleteMedia(media.getFilePath());
                } catch (Exception ex) {
                    log.warn("Could not delete S3 object for question media {}: {}", media.getId(), ex.getMessage());
                }
            });
        }

        daoAccessorService.getRepository(QuestionReponseRepository.class).delete(question);
    }
}