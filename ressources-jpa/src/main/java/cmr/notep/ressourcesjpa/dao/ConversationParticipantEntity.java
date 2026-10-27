package cmr.notep.ressourcesjpa.dao;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "conversation_participants", schema = "ressources")
public class ConversationParticipantEntity {

    @EmbeddedId
    private ConversationParticipantId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("utilisateurId")
    @JoinColumn(name = "utilisateur_id")
    private UtilisateursEntity utilisateur;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;

    /** Messages des autres envoyés après cette date = non lus. */
    @Column(name = "last_read_at")
    private LocalDateTime lastReadAt;

    @Column(name = "muted", nullable = false)
    private boolean muted = false;

    /**
     * « Supprimer la conversation pour moi » : la conversation est dans la corbeille de l'utilisateur
     * depuis cette date (restaurable) ; masquée tant qu'aucun message plus récent n'arrive, et
     * l'historique antérieur reste caché tant qu'elle n'est pas restaurée.
     */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /** Discussion mise en favori par l'utilisateur. */
    @Column(name = "favori", nullable = false)
    private boolean favori = false;

    /** « Marquer comme non lu » : effacé dès que l'utilisateur lit la conversation ou y écrit. */
    @Column(name = "marque_non_lu", nullable = false)
    private boolean marqueNonLu = false;

    /** Supprimée définitivement de la corbeille : les messages jusqu'à cette date ne sont plus jamais montrés. */
    @Column(name = "historique_depuis")
    private LocalDateTime historiqueDepuis;

    /** Messages of the conversation up to this date are hidden for the user (trash or permanent delete). */
    public LocalDateTime plancherHistorique() {
        if (deletedAt == null) return historiqueDepuis;
        if (historiqueDepuis == null) return deletedAt;
        return deletedAt.isAfter(historiqueDepuis) ? deletedAt : historiqueDepuis;
    }
}
