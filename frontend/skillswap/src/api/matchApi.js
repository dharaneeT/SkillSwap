// api/matchApi.js
import api from "./axios";

export const fetchMatches = (userId) => api.get(`/match/${userId}`);
