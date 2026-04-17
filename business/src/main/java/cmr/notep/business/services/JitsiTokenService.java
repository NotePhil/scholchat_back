package cmr.notep.business.services;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class JitsiTokenService {

    // vpaas-magic-cookie-b0d7354d679944a197a3e35a0d1e2c60
    @Value("${jitsi.app.id}")
    private String appId;

    // vpaas-magic-cookie-b0d7354d679944a197a3e35a0d1e2c60/a7454f
    @Value("${jitsi.api.key.id}")
    private String apiKeyId;

    // meet.jit.si or 8x8.vc
    @Value("${jitsi.domain}")
    private String domain;

    @Value("${jitsi.private.key.path:jitsi/jaas-private-key.pk}")
    private String privateKeyPath;

    private static final long EXPIRATION_MS = 3 * 60 * 60 * 1000L; // 3 hours

    private PrivateKey privateKey;

    @PostConstruct
    public void init() {
        try {
            ClassPathResource resource = new ClassPathResource(privateKeyPath);
            String pem = new String(resource.getInputStream().readAllBytes())
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s+", "");

            byte[] keyBytes = Base64.getDecoder().decode(pem);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            privateKey = KeyFactory.getInstance("RSA").generatePrivate(spec);
            log.info("JaaS RSA private key loaded successfully");
        } catch (Exception e) {
            log.error("Failed to load JaaS private key: {}", e.getMessage());
            throw new IllegalStateException("Cannot initialize JitsiTokenService", e);
        }
    }

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

        // The room claim must be "*" (wildcard) — JaaS validates room access
        // via the roomName in the API call, not via the JWT room claim.
        // Using a specific room name in the claim causes auth failures.
        return Jwts.builder()
                .setHeaderParam("kid", appId + "/" + apiKeyId)
                .setHeaderParam("typ", "JWT")
                .claim("context", context)
                .claim("room", "*")
                .setAudience("jitsi")
                .setIssuer("chat")
                .setSubject(appId)
                .setIssuedAt(new Date(now))
                .setNotBefore(new Date(now - 5_000))
                .setExpiration(new Date(now + EXPIRATION_MS))
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
    }
}
