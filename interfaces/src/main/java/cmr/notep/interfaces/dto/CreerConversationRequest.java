package cmr.notep.interfaces.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CreerConversationRequest {
    /** Autres participants : un seul => conversation DIRECT (retrouvée si elle existe), plusieurs => GROUPE. */
    private List<String> participantIds = new ArrayList<>();
    /** Titre du groupe (facultatif). */
    private String titre;
}
