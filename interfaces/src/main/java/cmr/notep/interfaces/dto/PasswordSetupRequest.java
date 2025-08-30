package cmr.notep.interfaces.dto;

import lombok.Data;

@Data
public class PasswordSetupRequest {
    private String email;
    private String passeAccess;
    private String type;
}