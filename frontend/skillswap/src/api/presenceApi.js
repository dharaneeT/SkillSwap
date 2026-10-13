// api/presenceApi.js  (NEW FILE)
import api from "./axios";

// "I'm here" ping, sent every ~30s while the app is open
export const sendHeartbeat = () => api.post("/presence/heartbeat");

// ids = [1, 2, 3]  ->  [{ userId, online, lastSeenAt }]
export const fetchPresence = (ids) =>
  api.get("/presence", { params: { ids: ids.join(",") } });
