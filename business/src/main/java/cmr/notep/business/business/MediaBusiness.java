package cmr.notep.business.business;

import cmr.notep.business.exceptions.SchoolException;
import cmr.notep.business.exceptions.enums.SchoolErrorCode;
import cmr.notep.business.services.MediaService;
import cmr.notep.ressourcesjpa.dao.MediaEntity;
import cmr.notep.ressourcesjpa.repository.MediaRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
@Slf4j
public class MediaBusiness {

    private final MediaService mediaService;
    private final MediaRepository mediaRepository;

    @Autowired
    public MediaBusiness(MediaService mediaService, MediaRepository mediaRepository) {
        this.mediaService = mediaService;
        this.mediaRepository = mediaRepository;
    }

    /**
     * Save media metadata to the database
     *
     * @param fileName    The name of the file
     * @param filePath    The path of the file in storage
     * @param contentType The content type of the file
     * @param mediaType   The type of media (IMAGE, VIDEO, etc.)
     * @param ownerId     The ID of the owner
     * @return The created media entity
     */
    @Transactional
    public MediaEntity saveMediaMetadata(String fileName, String filePath, String contentType, String mediaType, String ownerId) {
        log.debug("Saving media metadata for file: {}, mediaType: {}, owner: {}", fileName, mediaType, ownerId);

        // Create and save the media entity
        MediaEntity mediaEntity = new MediaEntity();
        mediaEntity.setFileName(fileName);
        mediaEntity.setFilePath(filePath);
        mediaEntity.setContentType(contentType);
        mediaEntity.setMediaType(mediaType);
        mediaEntity.setOwnerId(ownerId);
        mediaEntity.setUploadedDate(LocalDateTime.now());
        mediaEntity.setBucketName(mediaService.getDefaultBucketName());

        mediaRepository.save(mediaEntity);
        log.debug("Media entity created with ID: {}", mediaEntity.getId());

        return mediaEntity;
    }

    /**
     * Generate a presigned URL for uploading a file and create a media record
     *
     * @param fileName    The name of the file
     * @param contentType The content type of the file
     * @param mediaType   The type of media (IMAGE, VIDEO, etc.)
     * @param ownerId     The ID of the owner
     * @return Presigned URL for uploading
     */
    @Transactional
    public String generateUploadUrl(String fileName, String contentType, String mediaType, String ownerId) {
        log.debug("Generating upload URL for file: {}, mediaType: {}, owner: {}", fileName, mediaType, ownerId);

        // Generate a unique path for this file
        String uniqueId = UUID.randomUUID().toString();
        String filePath = ownerId + "/" + mediaType.toLowerCase() + "/" + uniqueId + "/" + fileName;

        // Create and save the media entity using the saveMediaMetadata method
        saveMediaMetadata(fileName, filePath, contentType, mediaType, ownerId);

        // Generate the upload URL
        String presignedUrl = mediaService.generateUploadPresignedUrl(filePath, contentType);
        return presignedUrl;
    }

    /**
     * Generate a presigned URL for downloading/viewing a file
     *
     * @param mediaId The ID of the media
     * @return Presigned URL for downloading/viewing
     */
    public String generateDownloadUrl(String mediaId) {
        MediaEntity mediaEntity = mediaRepository.findById(mediaId)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.RESOURCE_NOT_FOUND, "Media not found with ID: " + mediaId));

        return mediaService.generateDownloadPresignedUrl(mediaEntity.getFilePath());
    }

    /**
     * Delete a media file and its metadata
     *
     * @param mediaId The ID of the media to delete
     */
    @Transactional
    public void deleteMedia(String mediaId) {
        MediaEntity mediaEntity = mediaRepository.findById(mediaId)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.RESOURCE_NOT_FOUND, "Media not found with ID: " + mediaId));

        // Delete from storage
        mediaService.deleteMedia(mediaEntity.getFilePath());

        // Delete from database
        mediaRepository.delete(mediaEntity);
        log.info("Media deleted: {}", mediaId);
    }

    /**
     * Get all media for a specific owner
     *
     * @param ownerId The ID of the owner
     * @return List of media entities
     */
    public List<MediaEntity> getMediaByOwnerId(String ownerId) {
        return mediaRepository.findByOwnerId(ownerId);
    }

    /**
     * Get all media of a specific type
     *
     * @param mediaType The type of media (IMAGE, VIDEO, etc.)
     * @return List of media entities
     */
    public List<MediaEntity> getMediaByType(String mediaType) {
        return mediaRepository.findByMediaType(mediaType);
    }

    /**
     * Check if a media file exists
     *
     * @param mediaId The ID of the media
     * @return true if the media exists, false otherwise
     */
    public boolean doesMediaExist(String mediaId) {
        return mediaRepository.existsById(mediaId);
    }

    /**
     * Update media metadata
     *
     * @param mediaId   The ID of the media
     * @param fileSize  The size of the file
     * @param mediaType The type of media (optional)
     * @return The updated media entity
     */
    @Transactional
    public MediaEntity updateMediaMetadata(String mediaId, Long fileSize, String mediaType) {
        MediaEntity mediaEntity = mediaRepository.findById(mediaId)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.RESOURCE_NOT_FOUND, "Media not found with ID: " + mediaId));

        if (fileSize != null) {
            mediaEntity.setFileSize(fileSize);
        }

        if (mediaType != null && !mediaType.isEmpty()) {
            mediaEntity.setMediaType(mediaType);
        }

        return mediaRepository.save(mediaEntity);
    }

    /**
     * Get a media entity by ID
     *
     * @param mediaId The ID of the media
     * @return The media entity
     */
    public MediaEntity getMediaById(String mediaId) {
        return mediaRepository.findById(mediaId)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.RESOURCE_NOT_FOUND, "Media not found with ID: " + mediaId));
    }
}