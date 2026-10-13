// hooks/usePresence.js  (NEW FILE)
// usePresence([1, 2, 3]) -> { 1: { userId, online, lastSeenAt }, ... }, refreshed every 20s.
import { useCallback, useEffect, useState } from "react";
import { fetchPresence } from "../api/presenceApi";
import { usePolling } from "./usePolling";

export function usePresence(ids) {
  // stable string key, so a new array with the same ids doesn't trigger a refetch
  const key = [...new Set(ids.filter(Boolean))].sort((a, b) => a - b).join(",");
  const [map, setMap] = useState({});

  const load = useCallback(async () => {
    if (!key) return;
    try {
      const res = await fetchPresence(key.split(",").map(Number));
      setMap(Object.fromEntries(res.data.data.map((p) => [p.userId, p])));
    } catch {
      /* presence is decoration: never show an error for it */
    }
  }, [key]);

  useEffect(() => {
    load();
  }, [load]);

  usePolling(load, 20000, { enabled: Boolean(key) });

  return map;
}
