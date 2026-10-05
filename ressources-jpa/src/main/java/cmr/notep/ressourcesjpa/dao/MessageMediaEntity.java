package cmr.notep.ressourcesjpa.dao;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Attachment of a message (image / video / document). The file itself lives in
 * object storage (uploaded via POST /media/presigned-url); this row only keeps
 * its storage key and metadata.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "message_medias", schema = "ressources")
public class MessageMediaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false, length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id", nullable = false)
    private MessagesEntity message;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_path", nullable = false, length = 512)
    private String filePath;

    @Column(name = "content_type", length = 150)
    private String contentType;

    /** IMAGE | VIDEO | DOCUMENT */
    @Column(name = "media_type", length = 20)
    private String mediaType;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "ordre", nullable = false)
    private int ordre;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation;

    @PrePersist
    protected void onCreate() {
        if (dateCreation == null) {
            dateCreation = LocalDateTime.now();
        }
    }
}
