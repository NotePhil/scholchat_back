package cmr.notep.interfaces.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/** Page de messages d'une conversation, dans l'ordre chronologique. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessagePageDto {
    private List<ChatMessageDto> messages = new ArrayList<>();
    /** Des messages plus anciens existent (charger avec before = id du plus ancien). */
    private boolean hasMore;
}
