package cmr.notep.interfaces.modeles;

import cmr.notep.modele.EtatUtilisateur;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String accessToken;
//    private String refreshToken;
    private String tokenType;
    private Long expiresIn;
    private String userId;
    private String userEmail;
    private String username;
    private String userType;
    private EtatUtilisateur userStatus;
}