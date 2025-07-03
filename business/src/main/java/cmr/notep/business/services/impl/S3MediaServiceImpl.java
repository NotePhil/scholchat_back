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
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3MediaServiceImpl implements MediaService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final S3Config s3Config;

    @Override
    public String generateUploadPresignedUrl(String filePath, String contentType) {
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(s3Config.getBucketName())
                    .key(filePath)
                    .contentType(contentType)
                    .build();

            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(15)) // 15 minutes expiry
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
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(s3Config.getBucketName())
                    .key(filePath)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofHours(1)) // 1 hour expiry
                    .getObjectRequest(getObjectRequest)
                    .build();

            PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
            return presignedRequest.url().toString();
        } catch (Exception e) {
            log.error("Error generating download URL for path: {}", filePath, e);
            throw new RuntimeException("Failed to generate download URL", e);
        }
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
            // S3 doesn't actually have folders, but we can create a placeholder object
            String folderKey = folderPath.endsWith("/") ? folderPath : folderPath + "/";
            folderKey += ".keep";

            if (!doesObjectExist(folderKey)) {
                PutObjectRequest putRequest = PutObjectRequest.builder()
                        .bucket(s3Config.getBucketName())
                        .key(folderKey)
                        .build();

                s3Client.putObject(putRequest, RequestBody.fromString(""));
                log.info("Created folder placeholder: {}", folderKey);
            }
        } catch (Exception e) {
            log.error("Error ensuring folder exists: {}", folderPath, e);
            throw new RuntimeException("Failed to create folder", e);
        }
    }
}