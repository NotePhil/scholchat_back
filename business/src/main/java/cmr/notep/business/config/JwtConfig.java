package cmr.notep.business.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtConfig {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long expirationMillis;

    public String getSecretKey() {
        return secretKey;
    }

    public long getExpirationMillis() {
        return expirationMillis;
    }
}
