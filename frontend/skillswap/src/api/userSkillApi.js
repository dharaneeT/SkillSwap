import api from "./axios";

export const addUserSkill = (data) => api.post("/user-skill", data);
export const addUserSkillByName = (data) =>
  api.post("/user-skill/by-name", data);
