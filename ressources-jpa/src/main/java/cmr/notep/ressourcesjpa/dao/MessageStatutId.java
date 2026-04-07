package cmr.notep.ressourcesjpa.dao;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@Embeddable
public class MessageStatutId implements Serializable {

    @Column(name = "utilisateur_id")
    private String utilisateurId;

    @Column(name = "message_id")
    private String messageId;

    public MessageStatutId() {}

    public MessageStatutId(String utilisateurId, String messageId) {
        this.utilisateurId = utilisateurId;
        this.messageId = messageId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MessageStatutId)) return false;
        MessageStatutId that = (MessageStatutId) o;
        return Objects.equals(utilisateurId, that.utilisateurId) && Objects.equals(messageId, that.messageId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(utilisateurId, messageId);
    }
}
