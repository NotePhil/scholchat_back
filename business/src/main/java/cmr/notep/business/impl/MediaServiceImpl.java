package cmr.notep.business.impl;

import cmr.notep.business.services.MediaService;
import cmr.notep.business.business.MediaBusiness;
import cmr.notep.interfaces.dto.MediaDto;
import cmr.notep.ressourcesjpa.dao.MediaEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

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

        try {
            // Sanitize filename
            String sanitizedFileName = request.getFileName()
                    .replaceAll("\\s+", "_")
                    .replaceAll("[^a-zA-Z0-9._-]", "");

            // Generate URL and save metadata
            String presignedUrl = mediaBusiness.generateUploadUrl(
                    sanitizedFileName,
                    request.getContentType(),
                    request.getMediaType(),
                    request.getOwnerId(),
                    request.getDocumentType());

            // Return response
            Map<String, String> response = new HashMap<>();
            response.put("url", presignedUrl);
            response.put("fileName", sanitizedFileName);
            response.put("mediaType", request.getMediaType());
            response.put("documentType", request.getDocumentType());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to generate upload URL", e);
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
                "contentType", media.getContentType()
        ));
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

    @GetMapping("/download-by-path")
    public ResponseEntity<Map<String, String>> generateDownloadUrlByPath(
            @RequestParam String filePath) {
        String presignedUrl = mediaService.generateDownloadPresignedUrl(filePath);
        String fileName = filePath.substring(filePath.lastIndexOf('/') + 1);

        return ResponseEntity.ok(Map.of(
                "url", presignedUrl,
                "fileName", fileName
        ));
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

        public Long getFileSize() { return fileSize; }
        public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
        public String getMediaType() { return mediaType; }
        public void setMediaType(String mediaType) { this.mediaType = mediaType; }
    }
}