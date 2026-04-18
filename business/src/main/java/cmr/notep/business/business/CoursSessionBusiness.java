package cmr.notep.business.business;

import cmr.notep.business.exceptions.SchoolException;
import cmr.notep.business.exceptions.enums.SchoolErrorCode;
import cmr.notep.business.services.JitsiTokenService;
import cmr.notep.interfaces.dto.ChapitreProgressDTO;
import cmr.notep.interfaces.dto.SessionResponseDTO;
import cmr.notep.modele.SessionMode;
import cmr.notep.modele.SessionStatus;
import cmr.notep.ressourcesjpa.dao.*;
import cmr.notep.ressourcesjpa.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
@Transactional
public class CoursSessionBusiness {

    private final CoursSessionRepository sessionRepository;
    private final ChapitreProgressRepository progressRepository;
    private final CoursRepository coursRepository;
    private final UtilisateursRepository utilisateursRepository;
    private final JitsiTokenService jitsiTokenService;
    private final SimpMessagingTemplate messagingTemplate;

    @Value("${jitsi.app.id}")
    private String appId;

    @Value("${jitsi.domain}")
    private String jitsiDomain;

    // ─── Session start ────────────────────────────────────────────────────────

    public SessionResponseDTO startSession(String coursId, SessionMode mode, String userId, String userRole) {
        // Idempotent: return existing active session
        Optional<CoursSessionEntity> existing = sessionRepository.findByCoursIdAndStatus(coursId, SessionStatus.ACTIVE);
        if (existing.isPresent()) {
            log.info("Active session already exists for cours {}", coursId);
            return buildResponse(existing.get(), userId, userRole);
        }

        CoursEntity cours = getCours(coursId);

        CoursSessionEntity session = new CoursSessionEntity();
        session.setId(UUID.randomUUID().toString());
        session.setCoursId(coursId);
        session.setRoomName("scholchat-" + coursId + "-" + System.currentTimeMillis());
        session.setMode(mode);
        session.setStatus(SessionStatus.ACTIVE);
        session.setStartedAt(LocalDateTime.now());
        session.setStartedByUserId(userId);
        CoursSessionEntity saved = sessionRepository.save(session);

        SessionResponseDTO response = buildResponse(saved, userId, userRole);

        broadcast(coursId, Map.of(
                "event", "SESSION_STARTED",
                "session", response
        ));

        log.info("Session started for cours {} by user {}", coursId, userId);
        return response;
    }

    // ─── Get active session ───────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public SessionResponseDTO getActiveSession(String coursId, String userId, String userRole) {
        CoursSessionEntity session = sessionRepository.findByCoursIdAndStatus(coursId, SessionStatus.ACTIVE)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND,
                        "Aucune session active pour ce cours"));
        return buildResponse(session, userId, userRole);
    }

    // ─── End session ──────────────────────────────────────────────────────────

    public void endSession(String coursId, String sessionId, String userId) {
        CoursSessionEntity session = getSession(sessionId, coursId);

        if (!session.getStartedByUserId().equals(userId)) {
            throw new SchoolException(SchoolErrorCode.FORBIDDEN,
                    "Seul le créateur de la session peut la terminer");
        }

        session.setStatus(SessionStatus.ENDED);
        session.setEndedAt(LocalDateTime.now());
        sessionRepository.save(session);

        broadcast(coursId, Map.of(
                "event", "SESSION_ENDED",
                "sessionId", sessionId
        ));

        log.info("Session {} ended by user {}", sessionId, userId);
    }

    // ─── Change chapter ───────────────────────────────────────────────────────

    public void changeChapter(String coursId, String sessionId, String chapitreId) {
        CoursSessionEntity session = getSession(sessionId, coursId);
        session.setCurrentChapitreId(chapitreId);
        sessionRepository.save(session);

        CoursEntity cours = getCours(coursId);
        cours.getChapitres().stream()
                .filter(c -> c.getId().equals(chapitreId))
                .findFirst()
                .ifPresent(chapitre -> broadcast(coursId, Map.of(
                        "event", "CHAPTER_CHANGED",
                        "chapitreId", chapitreId,
                        "chapitreOrdre", chapitre.getOrdre(),
                        "chapitreTitle", chapitre.getTitre()
                )));

        log.info("Chapter changed to {} in session {}", chapitreId, sessionId);
    }

    // ─── Join session ─────────────────────────────────────────────────────────

    public SessionResponseDTO joinSession(String coursId, String sessionId, String userId, String userRole) {
        CoursSessionEntity session = getSession(sessionId, coursId);

        if (!session.getParticipantIds().contains(userId)) {
            session.getParticipantIds().add(userId);
            sessionRepository.save(session);
        }

        String userName = resolveUserName(userId);
        broadcast(coursId, Map.of(
                "event", "PARTICIPANT_JOINED",
                "userId", userId,
                "userName", userName,
                "totalParticipants", session.getParticipantIds().size()
        ));

        log.info("User {} joined session {}", userId, sessionId);
        return buildResponse(session, userId, userRole);
    }

    // ─── Leave session ────────────────────────────────────────────────────────

    public void leaveSession(String coursId, String sessionId, String userId) {
        CoursSessionEntity session = getSession(sessionId, coursId);
        session.getParticipantIds().remove(userId);
        sessionRepository.save(session);

        broadcast(coursId, Map.of(
                "event", "PARTICIPANT_LEFT",
                "userId", userId,
                "totalParticipants", session.getParticipantIds().size()
        ));

        log.info("User {} left session {}", userId, sessionId);
    }

    // ─── Chapter progress ─────────────────────────────────────────────────────

    public ChapitreProgressDTO saveProgress(String coursId, String chapitreId, boolean completed, String userId) {
        ChapitreProgressEntity progress = progressRepository
                .findByUserIdAndChapitreId(userId, chapitreId)
                .orElseGet(() -> {
                    ChapitreProgressEntity p = new ChapitreProgressEntity();
                    p.setId(UUID.randomUUID().toString());
                    p.setUserId(userId);
                    p.setChapitreId(chapitreId);
                    p.setCoursId(coursId);
                    return p;
                });

        progress.setCompleted(completed);
        progress.setCompletedAt(completed ? LocalDateTime.now() : null);
        ChapitreProgressEntity saved = progressRepository.save(progress);
        return toProgressDTO(saved);
    }

    @Transactional(readOnly = true)
    public List<ChapitreProgressDTO> getProgress(String coursId, String userId) {
        return progressRepository.findByUserIdAndCoursId(userId, coursId)
                .stream().map(this::toProgressDTO).collect(Collectors.toList());
    }

    // ─── WebSocket message handlers ───────────────────────────────────────────

    public void handlePing(String coursId) {
        broadcast(coursId, Map.of("event", "PONG", "timestamp", System.currentTimeMillis()));
    }

    public void handleChat(String coursId, String userId, String message) {
        String userName = resolveUserName(userId);
        messagingTemplate.convertAndSend("/topic/cours/" + coursId + "/chat", Map.of(
                "event", "CHAT_MESSAGE",
                "userId", userId,
                "userName", userName,
                "message", message,
                "timestamp", System.currentTimeMillis()
        ));
    }

    public void handleHandRaise(String coursId, String userId) {
        String userName = resolveUserName(userId);
        broadcast(coursId, Map.of(
                "event", "HAND_RAISED",
                "userId", userId,
                "userName", userName
        ));
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private CoursSessionEntity getSession(String sessionId, String coursId) {
        CoursSessionEntity session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Session introuvable"));
        if (!session.getCoursId().equals(coursId)) {
            throw new SchoolException(SchoolErrorCode.INVALID_INPUT, "Session n'appartient pas à ce cours");
        }
        return session;
    }

    private CoursEntity getCours(String coursId) {
        return coursRepository.findById(coursId)
                .orElseThrow(() -> new SchoolException(SchoolErrorCode.NOT_FOUND, "Cours introuvable"));
    }

    private SessionResponseDTO buildResponse(CoursSessionEntity session, String userId, String userRole) {
        CoursEntity cours = getCours(session.getCoursId());

        String userName = resolveUserName(userId);
        String userEmail = utilisateursRepository.findById(userId)
                .map(cmr.notep.ressourcesjpa.dao.UtilisateursEntity::getEmail).orElse("");
        String jitsiJwt = jitsiTokenService.generateToken(
                session.getRoomName(), userId, userName, userEmail, userRole);

        List<SessionResponseDTO.ChapitreDTO> chapitres = cours.getChapitres().stream()
                .sorted(Comparator.comparingInt(ChapitreEntity::getOrdre))
                .map(c -> SessionResponseDTO.ChapitreDTO.builder()
                        .id(c.getId())
                        .titre(c.getTitre())
                        .ordre(c.getOrdre())
                        .contenu(c.getContenu())
                        .fileUrl(c.getImageUrl())
                        .build())
                .collect(Collectors.toList());

        List<SessionResponseDTO.ParticipantDTO> participants = session.getParticipantIds().stream()
                .map(uid -> SessionResponseDTO.ParticipantDTO.builder()
                        .userId(uid)
                        .userName(resolveUserName(uid))
                        .build())
                .collect(Collectors.toList());

        return SessionResponseDTO.builder()
                .sessionId(session.getId())
                .roomName(session.getRoomName())
                .jitsiJwt(jitsiJwt)
                .jitsiDomain(jitsiDomain)
                .mode(session.getMode())
                .status(session.getStatus())
                .coursId(session.getCoursId())
                .coursTitle(cours.getTitre())
                .currentChapitreId(session.getCurrentChapitreId())
                .chapitres(chapitres)
                .participants(participants)
                .startedAt(session.getStartedAt())
                .build();
    }

    private String resolveUserName(String userId) {
        return utilisateursRepository.findById(userId)
                .map(u -> u.getPrenom() + " " + u.getNom())
                .orElse("Utilisateur inconnu");
    }

    private void broadcast(String coursId, Object payload) {
        messagingTemplate.convertAndSend("/topic/cours/" + coursId + "/session", payload);
    }

    private ChapitreProgressDTO toProgressDTO(ChapitreProgressEntity e) {
        return ChapitreProgressDTO.builder()
                .userId(e.getUserId())
                .chapitreId(e.getChapitreId())
                .coursId(e.getCoursId())
                .completed(e.isCompleted())
                .completedAt(e.getCompletedAt())
                .build();
    }
}
