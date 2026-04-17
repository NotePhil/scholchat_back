import axios from "axios";
import SockJS from "sockjs-client";
import { Client } from "@stomp/stompjs";

const BASE_URL = "http://localhost:8486/scholchat";
const WS_URL = "http://localhost:8486/scholchat/ws";

const sessionApi = axios.create({
  baseURL: BASE_URL,
  headers: { "Content-Type": "application/json", Accept: "application/json" },
});

sessionApi.interceptors.request.use((config) => {
  const token =
    localStorage.getItem("authToken") ||
    localStorage.getItem("cmr.notep.business.business.token");
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

// ─── REST ─────────────────────────────────────────────────────────────────────

class LiveSessionService {
  async startSession(coursId, mode = "VIDEO") {
    const res = await sessionApi.post(`/cours/${coursId}/session/start`, { mode });
    return res.data;
  }

  async getActiveSession(coursId) {
    const res = await sessionApi.get(`/cours/${coursId}/session/active`);
    return res.data;
  }

  async endSession(coursId, sessionId) {
    await sessionApi.post(`/cours/${coursId}/session/${sessionId}/end`);
  }

  async changeChapter(coursId, sessionId, chapitreId) {
    await sessionApi.post(
      `/cours/${coursId}/session/${sessionId}/chapter`,
      { chapitreId }
    );
  }

  async joinSession(coursId, sessionId) {
    const res = await sessionApi.post(
      `/cours/${coursId}/session/${sessionId}/join`
    );
    return res.data;
  }

  async leaveSession(coursId, sessionId) {
    await sessionApi.post(`/cours/${coursId}/session/${sessionId}/leave`);
  }

  async saveProgress(coursId, chapitreId, completed) {
    const res = await sessionApi.post(`/cours/${coursId}/progress`, {
      chapitreId,
      completed,
    });
    return res.data;
  }

  async getProgress(coursId) {
    const res = await sessionApi.get(`/cours/${coursId}/progress`);
    return res.data;
  }
}

// ─── WebSocket ────────────────────────────────────────────────────────────────

export class SessionWebSocket {
  constructor(coursId, handlers = {}) {
    this.coursId = coursId;
    this.handlers = handlers;
    this.client = null;
  }

  connect() {
    const token =
      localStorage.getItem("authToken") ||
      localStorage.getItem("cmr.notep.business.business.token");

    this.client = new Client({
      webSocketFactory: () => new SockJS(WS_URL),
      connectHeaders: { Authorization: `Bearer ${token}` },
      reconnectDelay: 5000,
      onConnect: () => {
        // Session events (start, end, chapter change, participants, hand raise, pong)
        this.client.subscribe(
          `/topic/cours/${this.coursId}/session`,
          (msg) => {
            const payload = JSON.parse(msg.body);
            const handler = this.handlers[payload.event];
            if (handler) handler(payload);
          }
        );

        // Chat events
        this.client.subscribe(
          `/topic/cours/${this.coursId}/chat`,
          (msg) => {
            const payload = JSON.parse(msg.body);
            if (this.handlers.CHAT_MESSAGE) this.handlers.CHAT_MESSAGE(payload);
          }
        );

        if (this.handlers.onConnected) this.handlers.onConnected();
      },
      onDisconnect: () => {
        if (this.handlers.onDisconnected) this.handlers.onDisconnected();
      },
    });

    this.client.activate();
  }

  sendPing() {
    this.client?.publish({
      destination: `/app/cours/${this.coursId}/session/ping`,
    });
  }

  sendChat(message) {
    this.client?.publish({
      destination: `/app/cours/${this.coursId}/chat`,
      body: JSON.stringify({ message }),
    });
  }

  raiseHand() {
    this.client?.publish({
      destination: `/app/cours/${this.coursId}/hand-raise`,
    });
  }

  disconnect() {
    this.client?.deactivate();
  }
}

export const liveSessionService = new LiveSessionService();
