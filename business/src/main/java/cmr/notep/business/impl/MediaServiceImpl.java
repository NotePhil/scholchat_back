package cmr.notep.business.impl;

import cmr.notep.business.services.MediaService;
import cmr.notep.business.business.MediaBusiness;
import cmr.notep.business.exceptions.SchoolException;
import cmr.notep.business.exceptions.enums.SchoolErrorCode;
import cmr.notep.business.security.AccessControlService;
import cmr.notep.business.security.CurrentUserService;
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
    private CurrentUserService currentUser;
    @Autowired
    private AccessControlService accessControl;

    // ─── Contrôles d'accès ───────────────────────────────────────────────────

    /** Segment de chemin S3 sûr (pas de "/", "..", etc.). */
    private static String safeSegment(String value, String field) {
        if (value == null) return null;
        String v = value.trim();
        if (!v.matches("[A-Za-z0-9_-]{1,64}")) {
            throw new SchoolException(SchoolErrorCode.INVALID_INPUT, "Valeur invalide pour " + field);
        }
        return v;
    }

    /**
     * Qui peut déposer un fichier pour {@code ownerId} : l'utilisateur lui-même ou un admin ;
     * en anonyme, uniquement le professeur en cours d'inscription (pièces justificatives).
     */
    private void requireCanUploadFor(String ownerId) {
        if (!currentUser.isAuthenticated()) {
            if (!accessControl.isSignupPendingProfessor(ownerId)) {
                throw new SchoolException(SchoolErrorCode.UNAUTHORIZED, "Authentification requise. Veuillez vous connecter.");
            }
            return;
        }
        if (!currentUser.isAdmin() && !currentUser.isSelf(ownerId)) {
            throw CurrentUserService.forbidden("Vous ne pouvez déposer des fichiers que pour votre propre compte.");
        }
    }

    /** Pièces d'identité (CNI) : visibles uniquement par leur propriétaire et les administrateurs. */
    private static boolean isIdentityDocument(MediaEntity media) {
        String path = media.getFilePath() != null ? media.getFilePath().toLowerCase() : "";
        return path.contains("/cni-recto/") || path.contains("/cni-verso/");
    }

    private boolean canRead(MediaEntity media) {
        if (!isIdentityDocument(media)) return true;
        return currentUser.isAdmin() || currentUser.isSelf(media.getOwnerId());
    }

    private void requireCanRead(MediaEntity media) {
        currentUser.requireAuthenticated();
        if (!canRead(media)) {
            throw CurrentUserService.forbidden("Ce document n'est accessible qu'à son propriétaire.");
        }
    }

    /** Résout l'identifiant accepté par download-by-path (chemin, id ou nom) pour le contrôle d'accès. */
    private Optional<MediaEntity> resolveMediaForCheck(String identifier) {
        try {
            Optional<MediaEntity> m = mediaRepository.findById(identifier);
            if (m.isEmpty()) m = mediaRepository.findByFilePath(identifier);
            if (m.isEmpty()) m = mediaRepository.findLatestByFileName(identifier);
            return m;
        } catch (RuntimeException e) {
            // plusieurs correspondances : on vérifie chacune
            List<MediaEntity> all = mediaRepository.findByFileName(identifier);
            all.forEach(this::requireCanRead);
            return Optional.empty();
        }
    }

    private void requireOwnerOrAdmin(MediaEntity media) {
        currentUser.requireAuthenticated();
        if (!currentUser.isAdmin() && !currentUser.isSelf(media.getOwnerId())) {
            throw CurrentUserService.forbidden("Seul le propriétaire du fichier peut effectuer cette action.");
        }
    }

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
            requireCanUploadFor(request.getOwnerId());
            request.setMediaType(safeSegment(request.getMediaType(), "mediaType"));
            request.setDocumentType(request.getDocumentType() == null
                    ? null : safeSegment(request.getDocumentType(), "documentType"));
            if (request.getMediaType() == null) {
                throw new SchoolException(SchoolErrorCode.INVALID_INPUT, "mediaType est requis");
            }
            // Professeur dont le profil n'est pas validé : seulement ses pièces justificatives.
            cmr.notep.business.security.ProfesseurVerificationService.requireTypePieceProfilSiNonValide(request.getDocumentType());
            
            String sanitizedFileName = request.getFileName()
                    .replaceAll("\\s+", "_")
                    .replaceAll("[^a-zA-Z0-9._-]", "");

            log.info("Sanitized filename: {}", sanitizedFileName);

            // Clean up any existing duplicates
            // (plus de nettoyage global par nom de fichier : il supprimait les fichiers homonymes des
            //  AUTRES utilisateurs ; saveMediaMetadata dédoublonne déjà par chemin.)

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
            String filePath = mediaBusiness.buildUploadFilePath(sanitizedFileName, request.getOwnerId(),
                    request.getMediaType(), request.getDocumentType());
            if (filePath != null) {
                response.put("filePath", filePath);
            }

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
        requireCanRead(media);
        String presignedUrl = mediaService.generateDownloadPresignedUrl(media.getFilePath());

        // Cache for 50 min in the browser — safe margin under the 55-min server cache TTL
        return ResponseEntity.ok()
                .header("Cache-Control", "private, max-age=3000")
                .body(Map.of(
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
            currentUser.requireAuthenticated();
            resolveMediaForCheck(filePath).ifPresent(this::requireCanRead);
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
        requireCanRead(media);
        MediaDto mediaDto = convertToDto(media);
        return ResponseEntity.ok(mediaDto);
    }

    @GetMapping("/cours/{coursId}")
    public ResponseEntity<List<MediaDto>> getMediaByCoursId(@PathVariable String coursId) {
        currentUser.requireAuthenticated();
        List<MediaEntity> mediaList = mediaBusiness.getMediaByCoursId(coursId);
        List<MediaDto> mediaDtoList = mediaList.stream().filter(this::canRead).map(this::convertToDto).collect(Collectors.toList());
        return ResponseEntity.ok(mediaDtoList);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<MediaDto>> getMediaByUserId(@PathVariable String userId) {
        if (userId == null || userId.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }

        currentUser.requireAuthenticated();
        List<MediaEntity> mediaList = mediaBusiness.getMediaByOwnerId(userId);
        List<MediaDto> mediaDtoList = mediaList.stream()
                .filter(this::canRead)
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(mediaDtoList);
    }

    @GetMapping("/find")
    public ResponseEntity<MediaDto> findByFileNameAndOwner(
            @RequestParam String fileName,
            @RequestParam String ownerId) {
        // Try exact match first
        currentUser.requireAuthenticated();
        Optional<MediaEntity> exact = mediaRepository.findByFileNameAndOwnerId(fileName, ownerId).filter(this::canRead);
        if (exact.isPresent()) return ResponseEntity.ok(convertToDto(exact.get()));
        // Fall back to partial match (e.g. stored as "timestamp_originalname.mp4")
        List<MediaEntity> partial = mediaRepository.findByOwnerIdAndFileNameContaining(ownerId, fileName)
                .stream().filter(this::canRead).collect(Collectors.toList());
        if (!partial.isEmpty()) return ResponseEntity.ok(convertToDto(partial.get(0)));
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{mediaId}")
    public ResponseEntity<Void> deleteMedia(@PathVariable String mediaId) {
        requireOwnerOrAdmin(mediaBusiness.getMediaById(mediaId));
        mediaBusiness.deleteMedia(mediaId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{mediaId}")
    public ResponseEntity<MediaDto> updateMedia(
            @PathVariable String mediaId,
            @RequestBody MediaUpdateRequest request) {
        requireOwnerOrAdmin(mediaBusiness.getMediaById(mediaId));
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
        requireOwnerOrAdmin(mediaBusiness.getMediaById(mediaId));
        if (!accessControl.userExists(newOwnerId)) {
            throw new SchoolException(SchoolErrorCode.NOT_FOUND, "Utilisateur introuvable");
        }
        mediaBusiness.updateMediaOwner(mediaId, newOwnerId);
        MediaEntity media = mediaBusiness.getMediaById(mediaId);
        return ResponseEntity.ok(convertToDto(media));
    }

    // New endpoint to clean up duplicates
    @PostMapping("/cleanup-duplicates/{fileName}")
    public ResponseEntity<Map<String, Object>> cleanupDuplicates(@PathVariable String fileName) {
        currentUser.requireAdmin();
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
    public ResponseEntity<org.springframework.core.io.InputStreamResource> proxyDownload(
            @PathVariable("mediaId") String mediaId,
            @RequestHeader(value = "Range", required = false) String rangeHeader) {
        try {
            MediaEntity media = mediaBusiness.getMediaById(mediaId);
            if (media == null) return ResponseEntity.notFound().build();
            requireCanRead(media);

            // For images/documents: redirect to presigned URL directly — avoids proxying large files
            String contentType = media.getContentType() != null ? media.getContentType() : "application/octet-stream";
            boolean isVideo = contentType.startsWith("video/") ||
                    (media.getMediaType() != null && media.getMediaType().equalsIgnoreCase("VIDEO"));

            if (!isVideo) {
                String presignedUrl = mediaService.generateDownloadPresignedUrl(media.getFilePath());
                return ResponseEntity.status(org.springframework.http.HttpStatus.FOUND)
                        .header("Location", presignedUrl)
                        .build();
            }

            String bucket = media.getBucketName() != null ? media.getBucketName() : "scholchat";
            String key = media.getFilePath();

            software.amazon.awssdk.services.s3.model.HeadObjectResponse head =
                s3Client.headObject(r -> r.bucket(bucket).key(key));
            long totalSize = head.contentLength();

            software.amazon.awssdk.services.s3.model.GetObjectRequest.Builder reqBuilder =
                software.amazon.awssdk.services.s3.model.GetObjectRequest.builder()
                    .bucket(bucket).key(key);

            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("Content-Type", contentType);
            headers.set("Content-Disposition", "inline; filename=\"" + media.getFileName() + "\"");
            headers.set("Accept-Ranges", "bytes");
            headers.set("Cache-Control", "private, max-age=3600");

            if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
                String[] parts = rangeHeader.substring(6).split("-");
                long start = Long.parseLong(parts[0]);
                long end = parts.length > 1 && !parts[1].isEmpty()
                        ? Long.parseLong(parts[1]) : totalSize - 1;
                end = Math.min(end, totalSize - 1);
                long length = end - start + 1;
                reqBuilder.range("bytes=" + start + "-" + end);
                java.io.InputStream stream = s3Client.getObject(reqBuilder.build());
                headers.set("Content-Range", "bytes " + start + "-" + end + "/" + totalSize);
                headers.setContentLength(length);
                return ResponseEntity.status(org.springframework.http.HttpStatus.PARTIAL_CONTENT)
                        .headers(headers)
                        .body(new org.springframework.core.io.InputStreamResource(stream));
            }

            java.io.InputStream stream = s3Client.getObject(reqBuilder.build());
            headers.setContentLength(totalSize);
            return ResponseEntity.ok().headers(headers)
                    .body(new org.springframework.core.io.InputStreamResource(stream));

        } catch (Exception e) {
            if (e instanceof SchoolException se) throw se; // refus d'accès : pas de repli
            log.error("Proxy download failed for mediaId {}: {}", mediaId, e.getMessage(), e);
            // Fallback: redirect to presigned URL if proxy fails
            try {
                MediaEntity media = mediaBusiness.getMediaById(mediaId);
                String presignedUrl = mediaService.generateDownloadPresignedUrl(media.getFilePath());
                return ResponseEntity.status(org.springframework.http.HttpStatus.FOUND)
                        .header("Location", presignedUrl)
                        .build();
            } catch (Exception fallbackEx) {
                return ResponseEntity.internalServerError().build();
            }
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
            filePath = java.net.URLDecoder.decode(filePath, java.nio.charset.StandardCharsets.UTF_8);
            // Seuls les objets réservés via /media/presigned-url peuvent être écrits, et seulement
            // par leur propriétaire (ou le professeur en cours d'inscription) : sinon n'importe qui
            // pouvait écraser n'importe quel fichier du bucket.
            if (filePath.contains("..")) {
                throw new SchoolException(SchoolErrorCode.INVALID_INPUT, "Chemin de fichier invalide");
            }
            MediaEntity cible = mediaRepository.findByFilePath(filePath)
                    .orElseThrow(() -> new SchoolException(SchoolErrorCode.OPERATION_INTERDITE,
                            "Aucun dépôt autorisé pour ce fichier. Demandez d'abord une URL de dépôt."));
            requireCanUploadFor(cible.getOwnerId());
            cmr.notep.business.security.ProfesseurVerificationService.requireTypePieceProfilSiNonValide(filePath);

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
            if (e instanceof SchoolException se) throw se;
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