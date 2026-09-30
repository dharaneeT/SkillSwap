// api/userSkillApi.js
import api from "./axios";

export const addUserSkill = (data) => api.post("/user-skill", data);
