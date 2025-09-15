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
import java.util.Optional;
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
        try {
            // Validate owner exists if provided (except for temp)
            UtilisateursEntity owner = null;
            if (ownerId != null && !ownerId.equals("temp")) {
                owner = utilisateursRepository.findById(ownerId)
                        .orElseThrow(() -> new SchoolException(
                                SchoolErrorCode.RESOURCE_NOT_FOUND,
                                "User not found with ID: " + ownerId));

                // Create user-specific folder structure
                String userFolderPath = "users/" + ownerId;
                mediaService.ensureFolderExists(userFolderPath);
                mediaService.ensureFolderExists(userFolderPath + "/" + mediaType.toLowerCase());
            }

            // Create media entity
            MediaEntity media = new MediaEntity();
            media.setId(UUID.randomUUID().toString());
            media.setFileName(fileName);
            media.setFilePath(filePath);
            media.setContentType(contentType);
            media.setMediaType(mediaType);
            media.setUploadedDate(LocalDateTime.now());
            media.setBucketName(mediaService.getDefaultBucketName());
            media.setOwnerId(ownerId);

            return mediaRepository.save(media);
        } catch (Exception e) {
            log.error("Error saving media metadata for file: {}", fileName, e);
            throw new SchoolException(SchoolErrorCode.INTERNAL_ERROR, "Failed to save media metadata");
        }
    }

    public String generateUploadUrl(String fileName, String contentType,
                                    String mediaType, String ownerId, String documentType) {
        try {
            String sanitizedFileName = sanitizeFileName(fileName);
            String filePath = buildUserMediaPath(ownerId, mediaType, documentType, sanitizedFileName);

            // Ensure folders exist (including user folder if not temp)
            ensureUserMediaFoldersExist(ownerId, mediaType, documentType);

            // Save metadata first
            saveMediaMetadata(fileName, filePath, contentType, mediaType, ownerId);

            return mediaService.generateUploadPresignedUrl(filePath, contentType);
        } catch (Exception e) {
            log.error("Error generating upload URL for file: {}", fileName, e);
            throw new SchoolException(SchoolErrorCode.INTERNAL_ERROR, "Failed to generate upload URL");
        }
    }

    public String generateDownloadUrl(String identifier) {
        try {
            // First try to find by ID
            Optional<MediaEntity> mediaOpt = mediaRepository.findById(identifier);
            if (mediaOpt.isPresent()) {
                return mediaService.generateDownloadPresignedUrl(mediaOpt.get().getFilePath());
            }

            // If not found by ID, try to find by file path
            mediaOpt = mediaRepository.findByFilePath(identifier);
            if (mediaOpt.isPresent()) {
                return mediaService.generateDownloadPresignedUrl(mediaOpt.get().getFilePath());
            }

            // If not found by exact path, try to find by filename
            String fileName = extractFileNameFromPath(identifier);
            mediaOpt = mediaRepository.findByFileName(fileName);
            if (mediaOpt.isPresent()) {
                return mediaService.generateDownloadPresignedUrl(mediaOpt.get().getFilePath());
            }

            throw new SchoolException(SchoolErrorCode.RESOURCE_NOT_FOUND,
                    "Media not found with identifier: " + identifier);
        } catch (SchoolException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error generating download URL for identifier: {}", identifier, e);
            throw new SchoolException(SchoolErrorCode.INTERNAL_ERROR, "Failed to generate download URL");
        }
    }
    // Helper method to extract filename from path
    private String extractFileNameFromPath(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return "";
        }
        // Handle both forward and backward slashes
        int lastSeparator = Math.max(
                filePath.lastIndexOf('/'),
                filePath.lastIndexOf('\\')
        );
        return lastSeparator >= 0 ? filePath.substring(lastSeparator + 1) : filePath;
    }

    @Transactional
    public void deleteMedia(String mediaId) {
        try {
            MediaEntity media = getMediaById(mediaId);
            mediaService.deleteMedia(media.getFilePath());
            mediaRepository.delete(media);
            log.info("Deleted media with ID: {}", mediaId);
        } catch (Exception e) {
            log.error("Error deleting media with ID: {}", mediaId, e);
            throw new SchoolException(SchoolErrorCode.INTERNAL_ERROR, "Failed to delete media");
        }
    }

    public MediaEntity getMediaById(String mediaId) {
        return mediaRepository.findById(mediaId)
                .orElseThrow(() -> new SchoolException(
                        SchoolErrorCode.RESOURCE_NOT_FOUND,
                        "Media not found with ID: " + mediaId));
    }

    public List<MediaEntity> getMediaByOwnerId(String ownerId) {
        try {
            if (ownerId == null || ownerId.isEmpty()) {
                return List.of();
            }
            return mediaRepository.findByOwnerId(ownerId);
        } catch (Exception e) {
            log.error("Error getting media for owner: {}", ownerId, e);
            throw new SchoolException(SchoolErrorCode.INTERNAL_ERROR, "Failed to get media by owner");
        }
    }

    public List<MediaEntity> getMediaByType(String mediaType) {
        try {
            return mediaRepository.findByMediaType(mediaType);
        } catch (Exception e) {
            log.error("Error getting media by type: {}", mediaType, e);
            throw new SchoolException(SchoolErrorCode.INTERNAL_ERROR, "Failed to get media by type");
        }
    }

    @Transactional
    public MediaEntity updateMediaMetadata(String mediaId, Long fileSize, String mediaType) {
        try {
            MediaEntity media = getMediaById(mediaId);
            if (fileSize != null) {
                media.setFileSize(fileSize);
            }
            if (mediaType != null) {
                media.setMediaType(mediaType);
            }
            return mediaRepository.save(media);
        } catch (Exception e) {
            log.error("Error updating media metadata for ID: {}", mediaId, e);
            throw new SchoolException(SchoolErrorCode.INTERNAL_ERROR, "Failed to update media metadata");
        }
    }

    public boolean mediaExistsByPath(String filePath) {
        try {
            return mediaRepository.findByFilePath(filePath).isPresent();
        } catch (Exception e) {
            log.error("Error checking media existence for path: {}", filePath, e);
            throw new SchoolException(SchoolErrorCode.INTERNAL_ERROR, "Failed to check media existence");
        }
    }

    @Transactional
    public void updateMediaOwner(String mediaId, String newOwnerId) {
        try {
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

            log.info("Updated media owner from {} to {} for media ID: {}",
                    media.getOwnerId(), newOwnerId, mediaId);
        } catch (Exception e) {
            log.error("Error updating media owner for ID: {}", mediaId, e);
            throw new SchoolException(SchoolErrorCode.INTERNAL_ERROR, "Failed to update media owner");
        }
    }

    // Helper methods
    private void validateUserExists(String ownerId) {
        if (ownerId == null || ownerId.equals("temp")) {
            return;
        }

        utilisateursRepository.findById(ownerId)
                .orElseThrow(() -> new SchoolException(
                        SchoolErrorCode.RESOURCE_NOT_FOUND,
                        "User not found with ID: " + ownerId));
    }

    private String getUserFolderPath(String ownerId) {
        if (ownerId == null || ownerId.equals("temp")) {
            return "temp";
        }
        return "users/" + ownerId;  // Keep the "users/" prefix for folder structure
    }

    private String sanitizeFileName(String fileName) {
        return fileName.replaceAll("\\s+", "_")
                .replaceAll("[^a-zA-Z0-9._-]", "");
    }

    private String buildUserMediaPath(String ownerId, String mediaType,
                                      String documentType, String fileName) {
        return String.format("users/%s/%s/%s/%s",  // Add "users/" prefix here
                ownerId,
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