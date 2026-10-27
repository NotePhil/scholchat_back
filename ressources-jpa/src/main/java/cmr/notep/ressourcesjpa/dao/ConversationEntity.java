package cmr.notep.ressourcesjpa.dao;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Conversation de la messagerie (type chat) : DIRECT (une seule par paire d'utilisateurs,
 * garantie par {@code direct_key}) ou GROUPE. Les dates sont des heures UTC.
 */
@Getter
@Setter
@Entity
@Table(name = "conversations", schema = "ressources")
public class ConversationEntity {

    public static final String DIRECT = "DIRECT";
    public static final String GROUPE = "GROUPE";

    @Id
    @Column(name = "id", nullable = false, updatable = false, length = 36)
    private String id;

    /** DIRECT | GROUPE */
    @Column(name = "type", nullable = false, length = 10)
    private String type;

    @Column(name = "titre")
    private String titre;

    @Column(name = "classe_id")
    private String classeId;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;

    /** DIRECT : "idMin|idMax" (ordre binaire), unique. */
    @Column(name = "direct_key", length = 520, unique = true)
    private String directKey;

    /** md5 des identifiants triés des participants joints par '|'. */
    @Column(name = "participants_key", length = 32)
    private String participantsKey;
}
