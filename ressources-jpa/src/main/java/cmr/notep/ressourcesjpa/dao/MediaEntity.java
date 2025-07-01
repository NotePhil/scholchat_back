package cmr.notep.ressourcesjpa.dao;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "media", schema = "ressources")
@Getter
@Setter
@NoArgsConstructor
public class MediaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private String id;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "file_path", unique = true)
    private String filePath;

    @Column(name = "content_type")
    private String contentType;

    @Column(name = "media_type")
    private String mediaType;

    @Column(name = "uploaded_date")
    private LocalDateTime uploadedDate;

    @Column(name = "owner_id")
    private String ownerId;

    @Column(name = "bucket_name")
    private String bucketName;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "file_type")
    private String fileType;

    @ManyToOne
    @JoinColumn(name = "evenement_id")
    private EvenementEntity evenement;
}