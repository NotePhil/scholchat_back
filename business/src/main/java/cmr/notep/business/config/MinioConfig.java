package cmr.notep.business.config;

import io.minio.MinioClient;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class MinioConfig {

    @Value("${minio.endpoint:http://localhost:9000}")
    private String endpoint;

    @Value("${minio.access-key:kemognepenka}")
    private String accessKey;

    @Value("${minio.secret-key:kemogne237}")
    private String secretKey;

    @Value("${minio.bucket.name:scholchat}")
    private String bucketName;

    @Value("${minio.secure:false}")
    private boolean secure;

    @Value("${minio.presigned-url.expiry:3600}")
    private int presignedUrlExpiry; // Default 1 hour in seconds

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }
}