import { Routes, Route, Navigate } from "react-router-dom";
import Header from "./components/Header";
import ProtectedRoute from "./Components/ProtectedRoute";
import LoginPage from "./pages/LoginPage";
import SignupPage from "./pages/SignupPage";
import SkillsListPage from "./pages/SkillsListPage";

function App() {
  return (
    <div className="montserrat min-h-screen text-white bg-slate-900">
      <Header />
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/signup" element={<SignupPage />} />
        <Route element={<ProtectedRoute />}>
          <Route path="/skills" element={<SkillsListPage />} />
        </Route>
        <Route path="*" element={<Navigate to="/skills" replace />} />
      </Routes>
    </div>
  );
}

export default App;
