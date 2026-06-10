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
    private final software.amazon.awssdk.services.s3.S3Client s3Client;

    @Autowired
    public MediaServiceImpl(MediaBusiness mediaBusiness, MediaService mediaService, MediaRepository mediaRepository, software.amazon.awssdk.services.s3.S3Client s3Client) {
        this.mediaBusiness = mediaBusiness;
        this.mediaService = mediaService;
        this.mediaRepository = mediaRepository;
        this.s3Client = s3Client;
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

            // Clean up any existing duplicates
            try { mediaBusiness.cleanupDuplicateMedia(sanitizedFileName); } catch (Exception e) { /* ignore */ }

            String presignedUrl = mediaBusiness.generateUploadUrl(
                    sanitizedFileName,
                    request.getContentType(),
                    request.getMediaType(),
                    request.getOwnerId(),
                    request.getDocumentType(),
                    request.getCoursId());

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

    @GetMapping("/cours/{coursId}")
    public ResponseEntity<List<MediaDto>> getMediaByCoursId(@PathVariable String coursId) {
        List<MediaEntity> mediaList = mediaBusiness.getMediaByCoursId(coursId);
        List<MediaDto> mediaDtoList = mediaList.stream().map(this::convertToDto).collect(Collectors.toList());
        return ResponseEntity.ok(mediaDtoList);
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

    @GetMapping("/find")
    public ResponseEntity<MediaDto> findByFileNameAndOwner(
            @RequestParam String fileName,
            @RequestParam String ownerId) {
        // Try exact match first
        Optional<MediaEntity> exact = mediaRepository.findByFileNameAndOwnerId(fileName, ownerId);
        if (exact.isPresent()) return ResponseEntity.ok(convertToDto(exact.get()));
        // Fall back to partial match (e.g. stored as "timestamp_originalname.mp4")
        List<MediaEntity> partial = mediaRepository.findByOwnerIdAndFileNameContaining(ownerId, fileName);
        if (!partial.isEmpty()) return ResponseEntity.ok(convertToDto(partial.get(0)));
        return ResponseEntity.notFound().build();
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
        dto.setCoursId(entity.getCoursId());
        return dto;
    }

    @GetMapping("/{mediaId}/content")
    public ResponseEntity<byte[]> proxyDownload(@PathVariable("mediaId") String mediaId) {
        try {
            MediaEntity media = mediaBusiness.getMediaById(mediaId);
            if (media == null) {
                return ResponseEntity.notFound().build();
            }

            log.info("Proxy download for media: id={}, filePath={}, bucket={}",
                    mediaId, media.getFilePath(), media.getBucketName());

            software.amazon.awssdk.services.s3.model.GetObjectRequest getRequest =
                software.amazon.awssdk.services.s3.model.GetObjectRequest.builder()
                    .bucket(media.getBucketName() != null ? media.getBucketName() : "scholchat")
                    .key(media.getFilePath())
                    .build();

            software.amazon.awssdk.core.ResponseBytes<software.amazon.awssdk.services.s3.model.GetObjectResponse> objectBytes =
                s3Client.getObjectAsBytes(getRequest);

            byte[] fileBytes = objectBytes.asByteArray();

            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("Content-Type", media.getContentType() != null ? media.getContentType() : "application/octet-stream");
            headers.set("Content-Disposition", "inline; filename=\"" + media.getFileName() + "\"");
            headers.set("Cache-Control", "public, max-age=3600");

            return new ResponseEntity<>(fileBytes, headers, org.springframework.http.HttpStatus.OK);
        } catch (Exception e) {
            log.error("Proxy download failed for mediaId {}: {}", mediaId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/proxy-upload")
    public ResponseEntity<Map<String, String>> proxyUpload(
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file,
            @RequestParam("presignedUrl") String presignedUrl,
            @RequestParam("contentType") String contentType) {
        try {
            log.info("=== PROXY UPLOAD START ===");
            log.info("File: {}, Size: {}, ContentType: {}", file.getOriginalFilename(), file.getSize(), contentType);

            // Re-generate a trusted presigned URL server-side using the same path
            // to prevent the frontend from sending a URL pointing to the wrong port/host
            java.net.URI uri = java.net.URI.create(presignedUrl);
            String path = uri.getPath();
            String bucketPrefix = "/" + mediaService.getDefaultBucketName() + "/";
            String filePath = path.startsWith(bucketPrefix)
                    ? path.substring(bucketPrefix.length())
                    : path.replaceFirst("^/", "");

            // Remove any query string appended to the path
            if (filePath.contains("?")) {
                filePath = filePath.substring(0, filePath.indexOf("?"));
            }

            String trustedUrl = mediaService.generateUploadPresignedUrl(filePath, contentType);
            log.info("Re-generated trusted presigned URL for path: {}", filePath);

            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("Content-Type", contentType);
            org.springframework.http.HttpEntity<byte[]> requestEntity =
                    new org.springframework.http.HttpEntity<>(file.getBytes(), headers);

            org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
            restTemplate.exchange(trustedUrl, org.springframework.http.HttpMethod.PUT, requestEntity, String.class);

            log.info("=== PROXY UPLOAD SUCCESS ===");
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "File uploaded successfully",
                    "fileName", file.getOriginalFilename() != null ? file.getOriginalFilename() : "unknown"
            ));
        } catch (Exception e) {
            log.error("Proxy upload failed: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error",
                    "message", "Upload failed: " + e.getMessage()
            ));
        }
    }

    public static class PresignedUrlRequest {
        private String fileName;
        private String contentType;
        private String mediaType;
        private String ownerId;
        private String documentType;
        private String coursId;

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
        public String getCoursId() { return coursId; }
        public void setCoursId(String coursId) { this.coursId = coursId; }
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