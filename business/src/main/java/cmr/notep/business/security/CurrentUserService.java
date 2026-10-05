package cmr.notep.business.security;

import cmr.notep.business.exceptions.SchoolException;
import cmr.notep.business.exceptions.enums.SchoolErrorCode;
import cmr.notep.ressourcesjpa.dao.UtilisateursEntity;
import cmr.notep.ressourcesjpa.repository.UtilisateursRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.Optional;

/**
 * Identité de l'appelant courant (issue du JWT, voir {@link JwtAuthenticationFilter}).
 *
 * Le principal Spring porte l'EMAIL de l'utilisateur (sujet du JWT) ; l'id applicatif est
 * résolu en base et mis en cache pour la durée de la requête HTTP.
 * Les rôles viennent des autorités du jeton (ROLE_ADMIN, ROLE_PROFESSOR, ...).
 */
@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private static final String CACHE_ATTR = CurrentUserService.class.getName() + ".user";

    private final UtilisateursRepository utilisateursRepository;

    public Optional<Authentication> authentication() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }
        return Optional.of(auth);
    }

    public boolean isAuthenticated() {
        return authentication().isPresent();
    }

    public Optional<String> currentEmail() {
        return authentication().map(Authentication::getName);
    }

    /** L'utilisateur connecté, ou vide si l'appel est anonyme. */
    public Optional<UtilisateursEntity> currentUser() {
        Optional<String> email = currentEmail();
        if (email.isEmpty()) {
            return Optional.empty();
        }
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            Object cached = attrs.getAttribute(CACHE_ATTR, RequestAttributes.SCOPE_REQUEST);
            if (cached instanceof UtilisateursEntity u && email.get().equalsIgnoreCase(u.getEmail())) {
                return Optional.of(u);
            }
        }
        Optional<UtilisateursEntity> user = utilisateursRepository.findByEmail(email.get());
        if (attrs != null) {
            user.ifPresent(u -> attrs.setAttribute(CACHE_ATTR, u, RequestAttributes.SCOPE_REQUEST));
        }
        return user;
    }

    private static final String ID_CACHE_ATTR = CurrentUserService.class.getName() + ".userId";

    /**
     * Id de l'appelant, résolu par une requête scalaire : l'entité n'est PAS chargée dans le
     * contexte de persistance (open-in-view). Pour un compte multi-rôles (ex. professeur ET parent),
     * charger l'utilisateur ici figeait son sous-type pour toute la requête, et un
     * ParentsRepository.findById(id) ultérieur renvoyait vide ("Parent introuvable").
     */
    public Optional<String> currentUserIdOpt() {
        Optional<String> email = currentEmail();
        if (email.isEmpty()) {
            return Optional.empty();
        }
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            Object cached = attrs.getAttribute(ID_CACHE_ATTR, RequestAttributes.SCOPE_REQUEST);
            if (cached instanceof String[] pair && pair.length == 2 && email.get().equalsIgnoreCase(pair[0])) {
                return Optional.of(pair[1]);
            }
        }
        Optional<String> id = utilisateursRepository.findIdByEmail(email.get());
        if (attrs != null) {
            id.ifPresent(v -> attrs.setAttribute(ID_CACHE_ATTR, new String[]{email.get(), v}, RequestAttributes.SCOPE_REQUEST));
        }
        return id;
    }

    /** Id de l'utilisateur connecté ; 401 si l'appel est anonyme. */
    public String requireUserId() {
        return currentUserIdOpt().orElseThrow(() -> new SchoolException(SchoolErrorCode.UNAUTHORIZED,
                "Authentification requise. Veuillez vous connecter."));
    }

    public UtilisateursEntity requireUser() {
        return currentUser().orElseThrow(() -> new SchoolException(SchoolErrorCode.UNAUTHORIZED,
                "Authentification requise. Veuillez vous connecter."));
    }

    public boolean hasRole(String role) {
        String wanted = role.startsWith("ROLE_") ? role : "ROLE_" + role;
        return authentication()
                .map(a -> a.getAuthorities().stream().map(GrantedAuthority::getAuthority).anyMatch(wanted::equals))
                .orElse(false);
    }

    public boolean isAdmin() {
        return hasRole("ADMIN");
    }

    public boolean isSelf(String userId) {
        return userId != null && currentUserIdOpt().map(userId::equals).orElse(false);
    }

    public void requireAuthenticated() {
        requireUserId();
    }

    public void requireAdmin() {
        requireAuthenticated();
        if (!isAdmin()) {
            throw forbidden("Action réservée aux administrateurs.");
        }
    }

    /** Autorise l'utilisateur lui-même ou un administrateur. */
    public void requireSelfOrAdmin(String userId) {
        requireAuthenticated();
        if (!isAdmin() && !isSelf(userId)) {
            throw forbidden("Vous ne pouvez accéder qu'à vos propres données.");
        }
    }

    public static SchoolException forbidden(String message) {
        return new SchoolException(SchoolErrorCode.OPERATION_INTERDITE, message);
    }
}
