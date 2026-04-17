package cmr.notep.interfaces.dto;

import cmr.notep.modele.SessionMode;
import lombok.Data;

@Data
public class StartSessionRequestDTO {
    private SessionMode mode;
}
