// api/roadmapApi.js  (NEW FILE)
import api from "./axios";

// generation can take a while, so this call gets a longer timeout
export const generateRoadmap = (data) =>
  api.post("/roadmaps/generate", data, { timeout: 90000 });

export const fetchRoadmapTemplates = () => api.get("/roadmaps/templates");
export const fetchTemplateDraft = (id, skill) =>
  api.get(`/roadmaps/templates/${encodeURIComponent(id)}/draft`, {
    params: skill ? { skill } : {},
  });

export const saveRoadmap = (draft) => api.post("/roadmaps", draft);
export const fetchRoadmaps = () => api.get("/roadmaps");
export const fetchRoadmapDashboard = () => api.get("/roadmaps/dashboard");
export const fetchRoadmap = (id) => api.get(`/roadmaps/${id}`);
export const updateRoadmap = (id, data) => api.put(`/roadmaps/${id}`, data);
export const deleteRoadmap = (id) => api.delete(`/roadmaps/${id}`);

export const updateMilestone = (id, milestoneId, data) =>
  api.put(`/roadmaps/${id}/milestones/${milestoneId}`, data);
export const setMilestoneDone = (id, milestoneId, completed) =>
  api.patch(`/roadmaps/${id}/milestones/${milestoneId}`, { completed });
export const setTaskDone = (id, taskId, completed) =>
  api.patch(`/roadmaps/${id}/tasks/${taskId}`, { completed });
