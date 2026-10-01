import api from "./axios";

export const fetchSkills = () => api.get("/skill/getskill"); // still used by ProfilePage
export const searchSkills = (params) => api.get("/skills", { params });
