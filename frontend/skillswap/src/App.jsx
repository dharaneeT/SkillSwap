import { Routes, Route, Navigate } from "react-router-dom";
import Header from "./components/Header";
import { useAuth } from "./hooks/useAuth"; // NEW
import { useHeartbeat } from "./hooks/useHeartbeat"; // NEW
import ProtectedRoute from "./components/ProtectedRoute"; // lowercase "components"
import HomePage from "./pages/HomePage";
import LoginPage from "./pages/LoginPage";
import SignupPage from "./pages/SignupPage";
import DashboardPage from "./pages/DashboardPage";
import ProfilePage from "./pages/ProfilePage";
import ChatPage from "./pages/ChatPage";
import AdminPage from "./pages/AdminPage";
import SkillsListPage from "./pages/SkillsListPage";
import MatchesPage from "./pages/MatchesPage";
import LeaderboardPage from "./pages/LeaderboardPage";
import OnboardingPage from "./pages/OnboardingPage";
import CertificatesPage from "./pages/CertificatesPage";
import CertificateViewPage from "./pages/CertificateViewPage";

function App() {
  const { isAuthenticated } = useAuth(); // NEW
  useHeartbeat(isAuthenticated); // NEW: tells the server "I'm online" every 30s while logged in

  return (
    <div className="min-h-screen bg-cream font-sans text-ink">
      <Header />
      <Routes>
        {/* public */}
        <Route path="/" element={<HomePage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/signup" element={<SignupPage />} />

        {/* logged-in users */}
        <Route element={<ProtectedRoute />}>
          <Route path="/dashboard" element={<DashboardPage />} />
          <Route path="/profile" element={<ProfilePage />} />
          <Route path="/skills" element={<SkillsListPage />} />
          <Route path="/chat" element={<ChatPage />} />
          <Route path="/chat/:userId" element={<ChatPage />} />
          <Route path="/matches" element={<MatchesPage />} />
          <Route path="/leaderboard" element={<LeaderboardPage />} />
          <Route path="/onboarding" element={<OnboardingPage />} />
          <Route path="/certificates" element={<CertificatesPage />} />
          <Route path="/certificates/:code" element={<CertificateViewPage />} />
        </Route>

        {/* admins only */}
        <Route element={<ProtectedRoute adminOnly />}>
          <Route path="/admin" element={<AdminPage />} />
        </Route>

        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </div>
  );
}

export default App;
