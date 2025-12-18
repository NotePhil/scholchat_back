package cmr.notep.interfaces.modeles;

import lombok.Data;
import java.util.List;

@Data
public class MessageListResponseDto {
    private List<MessageDto> messages;
    private int totalCount;
}