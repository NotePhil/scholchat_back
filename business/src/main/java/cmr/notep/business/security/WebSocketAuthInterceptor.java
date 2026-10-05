package cmr.notep.business.security;

import cmr.notep.business.utils.JwtUtil;
import cmr.notep.ressourcesjpa.dao.UtilisateursEntity;
import cmr.notep.ressourcesjpa.repository.UtilisateursRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Sécurité STOMP, appliquée aux deux points d'entrée (/ws en SockJS pour le web, /ws/native en
 * WebSocket brut pour le mobile).
 *
 * <ul>
 *   <li>CONNECT : en-tête natif {@code Authorization: Bearer <jwt d'accès>} OBLIGATOIRE, compte ACTIVE,
 *       sinon la connexion est refusée (trame ERROR). Le contrôle d'origine n'a pas de sens pour une
 *       application native : c'est ce jeton qui protège /ws/native.</li>
 *   <li>SUBSCRIBE : uniquement
 *       {@code /topic/messages/{sonId}}, {@code /topic/notifications/{sonId}} (propriétaire seulement),
 *       {@code /topic/cours/{coursId}/...} (utilisateurs ayant accès au cours, cf.
 *       AccessControlService#canAccessCours) et les destinations {@code /user/...} (par nature privées).</li>
 *   <li>SEND : uniquement {@code /app/cours/{coursId}/...} pour les ayants droit du cours ; il est
 *       interdit de publier directement sur {@code /topic/...} (usurpation de messages).</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private static final String USER_ID_ATTR = "scholchat.userId";
    private static final Pattern USER_TOPIC = Pattern.compile("^/topic/(messages|notifications)/([^/]+)$");
    private static final Pattern COURS_TOPIC = Pattern.compile("^/topic/cours/([^/]+)(/.*)?$");
    private static final Pattern COURS_APP = Pattern.compile("^/app/cours/([^/]+)(/.*)?$");

    private final JwtUtil jwtUtil;
    private final UtilisateursRepository utilisateursRepository;
    private final AccessControlService accessControl;
    private final ProfesseurVerificationService professeurVerification;

    private static final String PROF_NON_VALIDE_ATTR = "scholchat.professeurNonValide";

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null || accessor.getCommand() == null) {
            return message;
        }
        StompCommand command = accessor.getCommand();
        switch (command) {
            case CONNECT, STOMP -> authenticate(accessor);
            case SUBSCRIBE -> authorizeSubscribe(accessor);
            case SEND -> authorizeSend(accessor);
            default -> { /* DISCONNECT, UNSUBSCRIBE, ACK… */ }
        }
        return message;
    }

    // ─── CONNECT ──────────────────────────────────────────────────────────────

    private void authenticate(StompHeaderAccessor accessor) {
        String header = accessor.getFirstNativeHeader("Authorization");
        if (header == null) header = accessor.getFirstNativeHeader("authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            throw new MessageDeliveryException("Authentification requise (en-tête Authorization: Bearer <jeton>)");
        }
        String token = header.substring(7).trim();
        String email;
        List<String> roles;
        try {
            email = jwtUtil.getEmailFromToken(token);
            roles = jwtUtil.getRolesFromToken(token);
        } catch (Exception e) {
            throw new MessageDeliveryException("Jeton invalide ou expiré");
        }
        if (email == null || roles == null) {
            throw new MessageDeliveryException("Jeton invalide");
        }
        UtilisateursEntity user = utilisateursRepository.findByEmail(email)
                .orElseThrow(() -> new MessageDeliveryException("Utilisateur introuvable"));
        if (user.getEtat() != cmr.notep.modele.EtatUtilisateur.ACTIVE) {
            throw new MessageDeliveryException("Compte inactif");
        }
        // Profil professeur : ROLE_PROFESSOR seulement si les pièces ont été validées (cf. JwtAuthenticationFilter)
        List<String> effectifs = professeurVerification.rolesEffectifs(user.getId(), roles);
        boolean profNonValide = ProfesseurVerificationService.agitEnProfesseurNonValide(
                effectifs, jwtUtil.getSelectedRoleFromToken(token));
        List<SimpleGrantedAuthority> authorities = effectifs.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        accessor.setUser(new UsernamePasswordAuthenticationToken(email, null, authorities));
        Map<String, Object> attrs = accessor.getSessionAttributes();
        if (attrs != null) {
            attrs.put(USER_ID_ATTR, user.getId());
            attrs.put(PROF_NON_VALIDE_ATTR, profNonValide);
        }
    }

    // ─── SUBSCRIBE / SEND ─────────────────────────────────────────────────────

    private void authorizeSubscribe(StompHeaderAccessor accessor) {
        String userId = requireUserId(accessor);
        String destination = accessor.getDestination();
        if (destination == null) {
            throw new MessageDeliveryException("Destination manquante");
        }
        if (destination.startsWith("/user/")) {
            return; // destinations utilisateur : résolues par Spring sur la session courante
        }
        Matcher m = USER_TOPIC.matcher(destination);
        if (m.matches()) {
            if (!userId.equals(m.group(2)) ) {
                log.warn("WS: abonnement refusé à {} pour l'utilisateur {}", destination, userId);
                throw new MessageDeliveryException("Abonnement non autorisé");
            }
            return;
        }
        m = COURS_TOPIC.matcher(destination);
        if (m.matches()) {
            requireCoursAccess(accessor, userId, m.group(1), destination);
            return;
        }
        log.warn("WS: abonnement refusé à une destination inconnue {} (utilisateur {})", destination, userId);
        throw new MessageDeliveryException("Abonnement non autorisé");
    }

    private void authorizeSend(StompHeaderAccessor accessor) {
        String userId = requireUserId(accessor);
        String destination = accessor.getDestination();
        Matcher m = destination == null ? null : COURS_APP.matcher(destination);
        if (m != null && m.matches()) {
            requireCoursAccess(accessor, userId, m.group(1), destination);
            return;
        }
        log.warn("WS: envoi refusé vers {} (utilisateur {})", destination, userId);
        throw new MessageDeliveryException("Envoi non autorisé");
    }

    private void requireCoursAccess(StompHeaderAccessor accessor, String userId, String coursId, String destination) {
        if (isAdmin(accessor.getUser())) return;
        // Professeur non validé connecté en tant que professeur : aucune session de cours
        // (ses notifications et messages privés restent accessibles).
        Map<String, Object> attrs = accessor.getSessionAttributes();
        if (attrs != null && Boolean.TRUE.equals(attrs.get(PROF_NON_VALIDE_ATTR))) {
            log.warn("WS: professeur non validé, accès refusé à {} (utilisateur {})", destination, userId);
            throw new MessageDeliveryException(ProfesseurVerificationService.message(null));
        }
        if (!accessControl.canAccessCours(coursId, userId)) {
            log.warn("WS: accès refusé à {} pour l'utilisateur {}", destination, userId);
            throw new MessageDeliveryException("Accès au cours non autorisé");
        }
    }

    private String requireUserId(StompHeaderAccessor accessor) {
        Principal principal = accessor.getUser();
        if (principal == null) {
            throw new MessageDeliveryException("Authentification requise");
        }
        Map<String, Object> attrs = accessor.getSessionAttributes();
        Object cached = attrs != null ? attrs.get(USER_ID_ATTR) : null;
        if (cached instanceof String id) {
            return id;
        }
        String id = utilisateursRepository.findByEmail(principal.getName())
                .map(UtilisateursEntity::getId)
                .orElseThrow(() -> new MessageDeliveryException("Utilisateur introuvable"));
        if (attrs != null) attrs.put(USER_ID_ATTR, id);
        return id;
    }

    private static boolean isAdmin(Principal principal) {
        if (principal instanceof UsernamePasswordAuthenticationToken auth) {
            for (GrantedAuthority a : auth.getAuthorities()) {
                if ("ROLE_ADMIN".equals(a.getAuthority())) return true;
            }
        }
        return false;
    }
}
