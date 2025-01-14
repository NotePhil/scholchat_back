package cmr.notep.business.utils;

import cmr.notep.business.exceptions.SchoolException;
import cmr.notep.business.exceptions.enums.SchoolErrorCode;
import cmr.notep.business.config.JwtConfig;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.Map;

@Component
public class JwtUtil {
    private final Key secretKey;
    private final long expirationMillis;

    public JwtUtil(JwtConfig jwtConfig) {
        this.secretKey = Keys.hmacShaKeyFor(jwtConfig.getSecretKey().getBytes());
        this.expirationMillis = jwtConfig.getExpirationMillis();
    }

    // Generate a JWT token
    public String generateToken(String subject, Map<String, Object> claims) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + expirationMillis);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    // Validate a JWT token
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            throw new SchoolException(SchoolErrorCode.INVALID_TOKEN, "Invalid JWT token");
        }
    }

    // Extract claims from a JWT token
    public Claims extractClaims(String token) {
        try {
            return Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody();
        } catch (JwtException e) {
            throw new SchoolException(SchoolErrorCode.INVALID_TOKEN, "Failed to extract claims");
        }
    }
}
