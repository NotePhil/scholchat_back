package cmr.notep.business.business;

import cmr.notep.business.exceptions.SchoolException;
import cmr.notep.business.exceptions.enums.SchoolErrorCode;
import cmr.notep.business.services.MediaService;
import cmr.notep.ressourcesjpa.dao.MediaEntity;
import cmr.notep.ressourcesjpa.repository.MediaRepository;
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

    @Transactional
    public MediaEntity saveMediaMetadata(String fileName, String filePath,
                                         String contentType, String mediaType, String ownerId) {
        // Only proceed if we have a valid owner ID
        if (ownerId == null || ownerId.isEmpty()) {
            // Skip database save and return a transient entity with the metadata
            MediaEntity transientMedia = new MediaEntity();
            transientMedia.setId(UUID.randomUUID().toString()); // Generate ID for reference
            transientMedia.setFileName(fileName);
            transientMedia.setFilePath(filePath);
            transientMedia.setContentType(contentType);
            transientMedia.setMediaType(mediaType);
            transientMedia.setUploadedDate(LocalDateTime.now());
            transientMedia.setBucketName(mediaService.getDefaultBucketName());
            return transientMedia;
        }

        // Normal flow for authenticated users
        MediaEntity media = new MediaEntity();
        media.setFileName(fileName);
        media.setFilePath(filePath);
        media.setContentType(contentType);
        media.setMediaType(mediaType);
        media.setOwnerId(ownerId);
        media.setUploadedDate(LocalDateTime.now());
        media.setBucketName(mediaService.getDefaultBucketName());

        return mediaRepository.save(media);
    }

    public String generateUploadUrl(String fileName, String contentType,
                                    String mediaType, String ownerId, String documentType) {
        String sanitizedFileName = fileName.replaceAll("\\s+", "_")
                .replaceAll("[^a-zA-Z0-9._-]", "");

        // Build file path based on media type and document type
        String filePath;
        if (documentType != null && !documentType.isEmpty()) {
            filePath = String.format("%s/%s/%s/%s",
                    ownerId,
                    mediaType,
                    documentType,
                    sanitizedFileName);
        } else {
            filePath = String.format("%s/%s/%s",
                    ownerId,
                    mediaType,
                    sanitizedFileName);
        }

        // Save metadata (will be handled differently based on ownerId)
        saveMediaMetadata(fileName, filePath, contentType, mediaType, ownerId);

        // Generate the upload URL regardless of whether we saved to database
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
}