package cmr.notep.interfaces.modeles;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class Media implements Serializable {
    private String id;
    private String fileName;
    private String filePath;
    private String fileType;
    private Long fileSize;
    private String ownerId;
    private LocalDateTime uploadedDate;
    private String mediaType; // IMAGE, VIDEO, DOCUMENT, etc.
    private String contentType;
    private String bucketName;

    // Transient field for presigned URLs
    private String presignedUrl;
}