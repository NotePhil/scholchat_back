package cmr.notep.business.utils;

import cmr.notep.business.config.JwtConfig;
import cmr.notep.business.exceptions.SchoolException;
import cmr.notep.business.exceptions.enums.SchoolErrorCode;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.*;
import java.util.function.Function;

@Component
@Slf4j
public class JwtUtil {
    private final Key secretKey;
    private final long accessTokenExpirationMillis;
    private final long refreshTokenExpirationMillis;
    private final JwtConfig jwtConfig;

    public JwtUtil(JwtConfig jwtConfig) {
        this.secretKey = Keys.hmacShaKeyFor(jwtConfig.getSecretKey().getBytes());
        this.accessTokenExpirationMillis = jwtConfig.getAccessTokenExpirationMillis();
        this.refreshTokenExpirationMillis = jwtConfig.getRefreshTokenExpirationMillis();
        this.jwtConfig = jwtConfig;  // Remove the second parameter
    }


    // Generate an access token with roles
    public String generateAccessToken(String email, List<String> roles) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", roles); // Add roles to token
        return createToken(claims, email, accessTokenExpirationMillis);
    }

    // Generate a refresh token (without roles)
    public String generateRefreshToken(String email) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, email, refreshTokenExpirationMillis);
    }


    public String generatePasswordResetToken(String email) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtConfig.getPasswordResetTokenExpirationMillis()))
                .signWith(secretKey, SignatureAlgorithm.HS256) // Utilisez secretKey au lieu de jwtConfig.getSecretKey()
                .compact();
    }

    public boolean validatePasswordResetToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKey) // Utilisez secretKey ici aussi
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    private String createToken(Map<String, Object> claims, String subject, long expirationMillis) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationMillis))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String getEmailFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    public List<String> getRolesFromToken(String token) {
        return getClaimFromToken(token, claims -> claims.get("roles", List.class));
    }

    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    private <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    private Claims getAllClaimsFromToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            throw new SchoolException(SchoolErrorCode.TOKEN_EXPIRED, "The token has expired");
        } catch (SignatureException e) {
            throw new SchoolException(SchoolErrorCode.INVALID_TOKEN, "Invalid token signature");
        } catch (MalformedJwtException e) {
            throw new SchoolException(SchoolErrorCode.INVALID_TOKEN, "Malformed token");
        } catch (UnsupportedJwtException e) {
            throw new SchoolException(SchoolErrorCode.INVALID_TOKEN, "Unsupported token");
        } catch (IllegalArgumentException e) {
            throw new SchoolException(SchoolErrorCode.INVALID_TOKEN, "Token is empty or null");
        }
    }

    public boolean validateToken(String token) {
        try {
            getAllClaimsFromToken(token);
            return true;
        } catch (SchoolException e) {
            log.error("Token validation failed: {}", e.getMessage());
            return false;
        }
    }
}
