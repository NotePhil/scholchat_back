package cmr.notep.business.security;

import cmr.notep.ressourcesjpa.repository.ParentEleveRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;

import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Compte parent dont aucun enfant n'a encore été accepté dans une classe (inscription parent avec ses enfants :
 * le compte est créé ACTIF, mais reste limité tant que le professeur n'a validé aucun enfant).
 *
 * <p>Tant que c'est le cas, une session PARENT (profil choisi à la connexion = PARENT, ou jeton sans profil choisi
 * et sans autre profil utilisable) n'accède qu'à la liste blanche de {@link #estRouteAutorisee} ; tout le reste
 * reçoit un 403 {@code PARENT_SANS_ENFANT_VALIDE} (JwtAuthenticationFilter). Le contrôle est relu en base à chaque
 * requête (une seule requête SQL, mise en cache dans les attributs de la requête) : dès qu'un enfant est accepté,
 * le jeton déjà émis donne accès à toute l'application.</p>
 *
 * <p>Un compte multi-rôles n'est concerné que lorsqu'il agit en parent. Les comptes parent approuvés avec l'ancien
 * processus (accès du parent lui-même à une classe) ne sont pas bloqués (voir
 * ParentEleveRepository#parentAEnfantValide).</p>
 */
@Service
@RequiredArgsConstructor
public class ParentSansEnfantValide {

    public static final String MESSAGE = "Votre compte sera pleinement accessible dès qu'un de vos enfants aura été "
            + "accepté dans une classe.";

    private static final String ATTR_VALIDE = ParentSansEnfantValide.class.getName() + ".valide";
    private static final AntPathMatcher MATCHER = new AntPathMatcher();
    private static final Set<String> AUTRES_PROFILS = Set.of(
            "ROLE_STUDENT", "ROLE_PROFESSOR", "ROLE_PROFESSOR_PENDING", "ROLE_TUTOR", "ROLE_GESTIONNAIRE");

    private final ParentEleveRepository parentEleveRepository;

    /** L'appelant agit-il en tant que parent (et pas en administrateur) ? */
    public static boolean agitEnParent(List<String> roles, String profilChoisi) {
        if (roles == null || !roles.contains("ROLE_PARENT") || roles.contains("ROLE_ADMIN")) return false;
        if (profilChoisi != null && !profilChoisi.isBlank()) {
            return "PARENT".equals(profilChoisi.trim().toUpperCase(Locale.ROOT).replaceFirst("^ROLE_", ""));
        }
        return roles.stream().noneMatch(AUTRES_PROFILS::contains);
    }

    /** Au moins un enfant accepté dans une classe (résultat mis en cache pour la requête courante). */
    public boolean aEnfantValide(HttpServletRequest request, String parentId) {
        if (parentId == null) return false;
        Object cache = request != null ? request.getAttribute(ATTR_VALIDE) : null;
        if (cache instanceof Boolean b) return b;
        boolean valide = parentEleveRepository.parentAEnfantValide(parentId);
        if (request != null) request.setAttribute(ATTR_VALIDE, valide);
        return valide;
    }

    /**
     * Routes autorisées à un parent sans enfant validé : authentification (dont le changement de mot de passe),
     * son profil (lecture / mise à jour), ses notifications, ses enfants (liste, statuts, inscription d'un enfant),
     * les demandes d'accès pour SES enfants, et les routes publiques / techniques.
     */
    public boolean estRouteAutorisee(HttpServletRequest request, String userId) {
        String method = request.getMethod();
        String path = request.getServletPath();
        if (request.getPathInfo() != null) path = path + request.getPathInfo();
        if (path == null || path.isEmpty()) path = "/";

        if ("OPTIONS".equalsIgnoreCase(method)) return true;
        if (match(path, "/error", "/auth/**", "/ws/**", "/public/**", "/notifications", "/notifications/**",
                "/actuator/health", "/actuator/health/**")) {
            return true;
        }
        boolean get = "GET".equalsIgnoreCase(method);
        boolean post = "POST".equalsIgnoreCase(method);
        boolean patch = "PATCH".equalsIgnoreCase(method);

        // Son profil (lecture + mise à jour), ses profils
        if ((get || patch) && (estSoi(path, "/utilisateurs/{id}", userId) || estSoi(path, "/parents/{id}", userId))) {
            return true;
        }
        if (get && estSoi(path, "/utilisateurs/{id}/profils", userId)) return true;
        // Ses enfants : liste, statuts des inscriptions…
        if (get && (estSoi(path, "/parents/{id}/enfants", userId) || estSoi(path, "/parents/{id}/enfants/**", userId))) {
            return true;
        }
        // Inscription d'un nouvel enfant avec le code de sa classe
        if (post && estSoi(path, "/parents/{id}/enfants/inscription", userId)) return true;
        // Demande d'accès à une classe pour un de SES enfants
        if (post && MATCHER.match("/acceder/demandes", path)) {
            String demandeur = request.getParameter("utilisateurId");
            String enfant = request.getParameter("eleveAssocieId");
            return userId != null && userId.equals(demandeur) && "true".equalsIgnoreCase(request.getParameter("estParent"))
                    && enfant != null && !enfant.isBlank()
                    && parentEleveRepository.existsByParentIdAndEleveId(userId, enfant);
        }
        return false;
    }

    private static boolean estSoi(String path, String pattern, String userId) {
        if (!MATCHER.match(pattern, path)) return false;
        String id = MATCHER.extractUriTemplateVariables(pattern, path).get("id");
        return id != null && id.equals(userId);
    }

    private static boolean match(String path, String... patterns) {
        for (String p : patterns) {
            if (MATCHER.match(p, path)) return true;
        }
        return false;
    }
}
