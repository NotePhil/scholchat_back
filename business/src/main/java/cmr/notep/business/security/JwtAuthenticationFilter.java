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
 * Seuls les jetons d'accès sont acceptés (JwtUtil#isAccessToken : claim "roles" + type "access") et le compte
 * doit être ACTIVE : un jeton d'activation (lien ou code de vérification), de réinitialisation de mot de passe,
 * de renouvellement, de dépôt de pièces ou un refresh token ne vaut pas authentification.
 *
 * Mot de passe temporaire (must_change_password) : 403 MOT_DE_PASSE_A_CHANGER hors de la liste blanche
 * de {@link MotDePasseAChanger}.
 *
 * Parent sans enfant accepté dans une classe (session PARENT) : 403 PARENT_SANS_ENFANT_VALIDE hors de la liste
 * blanche de {@link ParentSansEnfantValide}.
 *
 * Profil professeur : ROLE_PROFESSOR n'est accordé que si les pièces ont été validées par
 * l'administrateur (sinon ROLE_PROFESSOR_PENDING), et un professeur non validé qui agit en
 * professeur n'accède qu'à une liste blanche de routes — voir {@link ProfesseurVerificationService}.
 */
@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    public static final String JWT_ERROR_ATTR = "scholchat.jwt.error";
    /** Profil choisi pour la session (claim selectedRole du jeton d'accès), voir CurrentUserService#sessionRole. */
    public static final String SESSION_ROLE_ATTR = "scholchat.jwt.selectedRole";

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final ProfesseurVerificationService professeurVerification;
    private final UtilisateursRepository utilisateursRepository;
    private final ParentSansEnfantValide parentSansEnfantValide;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService,
                                   ProfesseurVerificationService professeurVerification,
                                   UtilisateursRepository utilisateursRepository,
                                   ParentSansEnfantValide parentSansEnfantValide) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.professeurVerification = professeurVerification;
        this.utilisateursRepository = utilisateursRepository;
        this.parentSansEnfantValide = parentSansEnfantValide;
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
            if (userEmail == null || roles == null || !jwtUtil.isAccessToken(jwt)) {
                // Jeton signé mais qui n'est pas un jeton d'accès (activation, reset, renouvellement, refresh…)
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
                    String sessionRole = jwtUtil.getSelectedRoleFromToken(jwt);
                    if (sessionRole != null) {
                        request.setAttribute(SESSION_ROLE_ATTR, sessionRole);
                    }

                    // Mot de passe temporaire pas encore remplacé : seules quelques routes passent
                    // (changement de mot de passe, son profil, session…) — relu en base à chaque requête.
                    if (utilisateursRepository.findMustChangePasswordByEmail(userEmail)) {
                        if (userId == null) {
                            userId = utilisateursRepository.findIdByEmail(userEmail).orElse(null);
                        }
                        if (!MotDePasseAChanger.estRouteAutorisee(request, userId)) {
                            log.info("Mot de passe temporaire à changer : accès refusé à {} {}",
                                    request.getMethod(), request.getRequestURI());
                            RestAuthenticationEntryPoint.write(response, HttpServletResponse.SC_FORBIDDEN,
                                    SchoolErrorCode.MOT_DE_PASSE_A_CHANGER.name(), MotDePasseAChanger.MESSAGE);
                            return;
                        }
                    }

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

                    // Session PARENT sans aucun enfant accepté dans une classe : liste blanche seulement
                    // (profil, notifications, enfants, inscription / demande d'accès pour ses enfants…).
                    if (ParentSansEnfantValide.agitEnParent(effectifs, sessionRole)) {
                        if (userId == null) {
                            userId = utilisateursRepository.findIdByEmail(userEmail).orElse(null);
                        }
                        if (!parentSansEnfantValide.estRouteAutorisee(request, userId)
                                && !parentSansEnfantValide.aEnfantValide(request, userId)) {
                            log.info("Parent sans enfant validé : accès refusé à {} {}",
                                    request.getMethod(), request.getRequestURI());
                            RestAuthenticationEntryPoint.write(response, HttpServletResponse.SC_FORBIDDEN,
                                    SchoolErrorCode.PARENT_SANS_ENFANT_VALIDE.name(), ParentSansEnfantValide.MESSAGE);
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
