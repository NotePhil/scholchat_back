import React, { useEffect, useRef, useState } from "react";
import { useLiveSession } from "./useLiveSession";
import { liveSessionService } from "./liveSessionService";

/**
 * LiveSessionPage
 *
 * Props:
 *   coursId   {string}  — ID of the cours
 *   userRole  {string}  — e.g. "ROLE_PROFESSOR" / "ROLE_STUDENT"
 *   sessionId {string?} — if student is joining an existing session
 */
const LiveSessionPage = ({ coursId, userRole, sessionId: initialSessionId }) => {
  const {
    session, participants, chatMessages, handRaises,
    currentChapitreId, progress, loading, error, wsConnected,
    isProfessor, startSession, joinSession, endSession,
    changeChapter, sendChat, raiseHand, markChapterDone, loadProgress,
  } = useLiveSession(coursId, userRole);

  const [mode, setMode] = useState("VIDEO");
  const [chatInput, setChatInput] = useState("");
  const [jitsiReady, setJitsiReady] = useState(false);
  const jitsiContainerRef = useRef(null);
  const jitsiApiRef = useRef(null);
  const chatEndRef = useRef(null);

  // ─── Auto-join if sessionId is provided (student flow) ──────────────────
  useEffect(() => {
    if (initialSessionId) {
      joinSession(initialSessionId);
    }
    loadProgress();
  }, []); // eslint-disable-line

  // ─── Mount Jitsi when session becomes active ─────────────────────────────
  useEffect(() => {
    if (!session || session.status !== "ACTIVE" || !session.jitsiJwt) return;
    if (jitsiApiRef.current) return; // already mounted

    const script = document.createElement("script");
    script.src = `https://8x8.vc/vpaas-magic-cookie-b0d7354d679944a197a3e35a0d1e2c60/external_api.js`;
    script.async = true;
    script.onload = () => {
      jitsiApiRef.current = new window.JitsiMeetExternalAPI("8x8.vc", {
        roomName: session.roomName,
        parentNode: jitsiContainerRef.current,
        jwt: session.jitsiJwt,
        configOverwrite: {
          startWithAudioMuted: !isProfessor,
          startWithVideoMuted: session.mode === "AUDIO" || session.mode === "CONTENT_ONLY",
          disableDeepLinking: true,
        },
        interfaceConfigOverwrite: {
          SHOW_JITSI_WATERMARK: false,
          SHOW_WATERMARK_FOR_GUESTS: false,
          TOOLBAR_BUTTONS: isProfessor
            ? ["microphone", "camera", "desktop", "chat", "raisehand", "tileview", "hangup"]
            : ["microphone", "camera", "chat", "raisehand", "tileview", "hangup"],
        },
      });
      setJitsiReady(true);
    };
    document.head.appendChild(script);

    return () => {
      jitsiApiRef.current?.dispose();
      jitsiApiRef.current = null;
      setJitsiReady(false);
    };
  }, [session?.sessionId]); // eslint-disable-line

  // ─── Unmount Jitsi when session ends ────────────────────────────────────
  useEffect(() => {
    if (session?.status === "ENDED") {
      jitsiApiRef.current?.dispose();
      jitsiApiRef.current = null;
      setJitsiReady(false);
    }
  }, [session?.status]);

  // ─── Auto-scroll chat ────────────────────────────────────────────────────
  useEffect(() => {
    chatEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [chatMessages]);

  // ─── Handlers ────────────────────────────────────────────────────────────
  const handleStart = async () => {
    await startSession(mode);
  };

  const handleSendChat = (e) => {
    e.preventDefault();
    if (!chatInput.trim()) return;
    sendChat(chatInput.trim());
    setChatInput("");
  };

  const isCompleted = (chapitreId) =>
    progress.some((p) => p.chapitreId === chapitreId && p.completed);

  // ─── Pre-session screen ──────────────────────────────────────────────────
  if (!session || session.status === "ENDED") {
    return (
      <div className="min-h-screen bg-gray-900 flex items-center justify-center p-6">
        <div className="bg-gray-800 rounded-2xl shadow-xl p-8 w-full max-w-md text-white">
          <h1 className="text-2xl font-bold mb-2">Session en direct</h1>
          <p className="text-gray-400 mb-6">
            {session?.status === "ENDED"
              ? "La session est terminée."
              : isProfessor
              ? "Démarrez une session pour vos étudiants."
              : "En attente du démarrage de la session..."}
          </p>

          {error && (
            <div className="mb-4 p-3 bg-red-900/40 border border-red-700 rounded-xl text-red-300 text-sm">
              {error}
            </div>
          )}

          {isProfessor && session?.status !== "ENDED" && (
            <>
              <label className="block text-sm font-semibold text-gray-300 mb-2">
                Mode de session
              </label>
              <select
                value={mode}
                onChange={(e) => setMode(e.target.value)}
                className="w-full mb-6 px-4 py-3 rounded-xl bg-gray-700 border border-gray-600 text-white focus:ring-2 focus:ring-blue-500"
              >
                <option value="VIDEO">Vidéo + Audio</option>
                <option value="AUDIO">Audio seulement</option>
                <option value="CONTENT_ONLY">Contenu seulement</option>
              </select>
              <button
                onClick={handleStart}
                disabled={loading}
                className="w-full py-3 bg-blue-600 hover:bg-blue-700 text-white font-semibold rounded-xl transition-colors disabled:opacity-50"
              >
                {loading ? "Démarrage..." : "▶ Démarrer la session"}
              </button>
            </>
          )}
        </div>
      </div>
    );
  }

  // ─── Active session screen ───────────────────────────────────────────────
  return (
    <div className="h-screen bg-gray-900 flex flex-col overflow-hidden">

      {/* Top bar */}
      <div className="flex items-center justify-between px-4 py-2 bg-gray-800 border-b border-gray-700 shrink-0">
        <div className="flex items-center gap-3">
          <span className="w-2 h-2 rounded-full bg-green-400 animate-pulse" />
          <span className="text-white font-semibold text-sm">{session.coursTitle}</span>
          <span className="text-xs text-gray-400 bg-gray-700 px-2 py-0.5 rounded-full">
            {session.mode}
          </span>
          {!wsConnected && (
            <span className="text-xs text-yellow-400">⚠ Reconnexion...</span>
          )}
        </div>
        <div className="flex items-center gap-3">
          <span className="text-gray-400 text-sm">
            👥 {participants.length} participant{participants.length !== 1 ? "s" : ""}
          </span>
          {!isProfessor && (
            <button
              onClick={raiseHand}
              className="px-3 py-1.5 bg-yellow-600 hover:bg-yellow-700 text-white text-sm rounded-lg transition-colors"
            >
              ✋ Lever la main
            </button>
          )}
          {isProfessor && (
            <button
              onClick={endSession}
              className="px-3 py-1.5 bg-red-600 hover:bg-red-700 text-white text-sm rounded-lg transition-colors"
            >
              ⏹ Terminer
            </button>
          )}
        </div>
      </div>

      {/* Main layout */}
      <div className="flex flex-1 overflow-hidden">

        {/* Left: Jitsi + chapter content */}
        <div className="flex flex-col flex-1 overflow-hidden">

          {/* Jitsi video */}
          {session.mode !== "CONTENT_ONLY" && (
            <div
              ref={jitsiContainerRef}
              className="w-full bg-black"
              style={{ height: "55%" }}
            />
          )}

          {/* Current chapter content */}
          <div className="flex-1 overflow-y-auto p-4 bg-gray-900">
            {(() => {
              const chapitre = session.chapitres?.find(
                (c) => c.id === currentChapitreId
              ) ?? session.chapitres?.[0];
              if (!chapitre) return (
                <p className="text-gray-500 text-center mt-8">Aucun chapitre sélectionné</p>
              );
              return (
                <div className="max-w-3xl mx-auto">
                  <div className="flex items-center justify-between mb-4">
                    <h2 className="text-white text-xl font-bold">
                      {chapitre.ordre}. {chapitre.titre}
                    </h2>
                    {!isProfessor && (
                      <button
                        onClick={() => markChapterDone(chapitre.id)}
                        disabled={isCompleted(chapitre.id)}
                        className={`px-3 py-1.5 text-sm rounded-lg transition-colors ${
                          isCompleted(chapitre.id)
                            ? "bg-green-800 text-green-300 cursor-default"
                            : "bg-green-600 hover:bg-green-700 text-white"
                        }`}
                      >
                        {isCompleted(chapitre.id) ? "✓ Terminé" : "Marquer terminé"}
                      </button>
                    )}
                  </div>
                  <p className="text-gray-300 leading-relaxed whitespace-pre-wrap">
                    {chapitre.contenu}
                  </p>
                  {chapitre.fileUrl && (
                    <a
                      href={chapitre.fileUrl}
                      target="_blank"
                      rel="noreferrer"
                      className="inline-block mt-4 px-4 py-2 bg-blue-700 hover:bg-blue-600 text-white rounded-lg text-sm"
                    >
                      📎 Ouvrir le fichier
                    </a>
                  )}
                </div>
              );
            })()}
          </div>
        </div>

        {/* Right sidebar */}
        <div className="w-72 flex flex-col bg-gray-800 border-l border-gray-700 shrink-0">

          {/* Chapters list */}
          <div className="p-3 border-b border-gray-700">
            <p className="text-gray-400 text-xs font-semibold uppercase tracking-wider mb-2">
              Chapitres
            </p>
            <div className="space-y-1 max-h-48 overflow-y-auto">
              {session.chapitres?.map((c) => (
                <button
                  key={c.id}
                  onClick={() => isProfessor && changeChapter(c.id)}
                  className={`w-full text-left px-3 py-2 rounded-lg text-sm transition-colors flex items-center gap-2 ${
                    c.id === currentChapitreId
                      ? "bg-blue-700 text-white"
                      : isProfessor
                      ? "text-gray-300 hover:bg-gray-700 cursor-pointer"
                      : "text-gray-300 cursor-default"
                  }`}
                >
                  {isCompleted(c.id) && (
                    <span className="text-green-400 text-xs">✓</span>
                  )}
                  <span className="truncate">{c.ordre}. {c.titre}</span>
                </button>
              ))}
            </div>
          </div>

          {/* Hand raises */}
          {handRaises.length > 0 && (
            <div className="p-3 border-b border-gray-700">
              <p className="text-yellow-400 text-xs font-semibold uppercase tracking-wider mb-2">
                ✋ Mains levées
              </p>
              <div className="space-y-1 max-h-24 overflow-y-auto">
                {handRaises.slice(-5).map((h, i) => (
                  <p key={i} className="text-gray-300 text-xs">{h.userName}</p>
                ))}
              </div>
            </div>
          )}

          {/* Participants */}
          <div className="p-3 border-b border-gray-700">
            <p className="text-gray-400 text-xs font-semibold uppercase tracking-wider mb-2">
              Participants ({participants.length})
            </p>
            <div className="space-y-1 max-h-32 overflow-y-auto">
              {participants.map((p) => (
                <p key={p.userId} className="text-gray-300 text-xs truncate">
                  • {p.userName}
                </p>
              ))}
            </div>
          </div>

          {/* Chat */}
          <div className="flex flex-col flex-1 overflow-hidden p-3">
            <p className="text-gray-400 text-xs font-semibold uppercase tracking-wider mb-2">
              Chat
            </p>
            <div className="flex-1 overflow-y-auto space-y-2 mb-2">
              {chatMessages.map((m, i) => (
                <div key={i} className="text-xs">
                  <span className="text-blue-400 font-semibold">{m.userName}: </span>
                  <span className="text-gray-300">{m.message}</span>
                </div>
              ))}
              <div ref={chatEndRef} />
            </div>
            <form onSubmit={handleSendChat} className="flex gap-2">
              <input
                value={chatInput}
                onChange={(e) => setChatInput(e.target.value)}
                placeholder="Message..."
                className="flex-1 px-3 py-2 rounded-lg bg-gray-700 border border-gray-600 text-white text-xs focus:ring-1 focus:ring-blue-500 outline-none"
              />
              <button
                type="submit"
                disabled={!chatInput.trim()}
                className="px-3 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg text-xs disabled:opacity-40 transition-colors"
              >
                ➤
              </button>
            </form>
          </div>
        </div>
      </div>
    </div>
  );
};

export default LiveSessionPage;
