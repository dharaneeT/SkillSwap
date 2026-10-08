import { useEffect, useRef } from "react";

export function usePolling(callback, intervalMs, { enabled = true } = {}) {
  const cbRef = useRef(callback);
  useEffect(() => {
    cbRef.current = callback;
  });

  useEffect(() => {
    if (!enabled) return;
    const run = () => {
      if (!document.hidden) cbRef.current();
    };
    const id = setInterval(run, intervalMs);
    document.addEventListener("visibilitychange", run); // fires on tab focus too
    return () => {
      clearInterval(id);
      document.removeEventListener("visibilitychange", run);
    };
  }, [intervalMs, enabled]);
}
