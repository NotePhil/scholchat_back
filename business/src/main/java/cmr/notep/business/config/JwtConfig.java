package cmr.notep.business.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtConfig {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpirationMillis;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpirationMillis;

    @Value("${jwt.password-reset-token-expiration}")
    private long passwordResetTokenExpirationMillis;

    public long getPasswordResetTokenExpirationMillis() {
        return passwordResetTokenExpirationMillis;
    }
    public String getSecretKey() {
        return secretKey;
    }
    public long getAccessTokenExpirationMillis() {
        return accessTokenExpirationMillis;
    }
    public long getRefreshTokenExpirationMillis() {
        return refreshTokenExpirationMillis;
    }
}
