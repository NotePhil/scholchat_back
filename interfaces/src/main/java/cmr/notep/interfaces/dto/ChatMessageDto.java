package cmr.notep.interfaces.dto;

import cmr.notep.interfaces.modeles.Media;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/** Message d'une conversation (chat). */
@Data
public class ChatMessageDto {
    private String id;
    private String conversationId;
    private String expediteurId;
    private String expediteurNom;
    private String expediteurPrenom;
    /** "" si supprimé pour tous. */
    private String contenu;
    private List<Media> medias = new ArrayList<>();
    private String date;
    private ReplyTo replyTo;
    private boolean supprimePourTous;
    /** Pour mes propres messages : SENT | READ (lu par tous les autres participants) ; null sinon. */
    private String statut;
    /** Message des autres : lu par moi. Mes messages : lu par tous les autres. */
    private boolean lu;
    /** Mis en favori par l'appelant. */
    private boolean favori;

    // ── Corbeille (uniquement dans GET /conversations/corbeille) ──
    private String supprimeLe;
    private String conversationTitre;
    /** DIRECT | GROUPE */
    private String conversationType;

    @Data
    public static class ReplyTo {
        private String id;
        private String expediteurId;
        private String expediteurNom;
        private String expediteurPrenom;
        /** ~100 premiers caractères, ou « 📎 Pièce jointe ». */
        private String extrait;
        private boolean supprime;
    }
}
