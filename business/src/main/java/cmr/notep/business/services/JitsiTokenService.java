package cmr.notep.business.services;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class JitsiTokenService {

    // vpaas-magic-cookie-b0d7354d679944a197a3e35a0d1e2c60
    @Value("${jitsi.app.id}")
    private String appId;

    @Value("${jitsi.api.key.id}")
    private String apiKeyId;

    @Value("${jitsi.domain}")
    private String domain;

    @Value("${jitsi.secret}")
    private String secret;

    private static final long EXPIRATION_MS = 3 * 60 * 60 * 1000L;

    /**
     * Generates a JaaS-compatible RS256 JWT.
     *
     * @param roomName  the specific Jitsi room name
     * @param userId    the user's ID
     * @param userName  the user's display name
     * @param userEmail the user's email
     * @param userRole  ROLE_PROFESSOR / ROLE_ADMIN / etc.
     */
    public String generateToken(String roomName, String userId, String userName,
                                String userEmail, String userRole) {
        boolean isModerator = userRole != null &&
                (userRole.contains("PROFESSOR") || userRole.contains("ADMIN"));

        Map<String, Object> userCtx = new HashMap<>();
        userCtx.put("id", userId);
        userCtx.put("name", userName);
        userCtx.put("email", userEmail != null ? userEmail : "");
        userCtx.put("avatar", "");
        userCtx.put("moderator", isModerator);
        userCtx.put("hidden-from-recorder", false);

        Map<String, Object> features = new HashMap<>();
        features.put("livestreaming", false);
        features.put("file-upload", false);
        features.put("outbound-call", false);
        features.put("sip-outbound-call", false);
        features.put("transcription", false);
        features.put("list-visitors", false);
        features.put("recording", false);
        features.put("flip", false);

        Map<String, Object> context = new HashMap<>();
        context.put("user", userCtx);
        context.put("features", features);

        long now = System.currentTimeMillis();

        return Jwts.builder()
                .setHeaderParam("typ", "JWT")
                .claim("context", context)
                .claim("room", roomName)
                .setAudience(appId)
                .setIssuer(appId)
                .setSubject(appId)
                .setIssuedAt(new Date(now))
                .setNotBefore(new Date(now - 5_000))
                .setExpiration(new Date(now + EXPIRATION_MS))
                .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
                .compact();
    }
}
