// hooks/useRoomSocket.js  (NEW FILE)
// One live connection to a study room: chat, who is here, shared timer and "something changed" pings.
// It also sends a heartbeat every 15s (this is what attendance time is measured from).
import { useCallback, useEffect, useRef, useState } from "react";
import { fetchMessages, fetchTimer } from "../api/studyRoomApi";

const HEARTBEAT_MS = 15000;
const ACK_TIMEOUT_MS = 8000;
const PAGE = 50;
const FATAL = { 4403: "kicked", 4409: "full", 4410: "ended" }; // do not reconnect on these

// confirmed messages by id, then still-sending ones in the order they were typed
function order(list) {
  const sent = list.filter((m) => m.id != null).sort((a, b) => a.id - b.id);
  return [...sent, ...list.filter((m) => m.id == null)];
}

export function useRoomSocket(roomId, { enabled, myUserId }) {
  // connecting | open | reconnecting | closed | kicked | full | ended
  const [status, setStatus] = useState("connecting");
  const [messages, setMessages] = useState([]);
  const [hasMore, setHasMore] = useState(false);
  const [presence, setPresence] = useState([]);
  const [timer, setTimer] = useState(null);
  const [notesEvent, setNotesEvent] = useState(null);
  const [challengeTick, setChallengeTick] = useState(0);
  const [roomTick, setRoomTick] = useState(0);
  const [openCount, setOpenCount] = useState(0);
  const [reward, setReward] = useState(null);
  const [lastError, setLastError] = useState("");
  const [unread, setUnread] = useState(0);

  const wsRef = useRef(null);
  const myIdRef = useRef(myUserId);
  const chatVisibleRef = useRef(true);
  const ackTimers = useRef(new Map());
  const firstLoadRef = useRef(true);

  useEffect(() => {
    myIdRef.current = myUserId;
  });

  const mergeServer = useCallback((list) => {
    setMessages((prev) => {
      const next = [...prev];
      for (const m of list) {
        const i = next.findIndex(
          (x) => x.id === m.id || (m.clientId && x.clientId === m.clientId),
        );
        if (i >= 0) next[i] = { ...m };
        else next.push({ ...m });
      }
      return order(next);
    });
  }, []);

  const applyTimer = useCallback((view) => {
    if (!view) return;
    setTimer({ ...view, offset: Date.parse(view.serverNow) - Date.now() });
  }, []);

  const markFailed = useCallback((clientId, reason) => {
    clearTimeout(ackTimers.current.get(clientId));
    ackTimers.current.delete(clientId);
    setMessages((prev) =>
      prev.map((m) =>
        m.clientId === clientId && m.id == null
          ? { ...m, status: "failed", error: reason }
          : m,
      ),
    );
  }, []);

  const loadLatest = useCallback(() => {
    fetchMessages(roomId, { limit: PAGE })
      .then((res) => {
        const list = res.data.data;
        mergeServer(list);
        if (firstLoadRef.current) {
          setHasMore(list.length >= PAGE);
          firstLoadRef.current = false;
        }
      })
      .catch(() => {});
  }, [roomId, mergeServer]);

  const loadEarlier = useCallback(async () => {
    const oldest = messages.find((m) => m.id != null)?.id;
    if (!oldest) return;
    const res = await fetchMessages(roomId, { before: oldest, limit: PAGE });
    mergeServer(res.data.data);
    setHasMore(res.data.data.length >= PAGE);
  }, [roomId, messages, mergeServer]);

  const onFrame = useCallback(
    ({ type, data }) => {
      switch (type) {
        case "chat": {
          if (data.clientId) {
            clearTimeout(ackTimers.current.get(data.clientId));
            ackTimers.current.delete(data.clientId);
          }
          mergeServer([data]);
          const mine = data.senderId === myIdRef.current;
          if (!mine && (!chatVisibleRef.current || document.hidden)) {
            setUnread((n) => n + 1);
          }
          break;
        }
        case "chat_deleted":
          setMessages((prev) => prev.filter((m) => m.id !== data.id));
          break;
        case "presence":
          setPresence(data);
          break;
        case "timer":
          applyTimer(data);
          break;
        case "notes":
          setNotesEvent({ ...data, at: Date.now() });
          break;
        case "challenge":
          setChallengeTick((t) => t + 1);
          break;
        case "room":
          setRoomTick((t) => t + 1);
          if (data.status === "ARCHIVED") setStatus("ended");
          break;
        case "reward":
          setReward({ ...data, at: Date.now() });
          break;
        case "kicked":
          setStatus("kicked");
          break;
        case "error":
          if (data.clientId && !["null", "undefined"].includes(data.clientId)) {
            markFailed(data.clientId, data.message);
          } else {
            setLastError(data.message ?? "Something went wrong");
          }
          break;
        default:
          break; // "pong" and anything unknown
      }
    },
    [mergeServer, applyTimer, markFailed],
  );

  // ---- the connection itself (with reconnect + backoff) ----
  useEffect(() => {
    if (!enabled) return;
    const timers = ackTimers.current;
    let stopped = false;
    let attempt = 0;
    let failures = 0;
    let retry;
    let beat;

    const sendBeat = () => {
      const ws = wsRef.current;
      if (ws && ws.readyState === WebSocket.OPEN) {
        ws.send(
          JSON.stringify({ type: "heartbeat", visible: !document.hidden }),
        );
      }
    };
    const onVisibility = () => sendBeat();

    const connect = () => {
      const token = encodeURIComponent(localStorage.getItem("token") ?? "");
      const ws = new WebSocket(
        `${import.meta.env.VITE_WS_URL}/rooms?roomId=${roomId}&token=${token}`,
      );
      wsRef.current = ws;
      let opened = false;

      ws.onopen = () => {
        opened = true;
        attempt = 0;
        failures = 0;
        setStatus("open");
        setOpenCount((n) => n + 1);
        sendBeat();
        beat = setInterval(sendBeat, HEARTBEAT_MS);
        loadLatest(); // fills any gap while we were disconnected
        fetchTimer(roomId)
          .then((res) => applyTimer(res.data.data))
          .catch(() => {});
      };
      ws.onmessage = (ev) => {
        try {
          onFrame(JSON.parse(ev.data));
        } catch {
          /* ignore malformed frames */
        }
      };
      ws.onclose = (ev) => {
        clearInterval(beat);
        if (stopped) return;
        if (FATAL[ev.code]) {
          setStatus(FATAL[ev.code]);
          return;
        }
        if (!opened && ++failures >= 5) {
          setStatus("closed"); // refused (not a member / not logged in): stop trying
          return;
        }
        setStatus("reconnecting");
        const delay = Math.min(1000 * 2 ** attempt++, 15000);
        retry = setTimeout(connect, delay);
      };
    };

    connect();
    document.addEventListener("visibilitychange", onVisibility);
    return () => {
      stopped = true;
      clearTimeout(retry);
      clearInterval(beat);
      document.removeEventListener("visibilitychange", onVisibility);
      timers.forEach((t) => clearTimeout(t));
      timers.clear();
      wsRef.current?.close();
    };
  }, [roomId, enabled, loadLatest, applyTimer, onFrame]);

  // ---- sending ----
  const transmit = useCallback(
    (clientId, content) => {
      const ws = wsRef.current;
      if (!ws || ws.readyState !== WebSocket.OPEN) {
        markFailed(clientId, "Not connected");
        return;
      }
      ws.send(JSON.stringify({ type: "chat", content, clientId }));
      clearTimeout(ackTimers.current.get(clientId));
      ackTimers.current.set(
        clientId,
        setTimeout(
          () => markFailed(clientId, "No confirmation from the server"),
          ACK_TIMEOUT_MS,
        ),
      );
    },
    [markFailed],
  );

  const sendChat = useCallback(
    (text) => {
      const clientId = crypto.randomUUID();
      setMessages((prev) =>
        order([
          ...prev,
          {
            id: null,
            clientId,
            senderId: myIdRef.current,
            senderName: "You",
            content: text,
            createdAt: new Date().toISOString(),
            status: "sending",
          },
        ]),
      );
      transmit(clientId, text);
    },
    [transmit],
  );

  // same clientId: the server stores it only once, even if the first try did arrive
  const retryChat = useCallback(
    (clientId, content) => {
      setMessages((prev) =>
        prev.map((m) =>
          m.clientId === clientId
            ? { ...m, status: "sending", error: undefined }
            : m,
        ),
      );
      transmit(clientId, content);
    },
    [transmit],
  );

  const setChatVisible = useCallback((visible) => {
    chatVisibleRef.current = visible;
    if (visible) setUnread(0);
  }, []);

  return {
    status,
    messages,
    hasMore,
    presence,
    timer,
    notesEvent,
    challengeTick,
    roomTick,
    openCount,
    reward,
    lastError,
    unread,
    sendChat,
    retryChat,
    loadEarlier,
    applyTimer,
    setChatVisible,
  };
}
