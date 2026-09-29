import api from "./axios";

export const fetchSkills = () => api.get("/skill/getskill");
