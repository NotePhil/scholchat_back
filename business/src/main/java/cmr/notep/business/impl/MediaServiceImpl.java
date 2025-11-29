package cmr.notep.business.impl;

import cmr.notep.business.services.MediaService;
import cmr.notep.business.business.MediaBusiness;
import cmr.notep.interfaces.dto.MediaDto;
import cmr.notep.ressourcesjpa.dao.MediaEntity;
import cmr.notep.ressourcesjpa.repository.MediaRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/media")
@Slf4j
public class MediaServiceImpl {

    private final MediaBusiness mediaBusiness;
    private final MediaService mediaService;
    private final MediaRepository mediaRepository;

    @Autowired
    public MediaServiceImpl(MediaBusiness mediaBusiness, MediaService mediaService, MediaRepository mediaRepository) {
        this.mediaBusiness = mediaBusiness;
        this.mediaService = mediaService;
        this.mediaRepository = mediaRepository;
    }

    @PostMapping("/presigned-url")
    public ResponseEntity<Map<String, String>> generateUploadUrl(
            @RequestBody PresignedUrlRequest request) {
        log.info("=== GENERATING UPLOAD URL ====");
        log.info("Request: fileName={}, contentType={}, mediaType={}, ownerId={}, documentType={}", 
                request.getFileName(), request.getContentType(), request.getMediaType(), 
                request.getOwnerId(), request.getDocumentType());

        try {
            // Validate required fields
            if (request.getFileName() == null || request.getFileName().trim().isEmpty()) {
                throw new IllegalArgumentException("File name is required");
            }
            if (request.getOwnerId() == null || request.getOwnerId().trim().isEmpty()) {
                throw new IllegalArgumentException("Owner ID is required");
            }
            
            String sanitizedFileName = request.getFileName()
                    .replaceAll("\\s+", "_")
                    .replaceAll("[^a-zA-Z0-9._-]", "");

            log.info("Sanitized filename: {}", sanitizedFileName);

            // Clean up any existing duplicates before creating new upload
            mediaBusiness.cleanupDuplicateMedia(sanitizedFileName);

            String presignedUrl = mediaBusiness.generateUploadUrl(
                    sanitizedFileName,
                    request.getContentType(),
                    request.getMediaType(),
                    request.getOwnerId(),
                    request.getDocumentType());

            log.info("Generated presigned URL: {}", presignedUrl);

            Map<String, String> response = new HashMap<>();
            response.put("url", presignedUrl);
            response.put("fileName", sanitizedFileName);
            response.put("mediaType", request.getMediaType());
            response.put("documentType", request.getDocumentType());
            response.put("ownerId", request.getOwnerId());

            log.info("Response: {}", response);
            log.info("=== UPLOAD URL GENERATION SUCCESS ====");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("=== UPLOAD URL GENERATION FAILED ====");
            log.error("Error generating upload URL for file: {}", request.getFileName(), e);
            log.error("Error details: {}", e.getMessage());
            log.error("========================================");
            throw e;
        }
    }

    @GetMapping("/{mediaId}/download-url")
    public ResponseEntity<Map<String, String>> generateDownloadUrl(
            @PathVariable String mediaId) {
        MediaEntity media = mediaBusiness.getMediaById(mediaId);
        String presignedUrl = mediaService.generateDownloadPresignedUrl(media.getFilePath());

        return ResponseEntity.ok(Map.of(
                "url", presignedUrl,
                "fileName", media.getFileName(),
                "contentType", media.getContentType(),
                "ownerId", media.getOwnerId()
        ));
    }

    @GetMapping("/{mediaId}/download")
    public ResponseEntity<Map<String, String>> generateDirectDownloadUrl(
            @PathVariable String mediaId) {
        return generateDownloadUrl(mediaId);
    }

    @GetMapping("/download-by-path")
    public ResponseEntity<Map<String, String>> generateDownloadUrlByPath(
            @RequestParam String filePath) {
        log.info("=== GENERATING DOWNLOAD URL BY PATH ====");
        log.info("File path: {}", filePath);

        try {
            String presignedUrl = mediaBusiness.generateDownloadUrl(filePath);
            log.info("Generated download URL: {}", presignedUrl);

            // Try to get media metadata for additional info using the new method
            Optional<MediaEntity> mediaOpt = mediaRepository.findByFilePathWithOwner(filePath);
            if (!mediaOpt.isPresent()) {
                // Fallback to original method
                mediaOpt = mediaRepository.findByFilePath(filePath);
            }

            if (mediaOpt.isPresent()) {
                MediaEntity media = mediaOpt.get();
                Map<String, String> response = Map.of(
                        "url", presignedUrl,
                        "fileName", media.getFileName(),
                        "contentType", media.getContentType(),
                        "ownerId", media.getOwnerId()
                );
                log.info("Response with metadata: {}", response);
                return ResponseEntity.ok(response);
            } else {
                // If no metadata found, extract filename from path
                String fileName = extractFileNameFromPath(filePath);
                Map<String, String> response = Map.of(
                        "url", presignedUrl,
                        "fileName", fileName,
                        "contentType", "application/octet-stream",
                        "ownerId", ""
                );
                log.info("Response without metadata: {}", response);
                return ResponseEntity.ok(response);
            }
        } catch (Exception e) {
            log.error("=== DOWNLOAD URL GENERATION FAILED ====");
            log.error("Failed to generate download URL by path: {}", filePath, e);
            log.error("Error details: {}", e.getMessage());
            log.error("===========================================");
            throw e;
        }
    }

    @GetMapping("/{mediaId}")
    public ResponseEntity<MediaDto> getMediaById(@PathVariable String mediaId) {
        MediaEntity media = mediaBusiness.getMediaById(mediaId);
        MediaDto mediaDto = convertToDto(media);
        return ResponseEntity.ok(mediaDto);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<MediaDto>> getMediaByUserId(@PathVariable String userId) {
        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }

        List<MediaEntity> mediaList = mediaBusiness.getMediaByOwnerId(userId);
        List<MediaDto> mediaDtoList = mediaList.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(mediaDtoList);
    }

    @DeleteMapping("/{mediaId}")
    public ResponseEntity<Void> deleteMedia(@PathVariable String mediaId) {
        mediaBusiness.deleteMedia(mediaId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{mediaId}")
    public ResponseEntity<MediaDto> updateMedia(
            @PathVariable String mediaId,
            @RequestBody MediaUpdateRequest request) {
        MediaEntity updatedMedia = mediaBusiness.updateMediaMetadata(
                mediaId,
                request.getFileSize(),
                request.getMediaType()
        );
        return ResponseEntity.ok(convertToDto(updatedMedia));
    }

    @PostMapping("/{mediaId}/transfer-ownership")
    public ResponseEntity<MediaDto> transferOwnership(
            @PathVariable String mediaId,
            @RequestParam String newOwnerId) {
        mediaBusiness.updateMediaOwner(mediaId, newOwnerId);
        MediaEntity media = mediaBusiness.getMediaById(mediaId);
        return ResponseEntity.ok(convertToDto(media));
    }

    // New endpoint to clean up duplicates
    @PostMapping("/cleanup-duplicates/{fileName}")
    public ResponseEntity<Map<String, Object>> cleanupDuplicates(@PathVariable String fileName) {
        try {
            mediaBusiness.cleanupDuplicateMedia(fileName);
            return ResponseEntity.ok(Map.of(
                    "message", "Duplicate cleanup initiated for file: " + fileName,
                    "status", "success"
            ));
        } catch (Exception e) {
            log.error("Failed to clean up duplicates for file: {}", fileName, e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "message", "Failed to clean up duplicates: " + e.getMessage(),
                    "status", "error"
            ));
        }
    }

    private String extractFileNameFromPath(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return "unknown";
        }
        int lastSeparator = Math.max(
                filePath.lastIndexOf('/'),
                filePath.lastIndexOf('\\')
        );
        return lastSeparator >= 0 ? filePath.substring(lastSeparator + 1) : filePath;
    }

    private MediaDto convertToDto(MediaEntity entity) {
        MediaDto dto = new MediaDto();
        dto.setId(entity.getId());
        dto.setFileName(entity.getFileName());
        dto.setFilePath(entity.getFilePath());
        dto.setFileType(entity.getFileType());
        dto.setFileSize(entity.getFileSize());
        dto.setOwnerId(entity.getOwnerId());
        dto.setUploadedDate(entity.getUploadedDate());
        dto.setMediaType(entity.getMediaType());
        dto.setContentType(entity.getContentType());
        dto.setBucketName(entity.getBucketName());
        return dto;
    }

    public static class PresignedUrlRequest {
        private String fileName;
        private String contentType;
        private String mediaType;
        private String ownerId;
        private String documentType;

        // Getters and setters
        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }
        public String getContentType() { return contentType; }
        public void setContentType(String contentType) { this.contentType = contentType; }
        public String getMediaType() { return mediaType; }
        public void setMediaType(String mediaType) { this.mediaType = mediaType; }
        public String getOwnerId() { return ownerId; }
        public void setOwnerId(String ownerId) { this.ownerId = ownerId; }
        public String getDocumentType() { return documentType; }
        public void setDocumentType(String documentType) { this.documentType = documentType; }
    }

    public static class MediaUpdateRequest {
        private Long fileSize;
        private String mediaType;

        // Getters and setters
        public Long getFileSize() { return fileSize; }
        public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
        public String getMediaType() { return mediaType; }
        public void setMediaType(String mediaType) { this.mediaType = mediaType; }
    }
}