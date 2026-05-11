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
    private final ElevesRepository elevesRepository;
    private final AccederRepository accederRepository;
    private final CoursProgrammerRepository coursProgrammerRepository;
    private final SessionAttendanceRepository attendanceRepository;
    private final ProfesseursRepository professeursRepository;
    private final JitsiTokenService jitsiTokenService;
    private final SimpMessagingTemplate messagingTemplate;

    @Value("${jitsi.app.id}")
    private String appId;

    @Value("${jitsi.domain}")
    private String jitsiDomain;

    // ─── Session start ────────────────────────────────────────────────────────

    public SessionResponseDTO startSession(String coursId, SessionMode mode, String userId, String userRole) {
        log.info("Starting session for course {} by user {} with mode {}", coursId, userId, mode);
        
        // Professors can always start sessions for their courses - no access check needed here
        
        // Idempotent: return existing active session
        Optional<CoursSessionEntity> existing = sessionRepository.findByCoursIdAndStatus(coursId, SessionStatus.ACTIVE);
        if (existing.isPresent()) {
            log.info("Active session already exists for cours {}, returning existing session {}", coursId, existing.get().getId());
            
            // Ensure the professor is in the participant list
            CoursSessionEntity session = existing.get();
            if (!session.getParticipantIds().contains(userId)) {
                session.getParticipantIds().add(userId);
                sessionRepository.save(session);
                log.info("Added professor {} to existing session {}", userId, session.getId());
            }
            
            return buildResponse(session, userId, userRole);
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
        
        // Initialize with the professor as the first participant
        session.getParticipantIds().add(userId);
        
        CoursSessionEntity saved = sessionRepository.save(session);
        log.info("Created new session {} for course {} with professor {} as first participant", saved.getId(), coursId, userId);

        // Initialize expected participants based on course programming
        initializeExpectedParticipants(saved.getId(), coursId);

        SessionResponseDTO response = buildResponse(saved, userId, userRole);

        broadcast(coursId, Map.of(
                "event", "SESSION_STARTED",
                "session", response
        ));

        log.info("Session {} started successfully for cours {} by user {}", saved.getId(), coursId, userId);
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
        log.info("User {} attempting to join session {} for course {}", userId, sessionId, coursId);
        
        CoursSessionEntity session = getSession(sessionId, coursId);
        log.info("Session found: {} with {} existing participants: {}", sessionId, session.getParticipantIds().size(), session.getParticipantIds());

        // Check if user is allowed to join this session
        if (!isUserAllowedToJoin(coursId, userId)) {
            throw new SchoolException(SchoolErrorCode.FORBIDDEN, 
                    "Vous n'êtes pas autorisé à rejoindre cette session");
        }

        boolean wasAdded = false;
        if (!session.getParticipantIds().contains(userId)) {
            session.getParticipantIds().add(userId);
            sessionRepository.save(session);
            wasAdded = true;
            log.info("Added user {} to session {}. Total participants now: {}", userId, sessionId, session.getParticipantIds().size());
        } else {
            log.info("User {} already in session {}", userId, sessionId);
        }

        // Track attendance - mark user as joined
        trackUserJoined(sessionId, userId, coursId);

        // Build response with updated participant list
        SessionResponseDTO response = buildResponse(session, userId, userRole);
        
        // Broadcast participant joined event to all connected clients
        String userName = resolveUserName(userId);
        Map<String, Object> broadcastPayload = Map.of(
                "event", "PARTICIPANT_JOINED",
                "userId", userId,
                "userName", userName,
                "totalParticipants", session.getParticipantIds().size(),
                "sessionId", sessionId,
                "participants", response.getParticipants() // Include full participant list
        );
        
        broadcast(coursId, broadcastPayload);
        log.info("Broadcasted PARTICIPANT_JOINED event for user {} in session {}", userId, sessionId);

        log.info("User {} successfully joined session {}. Response contains {} participants", 
                userId, sessionId, response.getParticipants().size());
        return response;
    }

    // ─── Leave session ────────────────────────────────────────────────────────

    public void leaveSession(String coursId, String sessionId, String userId) {
        CoursSessionEntity session = getSession(sessionId, coursId);
        boolean wasRemoved = session.getParticipantIds().remove(userId);
        if (wasRemoved) {
            sessionRepository.save(session);
            log.info("Removed user {} from session {}. Total participants now: {}", userId, sessionId, session.getParticipantIds().size());
        } else {
            log.info("User {} was not in session {} participant list", userId, sessionId);
        }

        // Track attendance - mark user as left
        trackUserLeft(sessionId, userId, coursId);

        // Get updated participant list for broadcast
        List<SessionResponseDTO.ParticipantDTO> updatedParticipants = session.getParticipantIds().stream()
                .map(uid -> SessionResponseDTO.ParticipantDTO.builder()
                        .userId(uid)
                        .userName(resolveUserName(uid))
                        .build())
                .collect(Collectors.toList());

        broadcast(coursId, Map.of(
                "event", "PARTICIPANT_LEFT",
                "userId", userId,
                "totalParticipants", session.getParticipantIds().size(),
                "sessionId", sessionId,
                "participants", updatedParticipants // Include updated participant list
        ));

        log.info("User {} left session {} and broadcast sent", userId, sessionId);
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

    @Transactional(readOnly = true)
    public List<SessionResponseDTO> getCourseSessionHistory(String coursId) {
        log.info("Fetching session history for course {}", coursId);
        
        List<CoursSessionEntity> sessions = sessionRepository.findByCoursIdOrderByStartedAtDesc(coursId);
        log.info("Found {} sessions for course {}", sessions.size(), coursId);
        
        List<SessionResponseDTO> result = sessions.stream()
                .map(session -> {
                    SessionResponseDTO dto = buildSessionHistoryResponse(session);
                    log.debug("Session {} has {} participants", session.getId(), session.getParticipantIds().size());
                    return dto;
                })
                .collect(Collectors.toList());
                
        log.info("Returning {} session history records for course {}", result.size(), coursId);
        return result;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getSessionAttendance(String sessionId, String coursId) {
        log.info("Getting attendance data for session {} in course {}", sessionId, coursId);
        
        List<SessionAttendanceEntity> attendanceRecords = attendanceRepository.findBySessionId(sessionId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("sessionId", sessionId);
        result.put("coursId", coursId);
        
        // Group by status
        Map<String, List<Map<String, Object>>> attendanceByStatus = new HashMap<>();
        
        for (SessionAttendanceEntity record : attendanceRecords) {
            String status = record.getStatus().name();
            attendanceByStatus.computeIfAbsent(status, k -> new ArrayList<>());
            
            Map<String, Object> userRecord = new HashMap<>();
            userRecord.put("userId", record.getUserId());
            userRecord.put("userName", resolveUserName(record.getUserId()));
            userRecord.put("joinedAt", record.getJoinedAt());
            userRecord.put("leftAt", record.getLeftAt());
            userRecord.put("createdAt", record.getCreatedAt());
            
            attendanceByStatus.get(status).add(userRecord);
        }
        
        result.put("attendance", attendanceByStatus);
        
        // Add summary statistics
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalExpected", attendanceByStatus.getOrDefault("EXPECTED", Collections.emptyList()).size());
        summary.put("totalJoined", attendanceByStatus.getOrDefault("JOINED", Collections.emptyList()).size());
        summary.put("totalLeft", attendanceByStatus.getOrDefault("LEFT", Collections.emptyList()).size());
        summary.put("currentlyActive", attendanceRepository.countActiveParticipants(sessionId));
        
        result.put("summary", summary);
        
        log.info("Returning attendance data for session {} with {} total records", sessionId, attendanceRecords.size());
        return result;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getCurrentSessionParticipants(String coursId, String sessionId) {
        log.info("Getting current participants for session {} in course {}", sessionId, coursId);
        
        CoursSessionEntity session = getSession(sessionId, coursId);
        
        List<SessionResponseDTO.ParticipantDTO> participants = session.getParticipantIds().stream()
                .map(uid -> SessionResponseDTO.ParticipantDTO.builder()
                        .userId(uid)
                        .userName(resolveUserName(uid))
                        .build())
                .collect(Collectors.toList());
        
        Map<String, Object> result = new HashMap<>();
        result.put("sessionId", sessionId);
        result.put("coursId", coursId);
        result.put("participants", participants);
        result.put("totalParticipants", participants.size());
        result.put("participantIds", session.getParticipantIds());
        
        log.info("Returning {} current participants for session {}: {}", 
                participants.size(), sessionId, session.getParticipantIds());
        return result;
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

    private boolean isUserAllowedToJoin(String coursId, String userId) {
        try {
            log.info("Checking access for user {} to course {}", userId, coursId);
            
            // Get the course programming to check participation rules
            List<CoursProgrammerEntity> programmations = coursProgrammerRepository.findByCoursId(coursId);
            
            if (programmations.isEmpty()) {
                log.warn("No programming found for course {}, denying access", coursId);
                return false;
            }
            
            CoursProgrammerEntity programmation = programmations.get(0);
            log.info("Found programming for course {}: {} participants, {} classes", 
                    coursId, 
                    programmation.getParticipants() != null ? programmation.getParticipants().size() : 0,
                    programmation.getClasses() != null ? programmation.getClasses().size() : 0);
            
            // ALWAYS allow the professor who scheduled the course to join
            if (programmation.getProfesseur().getId().equals(userId)) {
                log.info("User {} is the professor who scheduled course {}, allowing access", userId, coursId);
                return true;
            }
            
            // Also allow any professor to join (for co-teaching, substitutes, etc.)
            if (isProfessor(userId)) {
                log.info("User {} is a professor, allowing access to course {}", userId, coursId);
                return true;
            }
            
            // Check if specific participants were selected
            if (programmation.getParticipants() != null && !programmation.getParticipants().isEmpty()) {
                // Only specific participants are allowed
                boolean isSpecificParticipant = programmation.getParticipants().stream()
                        .anyMatch(participant -> participant.getId().equals(userId));
                log.info("User {} is {} in specific participants list for course {}", 
                        userId, isSpecificParticipant ? "included" : "NOT included", coursId);
                return isSpecificParticipant;
            } else if (programmation.getClasses() != null && !programmation.getClasses().isEmpty()) {
                // All students from the classes are allowed
                log.info("No specific participants selected, checking class access for user {} in course {}", userId, coursId);
                
                // Check if user is a student first
                boolean isStudent = isStudent(userId);
                log.info("User {} is student: {}", userId, isStudent);
                
                if (!isStudent) {
                    log.info("User {} is not a student, denying access to course {}", userId, coursId);
                    return false;
                }
                
                // Check class access for each class in the programming
                for (ClassesEntity classe : programmation.getClasses()) {
                    boolean hasAccessToThisClass = accederRepository.existsByUtilisateurIdAndClasseId(userId, classe.getId());
                    log.info("User {} has access to class {}: {}", userId, classe.getId(), hasAccessToThisClass);
                    
                    if (hasAccessToThisClass) {
                        log.info("User {} has access to at least one class for course {}, allowing access", userId, coursId);
                        return true;
                    }
                }
                
                log.info("User {} does not have access to any classes for course {}, denying access", userId, coursId);
                return false;
            }
            
            log.warn("No valid participation rules found for course {}", coursId);
            return false;
            
        } catch (Exception e) {
            log.error("Error checking user access for course {}: {}", coursId, e.getMessage(), e);
            return false;
        }
    }

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

    private SessionResponseDTO buildSessionHistoryResponse(CoursSessionEntity session) {
        CoursEntity cours = getCours(session.getCoursId());

        // Get actual participants who joined this session from attendance records
        List<SessionAttendanceEntity> attendanceRecords = attendanceRepository.findBySessionId(session.getId());
        List<SessionResponseDTO.ParticipantDTO> participants = attendanceRecords.stream()
                .filter(record -> record.getStatus() == SessionAttendanceEntity.AttendanceStatus.JOINED || 
                                 record.getStatus() == SessionAttendanceEntity.AttendanceStatus.LEFT)
                .map(record -> SessionResponseDTO.ParticipantDTO.builder()
                        .userId(record.getUserId())
                        .userName(resolveUserName(record.getUserId()))
                        .build())
                .distinct() // Remove duplicates if any
                .collect(Collectors.toList());

        log.debug("Session {} history: found {} attendance records, {} actual participants", 
                session.getId(), attendanceRecords.size(), participants.size());

        return SessionResponseDTO.builder()
                .sessionId(session.getId())
                .roomName(session.getRoomName())
                .mode(session.getMode())
                .status(session.getStatus())
                .coursId(session.getCoursId())
                .coursTitle(cours.getTitre())
                .currentChapitreId(session.getCurrentChapitreId())
                .participants(participants)
                .startedAt(session.getStartedAt())
                .endedAt(session.getEndedAt())
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

    // ─── Attendance tracking methods ──────────────────────────────────────────

    private void initializeExpectedParticipants(String sessionId, String coursId) {
        log.info("Initializing expected participants for session {} and course {}", sessionId, coursId);
        
        try {
            // Get the course programming to determine expected participants
            List<CoursProgrammerEntity> programmations = coursProgrammerRepository.findByCoursId(coursId);
            
            Set<String> expectedParticipants = new HashSet<>();
            
            if (!programmations.isEmpty()) {
                CoursProgrammerEntity programmation = programmations.get(0); // Get the latest programming
                
                if (programmation.getParticipants() != null && !programmation.getParticipants().isEmpty()) {
                    // Specific participants were selected - only these students are expected
                    expectedParticipants = programmation.getParticipants().stream()
                            .map(UtilisateursEntity::getId)
                            .collect(Collectors.toSet());
                    log.info("Found {} specific participants for course {}", expectedParticipants.size(), coursId);
                } else if (programmation.getClasses() != null && !programmation.getClasses().isEmpty()) {
                    // No specific participants selected - ALL students from the classes are expected
                    for (ClassesEntity classe : programmation.getClasses()) {
                        List<AccederEntity> classAccess = accederRepository.findByClasseId(classe.getId());
                        Set<String> classStudents = classAccess.stream()
                                .map(AccederEntity::getUtilisateurId)
                                .filter(this::isStudent) // Only include students, not professors
                                .collect(Collectors.toSet());
                        expectedParticipants.addAll(classStudents);
                        log.info("Added {} students from class {} to expected participants (all class students)", classStudents.size(), classe.getId());
                    }
                }
            }
            
            // Create attendance records for all expected participants
            for (String userId : expectedParticipants) {
                // Check if attendance record already exists
                Optional<SessionAttendanceEntity> existingAttendance = 
                        attendanceRepository.findBySessionIdAndUserId(sessionId, userId);
                
                if (!existingAttendance.isPresent()) {
                    SessionAttendanceEntity attendance = new SessionAttendanceEntity();
                    attendance.setId(UUID.randomUUID().toString());
                    attendance.setSessionId(sessionId);
                    attendance.setUserId(userId);
                    attendance.setCoursId(coursId);
                    attendance.setStatus(SessionAttendanceEntity.AttendanceStatus.EXPECTED);
                    attendance.setCreatedAt(LocalDateTime.now());
                    attendanceRepository.save(attendance);
                }
            }
            
            log.info("Initialized {} expected participants for session {}", expectedParticipants.size(), sessionId);
            
        } catch (Exception e) {
            log.error("Error initializing expected participants for session {}: {}", sessionId, e.getMessage());
        }
    }
    
    private boolean isStudent(String userId) {
        // Check if user exists in the eleves table
        return elevesRepository.existsById(userId);
    }
    
    private boolean isProfessor(String userId) {
        // Check if user exists in the professeurs table
        return professeursRepository.existsById(userId);
    }
    
    private void trackUserJoined(String sessionId, String userId, String coursId) {
        try {
            log.info("Tracking user {} joining session {} for course {}", userId, sessionId, coursId);
            
            Optional<SessionAttendanceEntity> existingAttendance = 
                    attendanceRepository.findBySessionIdAndUserId(sessionId, userId);
            
            SessionAttendanceEntity attendance;
            if (existingAttendance.isPresent()) {
                attendance = existingAttendance.get();
                log.info("Found existing attendance record for user {} in session {}, current status: {}", 
                        userId, sessionId, attendance.getStatus());
            } else {
                // Create new attendance record if user wasn't expected
                attendance = new SessionAttendanceEntity();
                attendance.setId(UUID.randomUUID().toString());
                attendance.setSessionId(sessionId);
                attendance.setUserId(userId);
                attendance.setCoursId(coursId);
                attendance.setCreatedAt(LocalDateTime.now());
                log.info("Created new attendance record for user {} in session {}", userId, sessionId);
            }
            
            attendance.setStatus(SessionAttendanceEntity.AttendanceStatus.JOINED);
            attendance.setJoinedAt(LocalDateTime.now());
            SessionAttendanceEntity saved = attendanceRepository.save(attendance);
            
            log.info("Successfully tracked user {} joining session {} with status JOINED, record ID: {}", 
                    userId, sessionId, saved.getId());
        } catch (Exception e) {
            log.error("Error tracking user join for session {}: {}", sessionId, e.getMessage(), e);
        }
    }
    
    private void trackUserLeft(String sessionId, String userId, String coursId) {
        try {
            Optional<SessionAttendanceEntity> attendance = 
                    attendanceRepository.findBySessionIdAndUserId(sessionId, userId);
            
            if (attendance.isPresent()) {
                SessionAttendanceEntity record = attendance.get();
                record.setStatus(SessionAttendanceEntity.AttendanceStatus.LEFT);
                record.setLeftAt(LocalDateTime.now());
                attendanceRepository.save(record);
                
                log.info("Tracked user {} leaving session {}", userId, sessionId);
            }
        } catch (Exception e) {
            log.error("Error tracking user leave for session {}: {}", sessionId, e.getMessage());
        }
    }
}
