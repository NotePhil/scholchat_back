package cmr.notep.ressourcesjpa.dao;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "message_statut", schema = "ressources")
public class MessageStatutEntity {

    @EmbeddedId
    private MessageStatutId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("utilisateurId")
    @JoinColumn(name = "utilisateur_id")
    private UtilisateursEntity utilisateur;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("messageId")
    @JoinColumn(name = "message_id")
    private MessagesEntity message;

    @Column(name = "lu", nullable = false)
    private boolean lu = false;

    @Column(name = "favori", nullable = false)
    private boolean favori = false;

    @Column(name = "date_lecture")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateLecture;
}
