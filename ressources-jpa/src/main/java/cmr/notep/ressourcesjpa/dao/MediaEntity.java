package cmr.notep.ressourcesjpa.dao;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "media", schema = "ressources")
@Getter
@Setter
@NoArgsConstructor
public class MediaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false, columnDefinition = "UUID")
    private String id;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "file_path")
    private String filePath;

    @Column(name = "file_type")
    private String fileType;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "owner_id")
    private String ownerId;

    @Column(name = "uploaded_date")
    private LocalDateTime uploadedDate = LocalDateTime.now();

    @Column(name = "media_type")
    private String mediaType; // IMAGE, VIDEO, etc.

    @Column(name = "bucket_name")
    private String bucketName;

    @Column(name = "content_type")
    private String contentType;
}