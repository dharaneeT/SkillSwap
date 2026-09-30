// api/sessionApi.js
import api from "./axios";

export const fetchMySessions = () => api.get("/session/my");
export const bookSession = (data) => api.post("/session/book-session", data);
export const updateSession = (id, status) =>
  api.put(`/session/update/${id}`, { status });
