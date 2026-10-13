// hooks/useNow.js  (NEW FILE)  current time in ms, re-rendered every `intervalMs` (for countdowns)
import { useEffect, useState } from "react";

export function useNow(intervalMs = 30000) {
  const [now, setNow] = useState(() => Date.now());
  useEffect(() => {
    const id = setInterval(() => setNow(Date.now()), intervalMs);
    return () => clearInterval(id);
  }, [intervalMs]);
  return now;
}
