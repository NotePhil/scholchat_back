package cmr.notep.business.services.impl;

import cmr.notep.business.config.S3Config;
import cmr.notep.business.services.MediaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.*;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3MediaServiceImpl implements MediaService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final S3Config s3Config;

    // filePath -> (presignedUrl, expiresAt)
    private final Map<String, CachedUrl> downloadUrlCache = new ConcurrentHashMap<>();

    private record CachedUrl(String url, Instant expiresAt) {
        boolean isValid() { return Instant.now().isBefore(expiresAt); }
    }

    @Override
    public String generateUploadPresignedUrl(String filePath, String contentType) {
        try {
            // 1. THE CRITICAL FIX: Use the bucket name from config (s3Config.getBucketName())
            //    and the filePath as the key (folder + filename)
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(s3Config.getBucketName()) // <- Use the configured bucket name "scholchat"
                    .key(filePath)                   // <- This is the full path *inside* the bucket: "users/.../file.jpg"
                    .contentType(contentType)
                    .build();

            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(s3Config.getPresignedUrlExpiry()))
                    .putObjectRequest(putObjectRequest)
                    .build();

            PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);
            return presignedRequest.url().toString();

        } catch (Exception e) {
            log.error("Error generating upload URL for path: {}", filePath, e);
            throw new RuntimeException("Failed to generate upload URL", e);
        }
    }

    @Override
    public String generateDownloadPresignedUrl(String filePath) {
        CachedUrl cached = downloadUrlCache.get(filePath);
        if (cached != null && cached.isValid()) {
            return cached.url();
        }
        try {
            Duration ttl = Duration.ofHours(1);
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(s3Config.getBucketName())
                    .key(filePath)
                    .build();
            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(ttl)
                    .getObjectRequest(getObjectRequest)
                    .build();
            String url = s3Presigner.presignGetObject(presignRequest).url().toString();
            // Cache for 55 min — 5 min before the URL actually expires
            downloadUrlCache.put(filePath, new CachedUrl(url, Instant.now().plus(Duration.ofMinutes(55))));
            return url;
        } catch (Exception e) {
            log.error("Error generating download URL for path: {}", filePath, e);
            throw new RuntimeException("Failed to generate download URL", e);
        }
    }

    public void evictDownloadUrlCache(String filePath) {
        downloadUrlCache.remove(filePath);
    }


    @Override
    public void deleteMedia(String filePath) {
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(s3Config.getBucketName())
                    .key(filePath)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            log.info("Successfully deleted file: {}", filePath);
        } catch (Exception e) {
            log.error("Error deleting file: {}", filePath, e);
            throw new RuntimeException("Failed to delete media", e);
        }
    }

    @Override
    public boolean doesObjectExist(String filePath) {
        try {
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(s3Config.getBucketName())
                    .key(filePath)
                    .build();

            s3Client.headObject(headObjectRequest);
            return true;
        } catch (NoSuchKeyException e) {
            log.debug("File does not exist: {}", filePath);
            return false;
        } catch (Exception e) {
            log.error("Error checking file existence: {}", filePath, e);
            throw new RuntimeException("Failed to check file existence", e);
        }
    }

    @Override
    public String getDefaultBucketName() {
        return s3Config.getBucketName();
    }

    @Override
    public void moveMedia(String sourcePath, String destinationPath) {
        try {
            // First copy the object
            CopyObjectRequest copyRequest = CopyObjectRequest.builder()
                    .sourceBucket(s3Config.getBucketName())
                    .sourceKey(sourcePath)
                    .destinationBucket(s3Config.getBucketName())
                    .destinationKey(destinationPath)
                    .build();

            s3Client.copyObject(copyRequest);

            // Then delete the original
            deleteMedia(sourcePath);

            log.info("Successfully moved file from {} to {}", sourcePath, destinationPath);
        } catch (Exception e) {
            log.error("Error moving file from {} to {}", sourcePath, destinationPath, e);
            throw new RuntimeException("Failed to move media", e);
        }
    }

    @Override
    public void ensureFolderExists(String folderPath) {
        try {
            String folderKey = folderPath.endsWith("/") ? folderPath : folderPath + "/";
            folderKey += ".keep";

            // Check if the placeholder already exists using the correct bucket
            if (!doesObjectExist(folderKey)) {
                PutObjectRequest putRequest = PutObjectRequest.builder()
                        .bucket(s3Config.getBucketName()) // <- FIX HERE
                        .key(folderKey)                  // <- FIX HERE
                        .build();
                s3Client.putObject(putRequest, RequestBody.fromString(""));
            }
        } catch (Exception e) {
            log.error("Error ensuring folder exists: {}", folderPath, e);
            throw new RuntimeException("Failed to create folder", e);
        }
    }


}