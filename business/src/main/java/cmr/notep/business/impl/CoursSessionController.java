package cmr.notep.business.impl;

import cmr.notep.business.business.CoursSessionBusiness;
import cmr.notep.interfaces.dto.ChapitreProgressDTO;
import cmr.notep.interfaces.dto.SessionResponseDTO;
import cmr.notep.interfaces.dto.StartSessionRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import cmr.notep.ressourcesjpa.repository.UtilisateursRepository;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cours")
@Slf4j
@RequiredArgsConstructor
public class CoursSessionController {

    private final CoursSessionBusiness sessionBusiness;
    private final UtilisateursRepository utilisateursRepository;

    // ─── REST endpoints ───────────────────────────────────────────────────────

    @PostMapping("/{coursId}/session/start")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('PROFESSOR', 'ADMIN')")
    public SessionResponseDTO startSession(
            @PathVariable String coursId,
            @RequestBody StartSessionRequestDTO request) {
        String userId = getCurrentUserId();
        String userRole = getCurrentUserRole();
        return sessionBusiness.startSession(coursId, request.getMode(), userId, userRole);
    }

    @GetMapping("/{coursId}/session/active")
    public SessionResponseDTO getActiveSession(@PathVariable String coursId) {
        String userId = getCurrentUserId();
        String userRole = getCurrentUserRole();
        return sessionBusiness.getActiveSession(coursId, userId, userRole);
    }

    @PostMapping("/{coursId}/session/{sessionId}/end")
    @PreAuthorize("hasAnyRole('PROFESSOR', 'ADMIN')")
    public void endSession(@PathVariable String coursId, @PathVariable String sessionId) {
        sessionBusiness.endSession(coursId, sessionId, getCurrentUserId());
    }

    @PostMapping("/{coursId}/session/{sessionId}/chapter")
    @PreAuthorize("hasAnyRole('PROFESSOR', 'ADMIN')")
    public void changeChapter(
            @PathVariable String coursId,
            @PathVariable String sessionId,
            @RequestBody Map<String, String> body) {
        sessionBusiness.changeChapter(coursId, sessionId, body.get("chapitreId"));
    }

    @PostMapping("/{coursId}/session/{sessionId}/join")
    public SessionResponseDTO joinSession(
            @PathVariable String coursId,
            @PathVariable String sessionId) {
        String userId = getCurrentUserId();
        String userRole = getCurrentUserRole();
        return sessionBusiness.joinSession(coursId, sessionId, userId, userRole);
    }

    @PostMapping("/{coursId}/session/{sessionId}/leave")
    public void leaveSession(@PathVariable String coursId, @PathVariable String sessionId) {
        sessionBusiness.leaveSession(coursId, sessionId, getCurrentUserId());
    }

    @PostMapping("/{coursId}/progress")
    public ChapitreProgressDTO saveProgress(
            @PathVariable String coursId,
            @RequestBody Map<String, Object> body) {
        String chapitreId = (String) body.get("chapitreId");
        boolean completed = Boolean.TRUE.equals(body.get("completed"));
        return sessionBusiness.saveProgress(coursId, chapitreId, completed, getCurrentUserId());
    }

    @GetMapping("/{coursId}/progress")
    public List<ChapitreProgressDTO> getProgress(@PathVariable String coursId) {
        return sessionBusiness.getProgress(coursId, getCurrentUserId());
    }

    // ─── WebSocket message handlers ───────────────────────────────────────────

    @MessageMapping("/cours/{coursId}/session/ping")
    public void ping(@DestinationVariable String coursId, Principal principal) {
        sessionBusiness.handlePing(coursId);
    }

    @MessageMapping("/cours/{coursId}/chat")
    public void chat(
            @DestinationVariable String coursId,
            @Payload Map<String, String> payload,
            Principal principal) {
        String userId = getUserIdFromPrincipal(principal);
        sessionBusiness.handleChat(coursId, userId, payload.get("message"));
    }

    @MessageMapping("/cours/{coursId}/hand-raise")
    public void handRaise(@DestinationVariable String coursId, Principal principal) {
        String userId = getUserIdFromPrincipal(principal);
        sessionBusiness.handleHandRaise(coursId, userId);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private String getCurrentUserId() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return utilisateursRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable: " + email))
                .getId();
    }

    private String getUserIdFromPrincipal(Principal principal) {
        if (principal == null) throw new RuntimeException("WebSocket: utilisateur non authentifié");
        return utilisateursRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable: " + principal.getName()))
                .getId();
    }

    private String getCurrentUserRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("ROLE_STUDENT");
    }
}
