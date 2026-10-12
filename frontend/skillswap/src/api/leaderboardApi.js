import api from "./axios";

export const fetchLeaderboard = (limit = 20) =>
  api.get("/leaderboard", { params: { limit } });

export const fetchMyStanding = () => api.get("/leaderboard/me");
