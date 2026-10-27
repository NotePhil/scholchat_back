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
public class ConversationParticipantId implements Serializable {

    @Column(name = "conversation_id")
    private String conversationId;

    @Column(name = "utilisateur_id")
    private String utilisateurId;

    public ConversationParticipantId() {}

    public ConversationParticipantId(String conversationId, String utilisateurId) {
        this.conversationId = conversationId;
        this.utilisateurId = utilisateurId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConversationParticipantId that)) return false;
        return Objects.equals(conversationId, that.conversationId) && Objects.equals(utilisateurId, that.utilisateurId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(conversationId, utilisateurId);
    }
}
