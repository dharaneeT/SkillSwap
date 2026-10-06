import { createContext, useCallback, useMemo, useState } from "react";
import { useDispatch } from "react-redux";
import { loginRequest, signupRequest } from "../api/authApi";
import { skillsCleared } from "../store/skillsSlice";
import { sessionsCleared } from "../store/sessionsSlice";

// eslint-disable-next-line react-refresh/only-export-components
export const AuthContext = createContext(null);

function readStoredUser() {
  try {
    return JSON.parse(localStorage.getItem("user"));
  } catch {
    return null;
  }
}

export function AuthProvider({ children }) {
  const dispatch = useDispatch();
  const [user, setUser] = useState(readStoredUser);

  const saveSession = useCallback((auth) => {
    const u = { email: auth.email, role: auth.role };
    localStorage.setItem("token", auth.token); // axios.js reads this key
    localStorage.setItem("user", JSON.stringify(u));
    setUser(u);
  }, []);

  const login = useCallback(
    async (credentials) => {
      const res = await loginRequest(credentials);
      saveSession(res.data.data); // ApiResponse.data = AuthResponseDTO
    },
    [saveSession],
  );

  const signup = useCallback(
    async (form) => {
      const res = await signupRequest(form);
      saveSession(res.data.data);
    },
    [saveSession],
  );

  const logout = useCallback(() => {
    localStorage.removeItem("token");
    localStorage.removeItem("user");
    setUser(null);
    dispatch(skillsCleared()); // wipe Redux data on logout
    dispatch(sessionsCleared());
  }, [dispatch]);

  // useMemo: consumers only re-render when something here actually changes
  const value = useMemo(
    () => ({
      user,
      isAuthenticated: Boolean(user),
      isAdmin: user?.role === "ROLE_ADMIN", // backend sends the Spring authority string
      login,
      signup,
      logout,
    }),
    [user, login, signup, logout],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
