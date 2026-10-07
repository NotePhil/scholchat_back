package cmr.notep.business.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.AntPathMatcher;

/**
 * Compte connecté avec un mot de passe temporaire (utilisateurs.must_change_password = true, voir
 * InscriptionClasseService) : tant qu'il n'a pas choisi de nouveau mot de passe, seules les routes ci-dessous
 * sont accessibles ; tout le reste reçoit un 403 {@code MOT_DE_PASSE_A_CHANGER} (JwtAuthenticationFilter).
 * Le drapeau est relu en base à chaque requête : dès POST /auth/change-password réussi, le jeton déjà émis
 * donne accès à toute l'application (pas besoin d'un nouveau jeton).
 */
public final class MotDePasseAChanger {

    public static final String MESSAGE = "Vous vous êtes connecté avec un mot de passe temporaire : choisissez un "
            + "nouveau mot de passe pour continuer à utiliser ScholChat.";

    private static final AntPathMatcher MATCHER = new AntPathMatcher();

    private MotDePasseAChanger() {
    }

    /**
     * Routes autorisées : authentification (changement de mot de passe, de profil, connexion…), lecture de SON
     * profil, lecture de ses notifications (compteur), et les routes techniques/publiques.
     */
    public static boolean estRouteAutorisee(HttpServletRequest request, String userId) {
        String method = request.getMethod();
        String path = request.getServletPath();
        if (request.getPathInfo() != null) path = path + request.getPathInfo();
        if (path == null || path.isEmpty()) path = "/";

        if ("OPTIONS".equalsIgnoreCase(method)) return true;
        if (match(path, "/error", "/auth/**", "/ws/**", "/public/**", "/actuator/health", "/actuator/health/**")) {
            return true;
        }
        boolean get = "GET".equalsIgnoreCase(method);
        if (get && match(path, "/notifications", "/notifications/**")) return true;
        if (get && MATCHER.match("/utilisateurs/{id}", path)) {
            String id = MATCHER.extractUriTemplateVariables("/utilisateurs/{id}", path).get("id");
            return id != null && id.equals(userId);
        }
        if (get && MATCHER.match("/utilisateurs/{id}/profils", path)) {
            String id = MATCHER.extractUriTemplateVariables("/utilisateurs/{id}/profils", path).get("id");
            return id != null && id.equals(userId);
        }
        return false;
    }

    private static boolean match(String path, String... patterns) {
        for (String p : patterns) {
            if (MATCHER.match(p, path)) return true;
        }
        return false;
    }
}
