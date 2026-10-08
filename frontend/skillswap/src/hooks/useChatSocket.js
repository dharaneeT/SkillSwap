import { useCallback, useEffect, useRef, useState } from "react";

export function useChatSocket(onMessage) {
  const [status, setStatus] = useState("connecting"); // connecting | open | closed
  const wsRef = useRef(null);
  const handlerRef = useRef(onMessage);

  useEffect(() => {
    handlerRef.current = onMessage; // always call the latest handler
  });

  useEffect(() => {
    let stopped = false;
    let attempt = 0;
    let timer;

    const connect = () => {
      const token = encodeURIComponent(localStorage.getItem("token") ?? "");
      const ws = new WebSocket(`${import.meta.env.VITE_WS_URL}?token=${token}`);
      wsRef.current = ws;

      ws.onopen = () => {
        attempt = 0;
        setStatus("open");
      };
      ws.onmessage = (ev) => {
        try {
          handlerRef.current(JSON.parse(ev.data));
        } catch {
          /* ignore malformed frames */
        }
      };
      ws.onclose = () => {
        setStatus("closed");
        if (stopped) return;
        const delay = Math.min(1000 * 2 ** attempt++, 15000); // 1s, 2s, 4s ... 15s
        timer = setTimeout(() => {
          setStatus("connecting");
          connect();
        }, delay);
      };
    };

    connect();
    return () => {
      stopped = true;
      clearTimeout(timer);
      wsRef.current?.close();
    };
  }, []);

  const send = useCallback((payload) => {
    const ws = wsRef.current;
    if (!ws || ws.readyState !== WebSocket.OPEN) return false;
    ws.send(JSON.stringify(payload));
    return true;
  }, []);

  return { status, send };
}
