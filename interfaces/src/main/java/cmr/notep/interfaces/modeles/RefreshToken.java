package cmr.notep.interfaces.modeles;

import lombok.Data;

import java.time.Instant;

@Data
public class RefreshToken {
    private Long id;
    private String token;
    private Instant expiryDate;
    private Utilisateurs utilisateur;
}