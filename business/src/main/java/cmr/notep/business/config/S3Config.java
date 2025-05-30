package cmr.notep.business.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

@Data
@Configuration
public class S3Config {

    @Value("${s3.endpoint:http://localhost:9000}")  // Your MinIO endpoint
    private String endpoint;

    @Value("${s3.access-key:kemognepenka}")  // Your MinIO root user
    private String accessKey;

    @Value("${s3.secret-key:kemogne237}")  // Your MinIO root password
    private String secretKey;

    @Value("${s3.bucket.name:scholchat}")
    private String bucketName;

    @Value("${s3.region:us-east-1}")
    private String region;

    @Value("${s3.presigned-url.expiry:3600}")
    private int presignedUrlExpiry;

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .endpointOverride(URI.create(endpoint))  // Points to your MinIO server
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .region(Region.of(region))
                .forcePathStyle(true)  // Important for MinIO compatibility
                .build();
    }
}