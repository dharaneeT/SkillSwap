// api/userApi.js
import api from "./axios";

export const fetchMe = () => api.get("/users/me");
export const fetchUserById = (id) => api.get(`/users/${id}`);
