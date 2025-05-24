package cmr.notep.interfaces.modeles;

import cmr.notep.modele.InteractionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Interaction {

    private String id;
    private InteractionType type;
    private String content;
    private LocalDateTime creationDate;
    private String niveau;
    private String createdById;
    private String eventId;
    private String messageId;
}
