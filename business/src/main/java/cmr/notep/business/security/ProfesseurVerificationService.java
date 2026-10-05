package cmr.notep.business.security;

import cmr.notep.business.exceptions.SchoolException;
import cmr.notep.business.exceptions.enums.SchoolErrorCode;
import cmr.notep.interfaces.modeles.Professeurs;
import cmr.notep.interfaces.modeles.Utilisateurs;
import cmr.notep.modele.StatutVerificationProfesseur;
import cmr.notep.ressourcesjpa.repository.UtilisateursRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/**
 * Vérification du PROFIL professeur (pièces d'identité validées par l'administrateur), distincte de
 * l'état du compte : un professeur peut être connecté (compte ACTIVE, activation "partielle") sans
 * avoir les droits professeur.
 *
 * <h3>Mécanisme central</h3>
 * <ul>
 *   <li>Autorités : à CHAQUE requête, {@link JwtAuthenticationFilter} remplace ROLE_PROFESSOR par
 *       ROLE_PROFESSOR_PENDING (et inversement) selon le statut en base ({@link #rolesEffectifs}) :
 *       un jeton émis avant un refus ne garde pas les droits, et un professeur validé les obtient
 *       sans attendre l'expiration de son jeton. Tous les contrôles existants sur ROLE_PROFESSOR
 *       (SecurityConfig, @PreAuthorize, CurrentUserService#hasRole) refusent donc automatiquement.</li>
 *   <li>Liste blanche : quand l'appelant AGIT en tant que professeur non validé (profil choisi à la
 *       connexion = PROFESSOR, ou pas d'autre profil), seules les routes de {@link #estRouteAutorisee}
 *       passent (profil, pièces, notifications, authentification) ; tout le reste reçoit un 403
 *       PROFIL_PROFESSEUR_NON_VALIDE, y compris les routes qui ne contrôlent que l'authentification
 *       ou la propriété. Un compte multi-rôles qui agit en parent/élève n'est pas concerné (il perd
 *       seulement ROLE_PROFESSOR).</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class ProfesseurVerificationService {

    public static final String ROLE_PROFESSOR = "ROLE_PROFESSOR";
    public static final String ROLE_PROFESSOR_PENDING = "ROLE_PROFESSOR_PENDING";

    /** Attribut de requête posé par le filtre JWT : statut du professeur non validé qui agit en professeur. */
    public static final String REQUEST_ATTR_STATUT_NON_VALIDE = ProfesseurVerificationService.class.getName() + ".statut";

    /** Types de pièces qu'un professeur non validé peut déposer (chemin users/{id}/{mediaType}/{documentType}/…). */
    public static final Set<String> TYPES_PIECES_PROFIL = Set.of("cni-recto", "cni-verso", "selfie");

    /** Profils "hors professeur" : un compte qui en possède un peut agir sous ce profil. */
    private static final Set<String> AUTRES_PROFILS = Set.of(
            "ROLE_STUDENT", "ROLE_PARENT", "ROLE_TUTOR", "ROLE_GESTIONNAIRE", "ROLE_ADMIN");

    private static final AntPathMatcher MATCHER = new AntPathMatcher();

    private final UtilisateursRepository utilisateursRepository;

    // ─── Statut ───────────────────────────────────────────────────────────────

    /** Statut du profil professeur, vide si le compte n'a pas de profil professeur. */
    public Optional<StatutVerificationProfesseur> statut(String userId) {
        if (userId == null) return Optional.empty();
        return utilisateursRepository.findStatutVerificationProfesseur(userId)
                .map(StatutVerificationProfesseur::parse);
    }

    /** Droits professeur effectifs : profil VALIDE et rôle PROFESSOR actif. */
    public boolean isValide(String userId) {
        return userId != null && utilisateursRepository.isProfesseurValide(userId);
    }

    public String motifRejet(String userId) {
        return userId == null ? null : utilisateursRepository.findMotifRejetVerificationProfesseur(userId).orElse(null);
    }

    /** Renseigne le statut (lecture seule) sur la vue professeur renvoyée aux clients. */
    public <T extends Utilisateurs> T enrichir(T utilisateur) {
        if (utilisateur instanceof Professeurs p && p.getId() != null) {
            statut(p.getId()).ifPresent(s -> {
                p.setStatutVerification(s.name());
                p.setMotifRejetVerification(s == StatutVerificationProfesseur.REJETE ? motifRejet(p.getId()) : null);
            });
        }
        return utilisateur;
    }

    public static String message(StatutVerificationProfesseur statut) {
        if (statut == null) {
            return "Votre profil professeur n'est pas encore validé par l'administrateur.";
        }
        return switch (statut) {
            case DOCUMENTS_MANQUANTS -> "Votre profil professeur n'est pas encore validé : déposez votre CNI (recto et verso) "
                    + "et un selfie depuis votre profil. L'administrateur doit les valider avant que vous puissiez "
                    + "utiliser les fonctionnalités professeur.";
            case EN_ATTENTE_VALIDATION -> "Votre profil professeur est en attente de validation par l'administrateur. "
                    + "Vous pourrez utiliser les fonctionnalités professeur dès que vos pièces auront été validées.";
            case REJETE -> "Vos pièces justificatives ont été refusées par l'administrateur. Déposez de nouvelles pièces "
                    + "depuis votre profil pour qu'elles soient à nouveau examinées.";
            case VALIDE -> "Votre profil professeur est validé.";
        };
    }

    public static SchoolException nonValide(StatutVerificationProfesseur statut) {
        return new SchoolException(SchoolErrorCode.PROFIL_PROFESSEUR_NON_VALIDE, message(statut));
    }

    // ─── Autorités ────────────────────────────────────────────────────────────

    /**
     * Autorités effectives : ROLE_PROFESSOR seulement si le profil est VALIDE, sinon ROLE_PROFESSOR_PENDING.
     * Les autres rôles du jeton sont conservés.
     */
    public List<String> rolesEffectifs(String userId, List<String> rolesJeton) {
        boolean aProfil = rolesJeton.contains(ROLE_PROFESSOR) || rolesJeton.contains(ROLE_PROFESSOR_PENDING);
        if (!aProfil) return rolesJeton;
        boolean valide = isValide(userId);
        List<String> roles = new ArrayList<>();
        for (String r : rolesJeton) {
            if (!ROLE_PROFESSOR.equals(r) && !ROLE_PROFESSOR_PENDING.equals(r)) roles.add(r);
        }
        roles.add(valide ? ROLE_PROFESSOR : ROLE_PROFESSOR_PENDING);
        return roles;
    }

    /**
     * L'appelant agit-il en tant que professeur non validé ? Profil choisi à la connexion = PROFESSOR,
     * ou (jeton sans profil choisi) aucun autre profil utilisable.
     */
    public static boolean agitEnProfesseurNonValide(List<String> rolesEffectifs, String profilChoisi) {
        if (!rolesEffectifs.contains(ROLE_PROFESSOR_PENDING)) return false;
        if (rolesEffectifs.contains("ROLE_ADMIN")) return false;
        if (profilChoisi != null && !profilChoisi.isBlank()) {
            String p = profilChoisi.trim().toUpperCase(Locale.ROOT).replaceFirst("^ROLE_", "");
            return "PROFESSOR".equals(p) || "PROFESSEUR".equals(p);
        }
        return rolesEffectifs.stream().noneMatch(AUTRES_PROFILS::contains);
    }

    /**
     * Routes autorisées à un professeur non validé qui agit en professeur : authentification
     * (changement de mot de passe / de profil), lecture et mise à jour de SON profil et de ses pièces,
     * ses notifications, la consultation des motifs de rejet, et les routes publiques.
     */
    public static boolean estRouteAutorisee(HttpServletRequest request, String userId) {
        String method = request.getMethod();
        String path = request.getServletPath();
        if (request.getPathInfo() != null) path = path + request.getPathInfo();
        if (path == null || path.isEmpty()) path = "/";

        if ("OPTIONS".equalsIgnoreCase(method)) return true;
        if (match(path, "/error", "/auth/**", "/ws/**", "/notifications", "/notifications/**",
                "/public/**", "/actuator/health", "/actuator/health/**")) {
            return true;
        }
        boolean get = "GET".equalsIgnoreCase(method);
        boolean post = "POST".equalsIgnoreCase(method);
        boolean patch = "PATCH".equalsIgnoreCase(method);

        // Son propre profil (lecture + mise à jour, dont le dépôt des pièces)
        if ((get || patch) && MATCHER.match("/utilisateurs/{id}", path)) {
            String id = MATCHER.extractUriTemplateVariables("/utilisateurs/{id}", path).get("id");
            return id != null && id.equals(userId);
        }
        // Inscription (ajout d'un autre profil avec le même e-mail), renvoi du lien d'activation
        if (post && match(path, "/utilisateurs", "/utilisateurs/regenerate-activation")) return true;
        // Dépôt des pièces : le type de pièce est contrôlé dans MediaServiceImpl
        if (post && match(path, "/media/presigned-url", "/media/proxy-upload")) return true;
        // Aperçu de ses pièces
        if (get && match(path, "/media/download-by-path", "/media/*/download-url", "/media/*/download",
                "/media/*/content")) {
            return true;
        }
        // Libellés des motifs de rejet, catalogue des offres (publics/non sensibles)
        return get && match(path, "/motifsRejets", "/motifsRejets/**", "/offres", "/offres/*");
    }

    private static boolean match(String path, String... patterns) {
        for (String p : patterns) {
            if (MATCHER.match(p, path)) return true;
        }
        return false;
    }

    /** Statut du professeur non validé qui agit en professeur dans la requête courante (posé par le filtre JWT). */
    public static Optional<StatutVerificationProfesseur> statutNonValideRequeteCourante() {
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        if (attrs == null) return Optional.empty();
        Object v = attrs.getAttribute(REQUEST_ATTR_STATUT_NON_VALIDE, RequestAttributes.SCOPE_REQUEST);
        if (v instanceof StatutVerificationProfesseur s) return Optional.of(s);
        if (v instanceof String s) return Optional.ofNullable(StatutVerificationProfesseur.parse(s));
        return Optional.empty();
    }

    /**
     * Dépôt de fichier par un professeur non validé : seules ses pièces justificatives sont acceptées
     * (pas de média de cours, d'exercice, de message…).
     */
    public static void requireTypePieceProfilSiNonValide(String documentTypeOuChemin) {
        Optional<StatutVerificationProfesseur> statut = statutNonValideRequeteCourante();
        if (statut.isEmpty()) return;
        String v = documentTypeOuChemin == null ? "" : documentTypeOuChemin.toLowerCase(Locale.ROOT);
        boolean piece = TYPES_PIECES_PROFIL.contains(v)
                || TYPES_PIECES_PROFIL.stream().anyMatch(t -> v.contains("/" + t + "/"));
        if (!piece) {
            throw nonValide(statut.get());
        }
    }
}
