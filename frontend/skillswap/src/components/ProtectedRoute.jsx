import { Navigate, Outlet, useLocation } from "react-router-dom";
import { useAuth } from "../hooks/useAuth";

export default function ProtectedRoute({ adminOnly = false }) {
  const { user, isAdmin } = useAuth();
  //note Gets the current URL.
  const location = useLocation();

  if (!user) {
    // remember where they were going so login can send them back
    return <Navigate to="/login" replace state={{ from: location }} />;
  }
  if (adminOnly && !isAdmin) return <Navigate to="/dashboard" replace />;
  return <Outlet />;
}
