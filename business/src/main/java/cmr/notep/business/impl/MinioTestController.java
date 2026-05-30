package cmr.notep.business.impl;

import cmr.notep.business.config.S3Config;
import cmr.notep.business.services.MediaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/test/minio")
@Slf4j
@RequiredArgsConstructor
public class MinioTestController {

    private final S3Client s3Client;
    private final S3Config s3Config;
    private final MediaService mediaService;

    @GetMapping("/connectivity")
    public Map<String, Object> testConnectivity() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            log.info("Testing MinIO connectivity...");
            
            // Test 1: List buckets
            ListBucketsResponse bucketsResponse = s3Client.listBuckets();
            List<String> bucketNames = bucketsResponse.buckets().stream()
                    .map(Bucket::name)
                    .collect(Collectors.toList());
            
            result.put("status", "SUCCESS");
            result.put("endpoint", s3Config.getEndpoint());
            result.put("buckets", bucketNames);
            result.put("targetBucket", s3Config.getBucketName());
            result.put("bucketExists", bucketNames.contains(s3Config.getBucketName()));
            
            // Test 2: Check if target bucket exists and is accessible
            if (bucketNames.contains(s3Config.getBucketName())) {
                try {
                    ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                            .bucket(s3Config.getBucketName())
                            .maxKeys(5)
                            .build();
                    
                    ListObjectsV2Response listResponse = s3Client.listObjectsV2(listRequest);
                    result.put("bucketAccessible", true);
                    result.put("objectCount", listResponse.keyCount());
                    
                    List<String> sampleObjects = listResponse.contents().stream()
                            .limit(5)
                            .map(S3Object::key)
                            .collect(Collectors.toList());
                    result.put("sampleObjects", sampleObjects);
                    
                } catch (Exception e) {
                    result.put("bucketAccessible", false);
                    result.put("bucketError", e.getMessage());
                }
            }
            
            // Test 3: Generate a test presigned URL
            try {
                String testPath = "test/connectivity-test.txt";
                String testUrl = mediaService.generateUploadPresignedUrl(testPath, "text/plain");
                result.put("presignedUrlGeneration", "SUCCESS");
                result.put("testPresignedUrl", testUrl);
            } catch (Exception e) {
                result.put("presignedUrlGeneration", "FAILED");
                result.put("presignedUrlError", e.getMessage());
            }
            
        } catch (Exception e) {
            log.error("MinIO connectivity test failed", e);
            result.put("status", "FAILED");
            result.put("error", e.getMessage());
            result.put("endpoint", s3Config.getEndpoint());
        }
        
        return result;
    }

    @GetMapping("/bucket-info")
    public Map<String, Object> getBucketInfo() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            String bucketName = s3Config.getBucketName();
            
            // Check bucket location
            GetBucketLocationRequest locationRequest = GetBucketLocationRequest.builder()
                    .bucket(bucketName)
                    .build();
            
            GetBucketLocationResponse locationResponse = s3Client.getBucketLocation(locationRequest);
            
            result.put("bucketName", bucketName);
            result.put("bucketLocation", locationResponse.locationConstraintAsString());
            
            // List some objects
            ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                    .bucket(bucketName)
                    .prefix("users/")
                    .maxKeys(10)
                    .build();
            
            ListObjectsV2Response listResponse = s3Client.listObjectsV2(listRequest);
            
            List<Map<String, Object>> objects = listResponse.contents().stream()
                    .map(obj -> {
                        Map<String, Object> objInfo = new HashMap<>();
                        objInfo.put("key", obj.key());
                        objInfo.put("size", obj.size());
                        objInfo.put("lastModified", obj.lastModified().toString());
                        return objInfo;
                    })
                    .collect(Collectors.toList());
            
            result.put("userObjects", objects);
            result.put("totalObjects", listResponse.keyCount());
            
        } catch (Exception e) {
            log.error("Failed to get bucket info", e);
            result.put("error", e.getMessage());
        }
        
        return result;
    }

    @PostMapping("/test-upload")
    public Map<String, Object> testUpload(@RequestParam String fileName) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            String testPath = "test/" + fileName;
            String contentType = "text/plain";
            
            // Generate upload URL
            String uploadUrl = mediaService.generateUploadPresignedUrl(testPath, contentType);
            
            // Generate download URL
            String downloadUrl = mediaService.generateDownloadPresignedUrl(testPath);
            
            result.put("status", "SUCCESS");
            result.put("uploadUrl", uploadUrl);
            result.put("downloadUrl", downloadUrl);
            result.put("filePath", testPath);
            result.put("bucket", s3Config.getBucketName());
            
        } catch (Exception e) {
            log.error("Test upload failed", e);
            result.put("status", "FAILED");
            result.put("error", e.getMessage());
        }
        
        return result;
    }
}