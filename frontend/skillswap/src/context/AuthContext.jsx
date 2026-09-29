import { createContext, useContext, useState } from "react";
import { loginRequest, signupRequest } from "./api/authApi";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    try {
      return JSON.parse(localStorage.getItem("user"));
    } catch {
      return null;
    }
  });

  const saveSession = (auth) => {
    const u = { email: auth.email, role: auth.role };
    localStorage.setItem("token", auth.token);
    localStorage.setItem("user", JSON.stringify(u));
    setUser(u);
  };

  const login = async (credentials) => {
    const res = await loginRequest(credentials);
    saveSession(res.data.data); // res.data = ApiResponse, .data = AuthResponseDTO
  };

  const signup = async (form) => {
    const res = await signupRequest(form);
    saveSession(res.data.data); // your backend returns a token on signup too
  };

  const logout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("user");
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, login, signup, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

// eslint-disable-next-line react-refresh/only-export-components
export const useAuth = () => useContext(AuthContext);
