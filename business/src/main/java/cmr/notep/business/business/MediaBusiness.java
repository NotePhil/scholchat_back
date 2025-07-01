package cmr.notep.business.business;

import cmr.notep.business.exceptions.SchoolException;
import cmr.notep.business.exceptions.enums.SchoolErrorCode;
import cmr.notep.business.services.MediaService;
import cmr.notep.ressourcesjpa.dao.MediaEntity;
import cmr.notep.ressourcesjpa.dao.UtilisateursEntity;
import cmr.notep.ressourcesjpa.repository.MediaRepository;
import cmr.notep.ressourcesjpa.repository.UtilisateursRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class MediaBusiness {

    private final MediaService mediaService;
    private final MediaRepository mediaRepository;
    private final UtilisateursRepository utilisateursRepository;

    @Transactional
    public MediaEntity saveMediaMetadata(String fileName, String filePath,
                                         String contentType, String mediaType, String ownerId) {
        // Verify user exists and create folder if needed
        if (ownerId != null && !ownerId.equals("temp")) {
            validateUserExists(ownerId);
            createUserMediaFolder(ownerId, mediaType);
        }

        MediaEntity media = new MediaEntity();
        media.setId(UUID.randomUUID().toString());
        media.setFileName(fileName);
        media.setFilePath(filePath);
        media.setContentType(contentType);
        media.setMediaType(mediaType);
        media.setUploadedDate(LocalDateTime.now());
        media.setBucketName(mediaService.getDefaultBucketName());

        if (ownerId != null && !ownerId.isEmpty() && !ownerId.equals("temp")) {
            media.setOwnerId(ownerId);
        }

        return mediaRepository.save(media);
    }

    public String generateUploadUrl(String fileName, String contentType,
                                    String mediaType, String ownerId, String documentType) {
        String sanitizedFileName = sanitizeFileName(fileName);
        String filePath = buildUserMediaPath(ownerId, mediaType, documentType, sanitizedFileName);

        ensureUserMediaFoldersExist(ownerId, mediaType, documentType);
        saveMediaMetadata(fileName, filePath, contentType, mediaType, ownerId);

        return mediaService.generateUploadPresignedUrl(filePath, contentType);
    }

    public String generateDownloadUrl(String mediaId) {
        MediaEntity media = getMediaById(mediaId);
        return mediaService.generateDownloadPresignedUrl(media.getFilePath());
    }

    @Transactional
    public void deleteMedia(String mediaId) {
        MediaEntity media = getMediaById(mediaId);
        mediaService.deleteMedia(media.getFilePath());
        mediaRepository.delete(media);
    }

    public MediaEntity getMediaById(String mediaId) {
        return mediaRepository.findById(mediaId)
                .orElseThrow(() -> new SchoolException(
                        SchoolErrorCode.RESOURCE_NOT_FOUND,
                        "Media not found with ID: " + mediaId));
    }

    public List<MediaEntity> getMediaByOwnerId(String ownerId) {
        if (ownerId == null || ownerId.isEmpty()) {
            return List.of();
        }
        return mediaRepository.findByOwnerId(ownerId);
    }

    public List<MediaEntity> getMediaByType(String mediaType) {
        return mediaRepository.findByMediaType(mediaType);
    }

    @Transactional
    public MediaEntity updateMediaMetadata(String mediaId, Long fileSize, String mediaType) {
        MediaEntity media = getMediaById(mediaId);
        if (fileSize != null) media.setFileSize(fileSize);
        if (mediaType != null) media.setMediaType(mediaType);
        return mediaRepository.save(media);
    }

    public boolean mediaExistsByPath(String filePath) {
        return mediaRepository.findByFilePath(filePath).isPresent();
    }

    @Transactional
    public void updateMediaOwner(String mediaId, String newOwnerId) {
        MediaEntity media = getMediaById(mediaId);
        validateUserExists(newOwnerId);

        String oldPath = media.getFilePath();
        String newPath = oldPath.replaceFirst(
                getUserFolderPath(media.getOwnerId()),
                getUserFolderPath(newOwnerId)
        );

        mediaService.moveMedia(oldPath, newPath);
        media.setOwnerId(newOwnerId);
        media.setFilePath(newPath);
        mediaRepository.save(media);
    }

    // Helper methods
    private void validateUserExists(String ownerId) {
        if (ownerId == null || ownerId.equals("temp")) return;

        utilisateursRepository.findById(ownerId)
                .orElseThrow(() -> new SchoolException(
                        SchoolErrorCode.RESOURCE_NOT_FOUND,
                        "User not found with ID: " + ownerId));
    }

    private void createUserMediaFolder(String ownerId, String mediaType) {
        String userFolder = getUserFolderPath(ownerId);
        String mediaFolder = userFolder + "/" + mediaType.toLowerCase();
        mediaService.ensureFolderExists(userFolder);
        mediaService.ensureFolderExists(mediaFolder);
    }

    private String getUserFolderPath(String ownerId) {
        if (ownerId == null || ownerId.equals("temp")) {
            return "temp";
        }
        return "users/" + ownerId; // Using actual user ID instead of hash
    }

    private String sanitizeFileName(String fileName) {
        return fileName.replaceAll("\\s+", "_")
                .replaceAll("[^a-zA-Z0-9._-]", "");
    }

    private String buildUserMediaPath(String ownerId, String mediaType,
                                      String documentType, String fileName) {
        return String.format("%s/%s/%s/%s",
                getUserFolderPath(ownerId),
                mediaType.toLowerCase(),
                documentType.toLowerCase(),
                fileName);
    }

    private void ensureUserMediaFoldersExist(String ownerId, String mediaType, String documentType) {
        String basePath = getUserFolderPath(ownerId);
        String mediaPath = basePath + "/" + mediaType.toLowerCase();
        String documentPath = mediaPath + "/" + documentType.toLowerCase();

        mediaService.ensureFolderExists(basePath);
        mediaService.ensureFolderExists(mediaPath);
        mediaService.ensureFolderExists(documentPath);
    }
}