import { useState, useEffect, useRef, useCallback } from "react";
import { liveSessionService, SessionWebSocket } from "./liveSessionService";

/**
 * useLiveSession — manages the full lifecycle of a live cours session.
 *
 * @param {string} coursId
 * @param {string} userRole  — e.g. "ROLE_PROFESSOR" or "ROLE_STUDENT"
 */
export function useLiveSession(coursId, userRole) {
  const [session, setSession] = useState(null);       // SessionResponseDTO
  const [participants, setParticipants] = useState([]);
  const [chatMessages, setChatMessages] = useState([]);
  const [handRaises, setHandRaises] = useState([]);
  const [currentChapitreId, setCurrentChapitreId] = useState(null);
  const [progress, setProgress] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [wsConnected, setWsConnected] = useState(false);

  const wsRef = useRef(null);
  const isProfessor = userRole?.includes("PROFESSOR") || userRole?.includes("ADMIN");

  // ─── WebSocket handlers ──────────────────────────────────────────────────

  const connectWebSocket = useCallback((sid) => {
    if (wsRef.current) wsRef.current.disconnect();

    wsRef.current = new SessionWebSocket(coursId, {
      onConnected: () => {
        setWsConnected(true);
        // Refresh participants when WebSocket connects
        if (sid) {
          setTimeout(() => {
            liveSessionService.getCurrentSessionParticipants(coursId, sid)
              .then(data => {
                setParticipants(data.participants || []);
                console.log('Auto-refreshed participants on WS connect:', data.participants);
              })
              .catch(e => console.error('Failed to auto-refresh participants:', e));
          }, 1000);
        }
      },
      onDisconnected: () => setWsConnected(false),

      SESSION_STARTED: (payload) => {
        setSession(payload.session);
        setCurrentChapitreId(payload.session?.currentChapitreId);
        setParticipants(payload.session?.participants ?? []);
      },
      SESSION_ENDED: () => {
        setSession((s) => s ? { ...s, status: "ENDED" } : s);
      },
      CHAPTER_CHANGED: (payload) => {
        setCurrentChapitreId(payload.chapitreId);
      },
      PARTICIPANT_JOINED: (payload) => {
        console.log('PARTICIPANT_JOINED event received:', payload);
        // Update participants list with the full list from server if available
        if (payload.participants) {
          setParticipants(payload.participants);
        } else {
          // Fallback to adding individual participant
          setParticipants((prev) => {
            const exists = prev.some((p) => p.userId === payload.userId);
            return exists ? prev : [...prev, { userId: payload.userId, userName: payload.userName }];
          });
        }
      },
      PARTICIPANT_LEFT: (payload) => {
        console.log('PARTICIPANT_LEFT event received:', payload);
        // Update participants list with the full list from server if available
        if (payload.participants) {
          setParticipants(payload.participants);
        } else {
          // Fallback to removing individual participant
          setParticipants((prev) => prev.filter((p) => p.userId !== payload.userId));
        }
      },
      HAND_RAISED: (payload) => {
        setHandRaises((prev) => [...prev.slice(-19), payload]); // keep last 20
      },
      CHAT_MESSAGE: (payload) => {
        setChatMessages((prev) => [...prev, payload]);
      },
    });

    wsRef.current.connect();
  }, [coursId]);

  // ─── Start session (professor) ───────────────────────────────────────────

  const startSession = useCallback(async (mode = "VIDEO") => {
    setLoading(true);
    setError(null);
    try {
      const data = await liveSessionService.startSession(coursId, mode);
      setSession(data);
      setCurrentChapitreId(data.currentChapitreId);
      setParticipants(data.participants ?? []);
      connectWebSocket(data.sessionId);
      return data;
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  }, [coursId, connectWebSocket]);

  // ─── Join session (student) ──────────────────────────────────────────────

  const joinSession = useCallback(async (sessionId) => {
    setLoading(true);
    setError(null);
    try {
      const data = await liveSessionService.joinSession(coursId, sessionId);
      setSession(data);
      setCurrentChapitreId(data.currentChapitreId);
      setParticipants(data.participants ?? []);
      connectWebSocket(sessionId);
      return data;
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  }, [coursId, connectWebSocket]);

  // ─── End session (professor) ─────────────────────────────────────────────

  const endSession = useCallback(async () => {
    if (!session) return;
    try {
      await liveSessionService.endSession(coursId, session.sessionId);
      setSession((s) => ({ ...s, status: "ENDED" }));
    } catch (e) {
      setError(e.message);
    }
  }, [coursId, session]);

  // ─── Change chapter (professor) ──────────────────────────────────────────

  const changeChapter = useCallback(async (chapitreId) => {
    if (!session) return;
    try {
      await liveSessionService.changeChapter(coursId, session.sessionId, chapitreId);
      setCurrentChapitreId(chapitreId);
    } catch (e) {
      setError(e.message);
    }
  }, [coursId, session]);

  // ─── Chat & hand raise ───────────────────────────────────────────────────

  const sendChat = useCallback((message) => {
    wsRef.current?.sendChat(message);
  }, []);

  const raiseHand = useCallback(() => {
    wsRef.current?.raiseHand();
  }, []);

  // ─── Progress ────────────────────────────────────────────────────────────

  const markChapterDone = useCallback(async (chapitreId) => {
    try {
      const updated = await liveSessionService.saveProgress(coursId, chapitreId, true);
      setProgress((prev) => {
        const idx = prev.findIndex((p) => p.chapitreId === chapitreId);
        return idx >= 0
          ? prev.map((p, i) => (i === idx ? updated : p))
          : [...prev, updated];
      });
    } catch (e) {
      setError(e.message);
    }
  }, [coursId]);

  const loadProgress = useCallback(async () => {
    try {
      const data = await liveSessionService.getProgress(coursId);
      setProgress(data);
    } catch (e) {
      // non-blocking
    }
  }, [coursId]);

  const refreshParticipants = useCallback(async () => {
    if (!session?.sessionId) return;
    try {
      const data = await liveSessionService.getCurrentSessionParticipants(coursId, session.sessionId);
      setParticipants(data.participants || []);
      console.log('Refreshed participants:', data.participants);
    } catch (e) {
      console.error('Failed to refresh participants:', e);
    }
  }, [coursId, session?.sessionId]);

  // ─── Leave on unmount ────────────────────────────────────────────────────

  useEffect(() => {
    return () => {
      if (session?.sessionId && session.status === "ACTIVE") {
        liveSessionService.leaveSession(coursId, session.sessionId).catch(() => {});
      }
      wsRef.current?.disconnect();
    };
  }, []); // eslint-disable-line

  return {
    session,
    participants,
    chatMessages,
    handRaises,
    currentChapitreId,
    progress,
    loading,
    error,
    wsConnected,
    isProfessor,
    startSession,
    joinSession,
    endSession,
    changeChapter,
    sendChat,
    raiseHand,
    markChapterDone,
    loadProgress,
    refreshParticipants,
  };
}
