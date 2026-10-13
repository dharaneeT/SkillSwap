// api/adminApi.js   (EXISTING FILE - replaced; the 3 skill-moderation lines at the bottom are NEW)
import api from "./axios";

export const fetchUsers = () => api.get("/admin/users");
export const activateUser = (id) => api.put(`/admin/users/${id}/activate`);
export const deactivateUser = (id) => api.put(`/admin/users/${id}/deactivate`);
export const setUserRole = (id, role) =>
  api.put(`/admin/users/${id}/role`, null, { params: { role } });
export const deleteSkill = (id) => api.delete(`/admin/skills/${id}`);
export const addCredits = (userId, credits) =>
  api.post(`/credits/${userId}`, null, { params: { credits } });

// ---- NEW: skill moderation ----
export const fetchPendingSkills = () => api.get("/admin/skills/pending");
export const approveSkill = (id) => api.put(`/admin/skills/${id}/approve`);
export const mergeSkill = (id, targetId) =>
  api.post(`/admin/skills/${id}/merge`, { targetId });
