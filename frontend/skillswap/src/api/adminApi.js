// api/adminApi.js
import api from "./axios";

export const fetchUsers = () => api.get("/admin/users");
export const activateUser = (id) => api.put(`/admin/users/${id}/activate`);
export const deactivateUser = (id) => api.put(`/admin/users/${id}/deactivate`);
export const setUserRole = (id, role) =>
  api.put(`/admin/users/${id}/role`, null, { params: { role } });
export const deleteSkill = (id) => api.delete(`/admin/skills/${id}`);
export const addCredits = (userId, credits) =>
  api.post(`/credits/${userId}`, null, { params: { credits } });
