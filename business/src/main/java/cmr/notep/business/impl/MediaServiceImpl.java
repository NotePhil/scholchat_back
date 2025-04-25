package cmr.notep.business.impl;

import cmr.notep.business.services.MediaService;
import cmr.notep.business.business.MediaBusiness;
import cmr.notep.ressourcesjpa.dao.MediaEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/media")
@Slf4j
public class MediaServiceImpl {

    private final MediaBusiness mediaBusiness;
    private final MediaService mediaService;

    @Autowired
    public MediaServiceImpl(MediaBusiness mediaBusiness, MediaService mediaService) {
        this.mediaBusiness = mediaBusiness;
        this.mediaService = mediaService;
    }

    @PostMapping("/presigned-url")
    public ResponseEntity<Map<String, String>> generateUploadUrl(
            @RequestBody PresignedUrlRequest request) {
        log.debug("Generating upload URL for file: {}", request.getFileName());

        // Sanitize the filename by replacing spaces with underscores
        String sanitizedFileName = request.getFileName().replaceAll("\\s+", "_");

        // Create the file path based on mediaType and sanitized fileName
        String filePath = request.getMediaType() + "/" + request.getOwnerId() + "/" + sanitizedFileName;

        // Use the MediaService interface to generate the upload URL
        String presignedUrl = mediaService.generateUploadPresignedUrl(filePath, request.getContentType());

        // Update media metadata in the business layer
        mediaBusiness.saveMediaMetadata(
                sanitizedFileName,  // Save the sanitized filename
                filePath,
                request.getContentType(),
                request.getMediaType(),
                request.getOwnerId()
        );

        return ResponseEntity.ok(Map.of(
                "url", presignedUrl,
                "mediaType", request.getMediaType()
        ));
    }

    @GetMapping("/{mediaId}/download-url")
    public ResponseEntity<Map<String, String>> generateDownloadUrl(
            @PathVariable String mediaId) {
        log.debug("Generating download URL for media ID: {}", mediaId);

        // Get the media entity to retrieve the file path
        MediaEntity media = mediaBusiness.getMediaById(mediaId);

        // Use the MediaService interface to generate the download URL
        String presignedUrl = mediaService.generateDownloadPresignedUrl(media.getFilePath());

        return ResponseEntity.ok(Map.of(
                "url", presignedUrl
        ));
    }

    @DeleteMapping("/{mediaId}")
    public ResponseEntity<Void> deleteMedia(@PathVariable String mediaId) {
        log.debug("Deleting media with ID: {}", mediaId);

        // Get the media entity to retrieve the file path
        MediaEntity media = mediaBusiness.getMediaById(mediaId);

        // Delete from storage
        mediaService.deleteMedia(media.getFilePath());

        // Delete metadata from database
        mediaBusiness.deleteMedia(mediaId);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<List<MediaEntity>> getMediaByOwnerId(@PathVariable String ownerId) {
        log.debug("Fetching media for owner ID: {}", ownerId);

        List<MediaEntity> mediaList = mediaBusiness.getMediaByOwnerId(ownerId);

        return ResponseEntity.ok(mediaList);
    }

    @GetMapping("/type/{mediaType}")
    public ResponseEntity<List<MediaEntity>> getMediaByType(@PathVariable String mediaType) {
        log.debug("Fetching media of type: {}", mediaType);

        List<MediaEntity> mediaList = mediaBusiness.getMediaByType(mediaType);

        return ResponseEntity.ok(mediaList);
    }

    @GetMapping("/{mediaId}")
    public ResponseEntity<MediaEntity> getMediaById(@PathVariable String mediaId) {
        log.debug("Fetching media with ID: {}", mediaId);

        MediaEntity media = mediaBusiness.getMediaById(mediaId);

        return ResponseEntity.ok(media);
    }

    @PatchMapping("/{mediaId}")
    public ResponseEntity<MediaEntity> updateMediaMetadata(
            @PathVariable String mediaId,
            @RequestBody MediaUpdateRequest request) {
        log.debug("Updating metadata for media ID: {}", mediaId);

        MediaEntity updatedMedia = mediaBusiness.updateMediaMetadata(
                mediaId,
                request.getFileSize(),
                request.getMediaType()
        );

        return ResponseEntity.ok(updatedMedia);
    }

    @GetMapping("/exists")
    public ResponseEntity<Map<String, Boolean>> checkIfObjectExists(@RequestParam String filePath) {
        log.debug("Checking if file exists: {}", filePath);

        // Sanitize the file path by replacing spaces with underscores
        String sanitizedFilePath = filePath.replaceAll("\\s+", "_");

        boolean exists = mediaService.doesObjectExist(sanitizedFilePath);

        return ResponseEntity.ok(Map.of("exists", exists));
    }

    @GetMapping("/bucket")
    public ResponseEntity<Map<String, String>> getDefaultBucketName() {
        log.debug("Getting default bucket name");

        String bucketName = mediaService.getDefaultBucketName();

        return ResponseEntity.ok(Map.of("bucketName", bucketName));
    }

    // Request DTOs
    public static class PresignedUrlRequest {
        private String fileName;
        private String contentType;
        private String mediaType;
        private String ownerId;

        // Getters and setters
        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public String getContentType() {
            return contentType;
        }

        public void setContentType(String contentType) {
            this.contentType = contentType;
        }

        public String getMediaType() {
            return mediaType;
        }

        public void setMediaType(String mediaType) {
            this.mediaType = mediaType;
        }

        public String getOwnerId() {
            return ownerId;
        }

        public void setOwnerId(String ownerId) {
            this.ownerId = ownerId;
        }
    }

    public static class MediaUpdateRequest {
        private Long fileSize;
        private String mediaType;

        // Getters and setters
        public Long getFileSize() {
            return fileSize;
        }

        public void setFileSize(Long fileSize) {
            this.fileSize = fileSize;
        }

        public String getMediaType() {
            return mediaType;
        }

        public void setMediaType(String mediaType) {
            this.mediaType = mediaType;
        }
    }
}