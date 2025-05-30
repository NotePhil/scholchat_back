package cmr.notep.interfaces.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaDto {
    private String id;
    private String fileName;
    private String filePath;
    private String fileType;
    private Long fileSize;
    private String ownerId;
    private LocalDateTime uploadedDate;
    private String mediaType;
    private String contentType;
    private String bucketName;

    // For presigned URL operations
    private String presignedUrl;
    private String uploadUrl;
    private String downloadUrl;
}