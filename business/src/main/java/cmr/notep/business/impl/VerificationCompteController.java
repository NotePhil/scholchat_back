package cmr.notep.business.impl;

import cmr.notep.business.security.SimpleRateLimiter;
import cmr.notep.business.services.VerificationCompteService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Vérification du compte par code e-mail (routes publiques, bouton « Vérifier mon compte » de la page de connexion) :
 * <ul>
 *   <li>POST /auth/verification-compte/envoyer {email} → 200 {message} toujours (anti-énumération) ;</li>
 *   <li>POST /auth/verification-compte/verifier {email, code} → 200 {activationToken, email} ; le client continue
 *       comme avec le lien d'activation (POST /auth/registerPassword, Authorization: Bearer &lt;activationToken&gt;).</li>
 * </ul>
 */
@RestController
@RequestMapping("/auth/verification-compte")
public class VerificationCompteController {

    private final VerificationCompteService verificationCompteService;

    public VerificationCompteController(VerificationCompteService verificationCompteService) {
        this.verificationCompteService = verificationCompteService;
    }

    @PostMapping(path = "/envoyer", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> envoyer(@RequestBody Map<String, String> body, HttpServletRequest request) {
        verificationCompteService.envoyer(body == null ? null : body.get("email"), SimpleRateLimiter.ipClient(request));
        return Map.of("message", VerificationCompteService.MESSAGE_ENVOI);
    }

    @PostMapping(path = "/verifier", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> verifier(@RequestBody Map<String, String> body, HttpServletRequest request) {
        return verificationCompteService.verifier(body == null ? null : body.get("email"),
                body == null ? null : body.get("code"), SimpleRateLimiter.ipClient(request));
    }
}
