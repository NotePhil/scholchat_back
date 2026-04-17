package cmr.notep.interfaces.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ChapitreProgressDTO {
    private String userId;
    private String chapitreId;
    private String coursId;
    private boolean completed;
    private LocalDateTime completedAt;
}
