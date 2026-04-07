package cmr.notep.interfaces.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class MessageStatutDTO {
    private String messageId;
    private String utilisateurId;
    private boolean lu;
    private boolean favori;
    private Date dateLecture;
}
