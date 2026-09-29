import axios from "axios";

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL,
  headers: { "Content-Type": "application/json" },
});

// Attach JWT to every request
api.interceptors.request.use((config) => {
  const token = localStorage.getItem("token");
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

// Expired/invalid token -> log out. (Ignore /auth calls: a wrong password is also 401.)
api.interceptors.response.use(
  (res) => res,
  (err) => {
    const isAuthCall = err.config?.url?.startsWith("/auth");
    if (err.response?.status === 401 && !isAuthCall) {
      localStorage.removeItem("token");
      localStorage.removeItem("user");
      window.location.href = "/login";
    }
    return Promise.reject(err);
  },
);

// Turns any axios error into a message you can show
export const getErrorMessage = (err) => {
  if (err.response?.data?.message) return err.response.data.message; // from your ErrorResponseDTO
  if (err.request) return "Cannot reach the server. Is the backend running?";
  return "Something went wrong";
};

export default api;
