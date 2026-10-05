package cmr.notep.business.exceptions;

import cmr.notep.business.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Les refus de @PreAuthorize sont levés DANS le contrôleur : sans ce handler prioritaire,
 * le handler générique Exception.class (MediaExceptionHandler) les transformait en 500.
 * 401 si l'appel est anonyme, 403 sinon.
 */
@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class SecurityExceptionHandler {

    private final CurrentUserService currentUserService;

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDenied(AccessDeniedException ex) {
        if (!currentUserService.isAuthenticated()) {
            return body(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Authentification requise. Veuillez vous connecter.");
        }
        if (currentUserService.hasRole(cmr.notep.business.security.ProfesseurVerificationService.ROLE_PROFESSOR_PENDING)) {
            return body(HttpStatus.FORBIDDEN, "PROFIL_PROFESSEUR_NON_VALIDE",
                    cmr.notep.business.security.ProfesseurVerificationService.message(
                            cmr.notep.business.security.ProfesseurVerificationService.statutNonValideRequeteCourante().orElse(null)));
        }
        return body(HttpStatus.FORBIDDEN, "FORBIDDEN", "Vous n'avez pas les droits nécessaires pour effectuer cette action.");
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Object> handleAuthentication(AuthenticationException ex) {
        return body(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Authentification requise. Veuillez vous connecter.");
    }

    private static ResponseEntity<Object> body(HttpStatus status, String code, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("code", code);
        body.put("message", message);
        return new ResponseEntity<>(body, status);
    }
}
