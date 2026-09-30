// api/chatApi.js
import api from "./axios";

export const fetchPartners = () => api.get("/chat/partners");
export const fetchConversation = (userId) => api.get(`/chat/with/${userId}`);
export const sendMessage = (data) => api.post("/chat/send", data);
