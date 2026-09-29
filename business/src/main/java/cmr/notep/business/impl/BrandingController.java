package cmr.notep.business.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Serves the JSON consumed by the self-hosted Jitsi deployment's
 * DYNAMIC_BRANDING_URL (docker-jitsi-meet), so the meeting UI shows the
 * ScholChat logo instead of the default Jitsi watermark. Schema is Jitsi's
 * own dynamic-branding contract (logoImageUrl / logoClickUrl / backgroundColor),
 * fetched client-side by the jitsi-meet web app itself — must stay public
 * and openly CORS-enabled since it's requested from the Jitsi domain, not
 * ours (see CorsConfig's dedicated rule for this exact path).
 */
@RestController
@RequestMapping("/public")
public class BrandingController {

    @Value("${front.endpoint}")
    private String frontEndpoint;

    @GetMapping("/jitsi-branding")
    public Map<String, Object> jitsiBranding() {
        String base = frontEndpoint.endsWith("/")
                ? frontEndpoint.substring(0, frontEndpoint.length() - 1)
                : frontEndpoint;

        Map<String, Object> branding = new LinkedHashMap<>();
        branding.put("logoImageUrl", base + "/scholchat.png");
        branding.put("logoClickUrl", base);
        branding.put("backgroundColor", "#4f46e5");
        return branding;
    }
}
