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

    // Generate an access token with roles, additionally flagging entities (classe/etablissement)
    // whose offre/contrat is expired. Additive overload: existing callers/consumers of
    // generateAccessToken(email, roles) and its "roles" claim are unaffected.
    public String generateAccessToken(String email, List<String> roles, List<Map<String, String>> expiredEntities) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", roles);
        if (expiredEntities != null && !expiredEntities.isEmpty()) {
            claims.put("expiredEntities", expiredEntities);
        }
        return createToken(claims, email, accessTokenExpirationMillis);
    }

    /**
     * Jeton d'accès portant aussi le profil choisi à la connexion ("selectedRole") : sert à savoir si
     * l'appelant agit en professeur (cf. ProfesseurVerificationService) sur un compte multi-rôles.
     */
    public String generateAccessToken(String email, List<String> roles, List<Map<String, String>> expiredEntities,
                                      String selectedRole) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", roles);
        if (expiredEntities != null && !expiredEntities.isEmpty()) {
            claims.put("expiredEntities", expiredEntities);
        }
        if (selectedRole != null && !selectedRole.isBlank()) {
            claims.put("selectedRole", selectedRole);
        }
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

    // Jeton de renouvellement d'offre/contrat (meme forme que le jeton de reset de mot de passe,
    // mais porte le type et l'id de l'entite (classe/etablissement) a renouveler plutot qu'un email).
    public String generateRenewalToken(String entityType, String entityId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("entityType", entityType);
        claims.put("entityId", entityId);
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(entityId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtConfig.getRenewalTokenExpirationMillis()))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateRenewalToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Jeton des liens "approuver / rejeter la classe" envoyés à l'établissement : lie le lien
    // à UNE classe et UN établissement (sans lui, n'importe qui connaissant les deux ids
    // pouvait approuver une classe sans être connecté).
    public String generateClassDecisionToken(String classeId, String etablissementId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("purpose", "class-decision");
        claims.put("classeId", classeId);
        claims.put("etablissementId", etablissementId);
        return Jwts.builder()
                .setClaims(claims)
                .setSubject("class-decision:" + classeId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 30L * 24 * 3600 * 1000))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isValidClassDecisionToken(String token, String classeId, String etablissementId) {
        if (token == null || token.isBlank()) return false;
        try {
            Claims claims = Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody();
            return "class-decision".equals(claims.get("purpose", String.class))
                    && classeId != null && classeId.equals(claims.get("classeId", String.class))
                    && etablissementId != null && etablissementId.equals(claims.get("etablissementId", String.class));
        } catch (Exception e) {
            return false;
        }
    }

    /** Finalité du jeton de dépôt des pièces justificatives d'un professeur pendant l'inscription. */
    public static final String PURPOSE_PROFESSOR_DOCUMENTS = "PROFESSOR_DOCUMENTS";
    /** Durée de validité du jeton de dépôt des pièces (2 h). */
    public static final long PROFESSOR_DOCUMENTS_TOKEN_MILLIS = 2L * 3600 * 1000;

    /**
     * Jeton renvoyé par l'inscription publique d'un professeur : autorise, sans connexion, le dépôt de
     * SES pièces (CNI recto/verso, selfie) pendant 2 h. Sans claim "roles" : il ne vaut jamais
     * authentification (JwtAuthenticationFilter le refuse comme jeton d'accès).
     */
    public String generateProfessorDocumentsUploadToken(String userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("purpose", PURPOSE_PROFESSOR_DOCUMENTS);
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + PROFESSOR_DOCUMENTS_TOKEN_MILLIS))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    /** Vrai si {@code token} est un jeton de dépôt des pièces valide (signature, expiration) émis pour {@code userId}. */
    public boolean isValidProfessorDocumentsUploadToken(String token, String userId) {
        if (token == null || token.isBlank() || userId == null) return false;
        try {
            Claims claims = Jwts.parserBuilder().setSigningKey(secretKey).build()
                    .parseClaimsJws(token.trim()).getBody();
            return PURPOSE_PROFESSOR_DOCUMENTS.equals(claims.get("purpose", String.class))
                    && userId.equals(claims.getSubject());
        } catch (Exception e) {
            return false;
        }
    }

    public String getEntityTypeFromRenewalToken(String token) {
        return getClaimFromToken(token, claims -> claims.get("entityType", String.class));
    }

    public String getEntityIdFromRenewalToken(String token) {
        return getClaimFromToken(token, claims -> claims.get("entityId", String.class));
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

    /** Profil choisi à la connexion, ou null (jetons antérieurs). */
    public String getSelectedRoleFromToken(String token) {
        try {
            return getClaimFromToken(token, claims -> claims.get("selectedRole", String.class));
        } catch (Exception e) {
            return null;
        }
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
