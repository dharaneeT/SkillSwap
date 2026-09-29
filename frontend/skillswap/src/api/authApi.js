import api from "./axios";

export const signupRequest = (data) => api.post("/auth/signup", data);
export const loginRequest = (data) => api.post("/auth/login", data);
