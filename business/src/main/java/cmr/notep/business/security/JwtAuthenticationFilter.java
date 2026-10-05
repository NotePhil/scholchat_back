package cmr.notep.business.security;

import cmr.notep.business.exceptions.SchoolException;
import cmr.notep.business.exceptions.enums.SchoolErrorCode;
import cmr.notep.business.utils.JwtUtil;
import cmr.notep.modele.StatutVerificationProfesseur;
import cmr.notep.ressourcesjpa.repository.UtilisateursRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Authentifie la requête à partir de l'en-tête {@code Authorization: Bearer <jwt>}.
 *
 * Le filtre s'exécute sur TOUTES les routes (y compris publiques) : sans jeton, ou avec un
 * jeton invalide, la requête continue en anonyme et c'est la configuration d'autorisation
 * (SecurityConfig) qui décide — 401 via {@link RestAuthenticationEntryPoint} sur une route
 * protégée. La cause d'échec est exposée à l'entry point via l'attribut {@link #JWT_ERROR_ATTR}.
 *
 * Seuls les jetons d'accès sont acceptés (claim "roles" présent) et le compte doit être ACTIVE :
 * un jeton de réinitialisation de mot de passe, de renouvellement ou un refresh token ne vaut
 * pas authentification.
 *
 * Profil professeur : ROLE_PROFESSOR n'est accordé que si les pièces ont été validées par
 * l'administrateur (sinon ROLE_PROFESSOR_PENDING), et un professeur non validé qui agit en
 * professeur n'accède qu'à une liste blanche de routes — voir {@link ProfesseurVerificationService}.
 */
@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    public static final String JWT_ERROR_ATTR = "scholchat.jwt.error";

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final ProfesseurVerificationService professeurVerification;
    private final UtilisateursRepository utilisateursRepository;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService,
                                   ProfesseurVerificationService professeurVerification,
                                   UtilisateursRepository utilisateursRepository) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.professeurVerification = professeurVerification;
        this.utilisateursRepository = utilisateursRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")
                || SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7).trim();
        try {
            String userEmail = jwtUtil.getEmailFromToken(jwt); // lève TOKEN_EXPIRED / INVALID_TOKEN
            List<String> roles = jwtUtil.getRolesFromToken(jwt);
            if (userEmail == null || roles == null) {
                // Jeton signé mais qui n'est pas un jeton d'accès (reset, renouvellement, refresh…)
                request.setAttribute(JWT_ERROR_ATTR, "invalid");
            } else {
                UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);
                if (!userDetails.isEnabled()) {
                    request.setAttribute(JWT_ERROR_ATTR, "inactive");
                } else {
                    // Profil professeur : droits recalculés depuis la base à chaque requête
                    // (ROLE_PROFESSOR seulement si les pièces ont été validées par l'administrateur).
                    List<String> effectifs = roles;
                    String userId = null;
                    if (roles.contains(ProfesseurVerificationService.ROLE_PROFESSOR)
                            || roles.contains(ProfesseurVerificationService.ROLE_PROFESSOR_PENDING)) {
                        userId = utilisateursRepository.findIdByEmail(userEmail).orElse(null);
                        effectifs = professeurVerification.rolesEffectifs(userId, roles);
                    }
                    List<SimpleGrantedAuthority> authorities = effectifs.stream()
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    // Professeur non validé qui agit en professeur : seules les routes de la liste
                    // blanche (profil, pièces, notifications, authentification) sont accessibles.
                    if (ProfesseurVerificationService.agitEnProfesseurNonValide(effectifs, jwtUtil.getSelectedRoleFromToken(jwt))) {
                        StatutVerificationProfesseur statut = professeurVerification.statut(userId).orElse(null);
                        request.setAttribute(ProfesseurVerificationService.REQUEST_ATTR_STATUT_NON_VALIDE,
                                statut != null ? statut : StatutVerificationProfesseur.DOCUMENTS_MANQUANTS);
                        if (!ProfesseurVerificationService.estRouteAutorisee(request, userId)) {
                            log.info("Professeur non validé ({}) : accès refusé à {} {}", statut,
                                    request.getMethod(), request.getRequestURI());
                            RestAuthenticationEntryPoint.write(response, HttpServletResponse.SC_FORBIDDEN,
                                    SchoolErrorCode.PROFIL_PROFESSEUR_NON_VALIDE.name(),
                                    ProfesseurVerificationService.message(statut));
                            return;
                        }
                    }
                }
            }
        } catch (SchoolException e) {
            request.setAttribute(JWT_ERROR_ATTR,
                    e.getCode() == SchoolErrorCode.TOKEN_EXPIRED ? "expired" : "invalid");
            log.debug("JWT rejeté: {}", e.getMessage());
        } catch (Exception e) {
            // utilisateur supprimé, claim mal formé, etc.
            request.setAttribute(JWT_ERROR_ATTR, "invalid");
            log.debug("JWT rejeté: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
