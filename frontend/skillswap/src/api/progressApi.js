// api/progressApi.js  (NEW FILE)
import api from "./axios";

// learner stats + streaks: { sessionsLearned, skillsLearned, currentStreakWeeks,
//                            longestStreakWeeks, activeThisWeek, certificates }
export const fetchMyProgress = () => api.get("/progress/me");

export const fetchMyCertificates = () => api.get("/certificates/my");
export const fetchCertificate = (code) =>
  api.get(`/certificates/${encodeURIComponent(code)}`);
