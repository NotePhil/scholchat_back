package cmr.notep.business.impl;

import cmr.notep.business.config.S3Config;
import cmr.notep.business.exceptions.SchoolException;
import cmr.notep.business.exceptions.enums.SchoolErrorCode;
import cmr.notep.business.services.MediaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.*;

import java.time.Duration;
import java.net.URI;

@Service
@Slf4j
public class S3MediaServiceImpl implements MediaService {

    private final S3Client s3Client;
    private final S3Config s3Config;
    private final S3Presigner s3Presigner;

    @Autowired
    public S3MediaServiceImpl(S3Config s3Config) {
        this.s3Config = s3Config;
        this.s3Client = S3Client.builder()
                .endpointOverride(URI.create(s3Config.getEndpoint()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(s3Config.getAccessKey(), s3Config.getSecretKey())))
                .region(Region.of(s3Config.getRegion()))
                .forcePathStyle(true) // Configure path style here
                .build();

        this.s3Presigner = S3Presigner.builder()
                .endpointOverride(URI.create(s3Config.getEndpoint()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(s3Config.getAccessKey(), s3Config.getSecretKey())))
                .region(Region.of(s3Config.getRegion()))
                .build();

        initializeBucket();
    }

    private void initializeBucket() {
        try {
            HeadBucketRequest headBucketRequest = HeadBucketRequest.builder()
                    .bucket(s3Config.getBucketName())
                    .build();

            try {
                s3Client.headBucket(headBucketRequest);
                log.info("Bucket {} already exists", s3Config.getBucketName());
            } catch (NoSuchBucketException e) {
                CreateBucketRequest createBucketRequest = CreateBucketRequest.builder()
                        .bucket(s3Config.getBucketName())
                        .build();

                s3Client.createBucket(createBucketRequest);

                String policy = String.format(
                        "{\"Version\":\"2012-10-17\",\"Statement\":[{\"Effect\":\"Allow\",\"Principal\":{\"AWS\":[\"*\"]},\"Action\":[\"s3:GetObject\"],\"Resource\":[\"arn:aws:s3:::%s/*\"]}]}",
                        s3Config.getBucketName());

                PutBucketPolicyRequest putBucketPolicyRequest = PutBucketPolicyRequest.builder()
                        .bucket(s3Config.getBucketName())
                        .policy(policy)
                        .build();

                s3Client.putBucketPolicy(putBucketPolicyRequest);
                log.info("Created bucket: {}", s3Config.getBucketName());
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
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(s3Config.getBucketName())
                    .key(filePath)
                    .contentType(contentType)
                    .build();

            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofSeconds(s3Config.getPresignedUrlExpiry()))
                    .putObjectRequest(putObjectRequest)
                    .build();

            PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);
            return presignedRequest.url().toString();
        } catch (Exception e) {
            log.error("Error generating upload URL for path: {}", filePath, e);
            throw new SchoolException(SchoolErrorCode.OPERATION_FAILURE,
                    "Failed to generate upload URL: " + e.getMessage());
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
                    .signatureDuration(Duration.ofSeconds(s3Config.getPresignedUrlExpiry()))
                    .getObjectRequest(getObjectRequest)
                    .build();

            PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
            return presignedRequest.url().toString();
        } catch (Exception e) {
            log.error("Error generating download URL for path: {}", filePath, e);
            throw new SchoolException(SchoolErrorCode.OPERATION_FAILURE,
                    "Failed to generate download URL: " + e.getMessage());
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
            throw new SchoolException(SchoolErrorCode.OPERATION_FAILURE,
                    "Failed to delete file: " + e.getMessage());
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
        } catch (Exception e) {
            log.debug("File does not exist: {}", filePath);
            return false;
        }
    }

    @Override
    public String getDefaultBucketName() {
        return s3Config.getBucketName();
    }
}