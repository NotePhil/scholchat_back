package cmr.notep.interfaces.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/** Conversation de la messagerie (chat), vue par l'appelant. */
@Data
public class ConversationDto {
    private String id;
    /** DIRECT | GROUPE */
    private String type;
    private String titre;
    private String classeId;
    private String createdBy;
    private String createdAt;
    private String lastMessageAt;
    /** Tous les participants, appelant compris. */
    private List<Participant> participants = new ArrayList<>();
    private DernierMessage dernierMessage;
    private long nonLus;
    private boolean muted;

    // ── Filtres de la liste (propres à l'appelant) ──
    /** Discussion mise en favori par l'appelant. */
    private boolean favori;
    /** Contient au moins un message que l'appelant a mis en favori. */
    private boolean contientFavori;
    /** « Marquer comme non lu » (filtre « Non lus » avec nonLus > 0). */
    private boolean marqueNonLu;
    /** Filtre « Général » : groupe, message à une classe ou message général (plusieurs classes). */
    private boolean general;
    /** CLASSE (message à une classe) | GENERAL (message à plusieurs classes) | GROUPE | null (discussion individuelle). */
    private String categorie;

    // ── Corbeille ──
    /** Date de mise à la corbeille (uniquement dans GET /conversations/corbeille). */
    private String supprimeLe;
    /** Corbeille : un message plus récent a fait réapparaître la discussion ; seul l'historique antérieur y reste. */
    private boolean reapparue;

    @Data
    public static class Participant {
        private String id;
        private String prenom;
        private String nom;
        private String typeUtilisateur;
        private String lastReadAt;
    }

    @Data
    public static class DernierMessage {
        private String id;
        /** Texte, « 📎 Pièce jointe » s'il n'y a que des pièces jointes, "" si supprimé pour tous. */
        private String contenu;
        private String expediteurId;
        private String expediteurPrenom;
        private String expediteurNom;
        private String date;
        private boolean supprime;
    }
}
