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

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
        initializeBucket();
    }

    private void initializeBucket() {
        try {
            boolean bucketExists = minioClient.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .build());

            if (!bucketExists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(minioConfig.getBucketName())
                                .build());

                // Set bucket policy to allow public read if needed
                String policy = String.format(
                        "{\"Version\":\"2012-10-17\",\"Statement\":[{\"Effect\":\"Allow\",\"Principal\":{\"AWS\":[\"*\"]},\"Action\":[\"s3:GetObject\"],\"Resource\":[\"arn:aws:s3:::%s/*\"]}]}",
                        minioConfig.getBucketName());

                minioClient.setBucketPolicy(
                        SetBucketPolicyArgs.builder()
                                .bucket(minioConfig.getBucketName())
                                .config(policy)
                                .build()
                );

                log.info("Created bucket: {}", minioConfig.getBucketName());
            }
        } catch (Exception e) {
            log.error("Bucket initialization failed", e);
            throw new SchoolException(SchoolErrorCode.INIT_ERROR,
                    "Failed to initialize storage bucket: " + e.getMessage());
        }
    }

    @Override
    public String generateUploadPresignedUrl(String filePath, String contentType) {
        try {
            Map<String, String> reqParams = new HashMap<>();
            reqParams.put("Content-Type", contentType);

            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.PUT)
                            .bucket(minioConfig.getBucketName())
                            .object(filePath)
                            .expiry(minioConfig.getPresignedUrlExpiry(), TimeUnit.SECONDS)
                            .extraQueryParams(reqParams)
                            .build());
        } catch (Exception e) {
            log.error("Error generating upload URL for path: {}", filePath, e);
            throw new SchoolException(SchoolErrorCode.OPERATION_FAILURE,
                    "Failed to generate upload URL: " + e.getMessage());
        }
    }

    @Override
    public String generateDownloadPresignedUrl(String filePath) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(minioConfig.getBucketName())
                            .object(filePath)
                            .expiry(minioConfig.getPresignedUrlExpiry(), TimeUnit.SECONDS)
                            .build());
        } catch (Exception e) {
            log.error("Error generating download URL for path: {}", filePath, e);
            throw new SchoolException(SchoolErrorCode.OPERATION_FAILURE,
                    "Failed to generate download URL: " + e.getMessage());
        }
    }

    @Override
    public void deleteMedia(String filePath) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(filePath)
                            .build());
            log.info("Successfully deleted file: {}", filePath);
        } catch (Exception e) {
            log.error("Error deleting file: {}", filePath, e);
            throw new SchoolException(SchoolErrorCode.OPERATION_FAILURE,
                    "Failed to delete file: " + e.getMessage());
        }
    }

    @Override
    public boolean doesObjectExist(String filePath) {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(filePath)
                            .build());
            return true;
        } catch (Exception e) {
            log.debug("File does not exist: {}", filePath);
            return false;
        }
    }

    @Override
    public String getDefaultBucketName() {
        return minioConfig.getBucketName();
    }

    private String encodeFilePath(String filePath) {
        try {
            return URLEncoder.encode(filePath, StandardCharsets.UTF_8.toString())
                    .replaceAll("\\+", "%20")
                    .replaceAll("%2F", "/");
        } catch (Exception e) {
            log.error("Error encoding file path: {}", filePath, e);
            return filePath;
        }
    }
}