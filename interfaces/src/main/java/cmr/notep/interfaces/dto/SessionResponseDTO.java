package cmr.notep.interfaces.dto;

import cmr.notep.modele.SessionMode;
import cmr.notep.modele.SessionStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class SessionResponseDTO {
    private String sessionId;
    private String roomName;
    private String jitsiJwt;
    private String jitsiDomain;
    private SessionMode mode;
    private SessionStatus status;
    private String coursId;
    private String coursTitle;
    private String currentChapitreId;
    private List<ChapitreDTO> chapitres;
    private List<ParticipantDTO> participants;
    private LocalDateTime startedAt;

    @Data
    @Builder
    public static class ChapitreDTO {
        private String id;
        private String titre;
        private Integer ordre;
        private String contenu;
        private String fileUrl;
    }

    @Data
    @Builder
    public static class ParticipantDTO {
        private String userId;
        private String userName;
    }
}
