package cmr.notep.business.impl;

import cmr.notep.business.config.MinioConfig;
import cmr.notep.business.exceptions.SchoolException;
import cmr.notep.business.exceptions.enums.SchoolErrorCode;
import cmr.notep.business.services.MediaService;
import io.minio.*;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class MinioMediaServiceImpl implements MediaService {

    private final MinioClient minioClient;
    private final MinioConfig minioConfig;

    @Autowired
    public MinioMediaServiceImpl(MinioClient minioClient, MinioConfig minioConfig) {
        this.minioClient = minioClient;
        this.minioConfig = minioConfig;

        // Ensure bucket exists on startup
        createBucketIfNotExists();
    }

    private void createBucketIfNotExists() {
        try {
            boolean bucketExists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(minioConfig.getBucketName()).build()
            );

            if (!bucketExists) {
                log.info("Creating bucket: {}", minioConfig.getBucketName());
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(minioConfig.getBucketName()).build()
                );
                log.info("Bucket created successfully: {}", minioConfig.getBucketName());
            } else {
                log.info("Bucket already exists: {}", minioConfig.getBucketName());
            }
        } catch (Exception e) {
            log.error("Error checking/creating bucket: {}", e.getMessage(), e);
            throw new SchoolException(SchoolErrorCode.INIT_ERROR, "Error initializing storage: " + e.getMessage());
        }
    }

    @Override
    public String generateUploadPresignedUrl(String filePath, String contentType) {
        try {
            log.debug("Generating upload URL for path: {}, contentType: {}", filePath, contentType);

            Map<String, String> reqParams = new HashMap<>();
            reqParams.put("Content-Type", contentType);

            String url = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.PUT)
                            .bucket(minioConfig.getBucketName())
                            .object(filePath)
                            .expiry(minioConfig.getPresignedUrlExpiry(), TimeUnit.SECONDS)
                            .extraQueryParams(reqParams)
                            .build()
            );

            log.debug("Generated upload URL: {}", url);
            return url;
        } catch (Exception e) {
            log.error("Error generating upload URL: {}", e.getMessage(), e);
            throw new SchoolException(SchoolErrorCode.OPERATION_FAILURE, "Error generating upload URL: " + e.getMessage());
        }
    }

    @Override
    public String generateDownloadPresignedUrl(String filePath) {
        try {
            log.debug("Generating download URL for path: {}", filePath);

            String url = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(minioConfig.getBucketName())
                            .object(filePath)
                            .expiry(minioConfig.getPresignedUrlExpiry(), TimeUnit.SECONDS)
                            .build()
            );

            log.debug("Generated download URL: {}", url);
            return url;
        } catch (Exception e) {
            log.error("Error generating download URL: {}", e.getMessage(), e);
            throw new SchoolException(SchoolErrorCode.OPERATION_FAILURE, "Error generating download URL: " + e.getMessage());
        }
    }

    @Override
    public void deleteMedia(String filePath) {
        try {
            log.debug("Deleting object at path: {}", filePath);

            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(filePath)
                            .build()
            );

            log.debug("Object deleted successfully: {}", filePath);
        } catch (Exception e) {
            log.error("Error deleting object: {}", e.getMessage(), e);
            throw new SchoolException(SchoolErrorCode.OPERATION_FAILURE, "Error deleting media: " + e.getMessage());
        }
    }

    @Override
    public boolean doesObjectExist(String filePath) {
        try {
            log.debug("Checking if object exists at path: {}", filePath);

            // Try to get object stat - if it succeeds, the object exists
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(filePath)
                            .build()
            );

            log.debug("Object exists: {}", filePath);
            return true;
        } catch (Exception e) {
            log.debug("Object does not exist or error checking: {}", filePath);
            return false;
        }
    }

    @Override
    public String getDefaultBucketName() {
        return minioConfig.getBucketName();
    }
}