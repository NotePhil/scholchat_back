package cmr.notep.interfaces.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LoginDto {
    private String email;
    private String password;
}
