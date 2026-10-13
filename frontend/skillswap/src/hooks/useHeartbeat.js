// hooks/useHeartbeat.js  (NEW FILE)
// Tells the server every 30s that this user has the app open. Pauses while the tab is hidden,
// so a forgotten background tab goes "offline" after ~75s instead of staying online forever.
import { useEffect } from "react";
import { sendHeartbeat } from "../api/presenceApi";
import { usePolling } from "./usePolling";

const ping = () => sendHeartbeat().catch(() => {});

export function useHeartbeat(enabled) {
  // first ping right away (usePolling only fires after the first interval)
  useEffect(() => {
    if (enabled && !document.hidden) ping();
  }, [enabled]);

  // usePolling skips hidden tabs and also fires when the tab becomes visible again
  usePolling(ping, 30000, { enabled });
}
