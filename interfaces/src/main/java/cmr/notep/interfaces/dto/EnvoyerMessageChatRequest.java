package cmr.notep.interfaces.dto;

import cmr.notep.interfaces.modeles.Media;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class EnvoyerMessageChatRequest {
    private String contenu;
    /** Pièces jointes déjà téléversées (POST /media/presigned-url) : filePath, fileName, contentType, fileSize. */
    private List<Media> medias = new ArrayList<>();
    private String replyToId;
}
