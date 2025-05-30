package cmr.notep.interfaces.dto;

import lombok.Data;

@Data
public class PasswordResetRequest {
    private String token;
    private String newPassword;
    private String confirmPassword;
}