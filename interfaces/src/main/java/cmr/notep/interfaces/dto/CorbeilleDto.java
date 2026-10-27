package cmr.notep.interfaces.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/** Corbeille de la messagerie de l'appelant : discussions et messages supprimés pour lui, restaurables. */
@Data
public class CorbeilleDto {
    private List<ConversationDto> conversations = new ArrayList<>();
    private List<ChatMessageDto> messages = new ArrayList<>();
    private long total;
}
