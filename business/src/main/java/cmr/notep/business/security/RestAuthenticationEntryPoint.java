package cmr.notep.business.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Réponses JSON de la couche sécurité :
 * - 401 UNAUTHORIZED : jeton absent, invalide ou expiré (les clients traitent 401 comme fin de session) ;
 * - 403 FORBIDDEN    : utilisateur authentifié mais sans le droit requis.
 */
@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint, AccessDeniedHandler {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        Object cause = request.getAttribute(JwtAuthenticationFilter.JWT_ERROR_ATTR);
        String message;
        String code;
        if ("expired".equals(cause)) {
            code = "TOKEN_EXPIRED";
            message = "Votre session a expiré. Veuillez vous reconnecter.";
        } else if ("inactive".equals(cause)) {
            code = "INACTIVE_USER";
            message = "Votre compte n'est pas actif.";
        } else if ("invalid".equals(cause)) {
            code = "INVALID_TOKEN";
            message = "Jeton d'authentification invalide. Veuillez vous reconnecter.";
        } else {
            code = "UNAUTHORIZED";
            message = "Authentification requise. Veuillez vous connecter.";
        }
        write(response, HttpServletResponse.SC_UNAUTHORIZED, code, message);
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        if (professeurNonValide()) {
            // Route réservée aux professeurs, appelant dont le profil professeur n'est pas encore validé
            write(response, HttpServletResponse.SC_FORBIDDEN, "PROFIL_PROFESSEUR_NON_VALIDE",
                    ProfesseurVerificationService.message(
                            ProfesseurVerificationService.statutNonValideRequeteCourante().orElse(null)));
            return;
        }
        write(response, HttpServletResponse.SC_FORBIDDEN, "FORBIDDEN",
                "Vous n'avez pas les droits nécessaires pour effectuer cette action.");
    }

    /** L'appelant possède un profil professeur pas encore validé (ROLE_PROFESSOR_PENDING). */
    static boolean professeurNonValide() {
        org.springframework.security.core.Authentication auth =
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> ProfesseurVerificationService.ROLE_PROFESSOR_PENDING.equals(a.getAuthority()));
    }

    static void write(HttpServletResponse response, int status, String code, String message) throws IOException {
        if (response.isCommitted()) return;
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"timestamp\":\"" + LocalDateTime.now() + "\",\"code\":\"" + code
                + "\",\"message\":\"" + message.replace("\"", "\\\"") + "\"}");
    }
}
