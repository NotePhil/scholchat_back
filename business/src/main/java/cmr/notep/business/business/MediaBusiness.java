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
            UtilisateursEntity owner = null;
            if (ownerId != null && !ownerId.equals("temp")) {
                owner = utilisateursRepository.findById(ownerId)
                        .orElseThrow(() -> new SchoolException(
                                SchoolErrorCode.RESOURCE_NOT_FOUND,
                                "User not found with ID: " + ownerId));

                String userFolderPath = "users/" + ownerId;
                mediaService.ensureFolderExists(userFolderPath);
                mediaService.ensureFolderExists(userFolderPath + "/" + mediaType.toLowerCase());
            }

            // Check if media with same file path already exists
            Optional<MediaEntity> existingMedia = mediaRepository.findByFilePath(filePath);
            if (existingMedia.isPresent()) {
                log.warn("Media with file path {} already exists. Updating metadata.", filePath);
                MediaEntity media = existingMedia.get();
                media.setFileName(fileName);
                media.setContentType(contentType);
                media.setMediaType(mediaType);
                media.setUploadedDate(LocalDateTime.now());
                media.setOwnerId(ownerId);

                MediaEntity savedMedia = mediaRepository.save(media);
                log.debug("Media entity updated successfully with ID: {}", savedMedia.getId());
                return savedMedia;
            }

            MediaEntity media = new MediaEntity();
            String mediaId = UUID.randomUUID().toString();
            media.setId(mediaId);
            media.setFileName(fileName);
            media.setFilePath(filePath);
            media.setContentType(contentType);
            media.setMediaType(mediaType);
            media.setUploadedDate(LocalDateTime.now());
            media.setBucketName(mediaService.getDefaultBucketName());
            media.setOwnerId(ownerId);

            log.debug("Saving media entity with ID: {} and filePath: {}", mediaId, filePath);
            MediaEntity savedMedia = mediaRepository.save(media);
            log.debug("Media entity saved successfully with ID: {}", savedMedia.getId());

            return savedMedia;
        } catch (Exception e) {
            log.error("Error saving media metadata for file: {} - {}", fileName, e.getMessage(), e);
            throw new SchoolException(SchoolErrorCode.INTERNAL_ERROR,
                    "Failed to save media metadata: " + e.getMessage());
        }
    }

    public String generateUploadUrl(String fileName, String contentType,
                                    String mediaType, String ownerId, String documentType) {
        try {
            String sanitizedFileName = sanitizeFileName(fileName);
            String filePath = buildUserMediaPath(ownerId, mediaType, documentType, sanitizedFileName);

            ensureUserMediaFoldersExist(ownerId, mediaType, documentType);

            MediaEntity savedMedia = saveMediaMetadata(fileName, filePath, contentType, mediaType, ownerId);
            log.debug("Media metadata saved with ID: {}", savedMedia.getId());

            return mediaService.generateUploadPresignedUrl(filePath, contentType);
        } catch (Exception e) {
            log.error("Error generating upload URL for file: {} - {}", fileName, e.getMessage(), e);
            throw new SchoolException(SchoolErrorCode.INTERNAL_ERROR,
                    "Failed to generate upload URL: " + e.getMessage());
        }
    }

    public String generateDownloadUrl(String identifier) {
        try {
            // First try by ID
            Optional<MediaEntity> mediaOpt = mediaRepository.findById(identifier);
            if (mediaOpt.isPresent()) {
                return mediaService.generateDownloadPresignedUrl(mediaOpt.get().getFilePath());
            }

            // Then try by file path with owner validation
            mediaOpt = mediaRepository.findByFilePathWithOwner(identifier);
            if (mediaOpt.isPresent()) {
                return mediaService.generateDownloadPresignedUrl(mediaOpt.get().getFilePath());
            }

            // Then try by file name with latest result
            mediaOpt = mediaRepository.findLatestByFileName(identifier);
            if (mediaOpt.isPresent()) {
                return mediaService.generateDownloadPresignedUrl(mediaOpt.get().getFilePath());
            }

            // If all else fails, try the original findByFilePath (may return multiple)
            mediaOpt = mediaRepository.findByFilePath(identifier);
            if (mediaOpt.isPresent()) {
                return mediaService.generateDownloadPresignedUrl(mediaOpt.get().getFilePath());
            }

            throw new SchoolException(SchoolErrorCode.RESOURCE_NOT_FOUND,
                    "Media not found with identifier: " + identifier);
        } catch (SchoolException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error generating download URL for identifier: {} - {}", identifier, e.getMessage(), e);
            throw new SchoolException(SchoolErrorCode.INTERNAL_ERROR,
                    "Failed to generate download URL: " + e.getMessage());
        }
    }

    private String extractFileNameFromPath(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return "";
        }
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
            log.error("Error deleting media with ID: {} - {}", mediaId, e.getMessage(), e);
            throw new SchoolException(SchoolErrorCode.INTERNAL_ERROR,
                    "Failed to delete media: " + e.getMessage());
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
            log.error("Error getting media for owner: {} - {}", ownerId, e.getMessage(), e);
            throw new SchoolException(SchoolErrorCode.INTERNAL_ERROR,
                    "Failed to get media by owner: " + e.getMessage());
        }
    }

    public List<MediaEntity> getMediaByType(String mediaType) {
        try {
            return mediaRepository.findByMediaType(mediaType);
        } catch (Exception e) {
            log.error("Error getting media by type: {} - {}", mediaType, e.getMessage(), e);
            throw new SchoolException(SchoolErrorCode.INTERNAL_ERROR,
                    "Failed to get media by type: " + e.getMessage());
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
            log.error("Error updating media metadata for ID: {} - {}", mediaId, e.getMessage(), e);
            throw new SchoolException(SchoolErrorCode.INTERNAL_ERROR,
                    "Failed to update media metadata: " + e.getMessage());
        }
    }

    public boolean mediaExistsByPath(String filePath) {
        try {
            return mediaRepository.findByFilePath(filePath).isPresent();
        } catch (Exception e) {
            log.error("Error checking media existence for path: {} - {}", filePath, e.getMessage(), e);
            throw new SchoolException(SchoolErrorCode.INTERNAL_ERROR,
                    "Failed to check media existence: " + e.getMessage());
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
            log.error("Error updating media owner for ID: {} - {}", mediaId, e.getMessage(), e);
            throw new SchoolException(SchoolErrorCode.INTERNAL_ERROR,
                    "Failed to update media owner: " + e.getMessage());
        }
    }

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
        return "users/" + ownerId;
    }

    private String sanitizeFileName(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return "unnamed_file_" + System.currentTimeMillis();
        }
        return fileName.replaceAll("\\s+", "_")
                .replaceAll("[^a-zA-Z0-9._-]", "");
    }

    private String buildUserMediaPath(String ownerId, String mediaType,
                                      String documentType, String fileName) {
        return String.format("users/%s/%s/%s/%s",
                ownerId,
                mediaType.toLowerCase(),
                documentType.toLowerCase(),
                fileName);
    }

    private void ensureUserMediaFoldersExist(String ownerId, String mediaType, String documentType) {
        try {
            String basePath = getUserFolderPath(ownerId);
            String mediaPath = basePath + "/" + mediaType.toLowerCase();
            String documentPath = mediaPath + "/" + documentType.toLowerCase();

            mediaService.ensureFolderExists(basePath);
            mediaService.ensureFolderExists(mediaPath);
            mediaService.ensureFolderExists(documentPath);
        } catch (Exception e) {
            log.error("Error ensuring folders exist for owner: {} - {}", ownerId, e.getMessage(), e);
            throw new SchoolException(SchoolErrorCode.INTERNAL_ERROR,
                    "Failed to create folder structure: " + e.getMessage());
        }
    }

    // New method to handle duplicate file names
    @Transactional
    public void cleanupDuplicateMedia(String fileName) {
        try {
            List<MediaEntity> duplicates = mediaRepository.findByFileName(fileName);
            if (duplicates.size() > 1) {
                log.warn("Found {} duplicate media entries for file: {}", duplicates.size(), fileName);

                // Keep the latest one and delete others
                MediaEntity latest = duplicates.stream()
                        .max((m1, m2) -> m2.getUploadedDate().compareTo(m1.getUploadedDate()))
                        .orElse(null);

                if (latest != null) {
                    for (MediaEntity media : duplicates) {
                        if (!media.getId().equals(latest.getId())) {
                            try {
                                mediaService.deleteMedia(media.getFilePath());
                                mediaRepository.delete(media);
                                log.info("Deleted duplicate media: {}", media.getId());
                            } catch (Exception e) {
                                log.error("Failed to delete duplicate media: {}", media.getId(), e);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error cleaning up duplicate media for file: {}", fileName, e);
        }
    }
}