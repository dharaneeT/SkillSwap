import { useState } from "react";
import { Link, NavLink, useNavigate } from "react-router-dom";
import { useAuth } from "../hooks/useAuth";
import NotificationCenter from "./NotificationCenter";
import Icon from "./Icon";

const NAV = [
  { to: "/dashboard", label: "Dashboard", icon: "layout-dashboard" },
  { to: "/skills", label: "Skills", icon: "book-open" },
  { to: "/chat", label: "Chat", icon: "message-circle" },
  { to: "/matches", label: "Matches", icon: "users" },
  { to: "/profile", label: "Profile", icon: "user" },
  { to: "/leaderboard", label: "Leaderboard", icon: "trophy" },
];

const linkClass = ({ isActive }) => `nav-link${isActive ? " active" : ""}`;

function Header() {
  const { user, isAdmin, logout } = useAuth();
  //note Gives the component a function that can programmatically change routes.
  const navigate = useNavigate();
  const [menuOpen, setMenuOpen] = useState(false); // mobile menu (visual only)

  const handleLogout = () => {
    logout();
    navigate("/login");
  };

  const closeMenu = () => setMenuOpen(false);

  return (
    <header className="sticky top-0 z-40 bg-cream px-3 pb-2 pt-3 sm:px-6 sm:pt-4">
      <div className="mx-auto max-w-6xl rounded-full border border-line bg-white px-3 py-2 sm:px-4">
        <div className="flex items-center justify-between gap-3">
          <Link
            to="/"
            onClick={closeMenu}
            className="flex items-center gap-2 pl-1 text-lg font-extrabold tracking-tight text-ink"
          >
            <span className="flex h-8 w-8 items-center justify-center rounded-full bg-brand text-white">
              <Icon name="repeat" size={16} />
            </span>
            SkillSwap
          </Link>

          {user ? (
            <>
              <nav
                aria-label="Main"
                className="hidden items-center gap-1 lg:flex"
              >
                {NAV.map((n) => (
                  <NavLink key={n.to} to={n.to} className={linkClass}>
                    <Icon name={n.icon} size={16} />
                    {n.label}
                  </NavLink>
                ))}
                {isAdmin && (
                  <NavLink to="/admin" className={linkClass}>
                    <Icon name="shield-check" size={16} />
                    Admin
                  </NavLink>
                )}
              </nav>

              <div className="flex items-center gap-2">
                <NotificationCenter />
                <span
                  className="hidden max-w-48 items-center gap-2 truncate rounded-full bg-surface px-3 py-2 text-xs font-medium text-muted xl:inline-flex"
                  title={user.email}
                >
                  <Icon name="user" size={14} />
                  <span className="truncate">{user.email}</span>
                </span>
                <button
                  onClick={handleLogout}
                  className="btn btn-secondary btn-sm hidden lg:inline-flex"
                >
                  <Icon name="log-out" size={15} />
                  Logout
                </button>
                <button
                  type="button"
                  onClick={() => setMenuOpen((o) => !o)}
                  aria-label={menuOpen ? "Close menu" : "Open menu"}
                  aria-expanded={menuOpen}
                  title={menuOpen ? "Close menu" : "Open menu"}
                  className="btn-icon lg:hidden"
                >
                  <Icon name={menuOpen ? "x" : "menu"} size={20} />
                </button>
              </div>
            </>
          ) : (
            <div className="flex items-center gap-2">
              <Link to="/login" className="btn btn-ghost btn-sm">
                <Icon name="log-in" size={15} />
                Login
              </Link>
              <Link to="/signup" className="btn btn-primary btn-sm">
                Sign up
              </Link>
            </div>
          )}
        </div>

        {user && menuOpen && (
          <nav
            aria-label="Mobile"
            className="mt-3 flex flex-col gap-1 border-t border-line pt-3 lg:hidden"
          >
            {NAV.map((n) => (
              <NavLink
                key={n.to}
                to={n.to}
                onClick={closeMenu}
                className={linkClass}
              >
                <Icon name={n.icon} size={17} />
                {n.label}
              </NavLink>
            ))}
            {isAdmin && (
              <NavLink to="/admin" onClick={closeMenu} className={linkClass}>
                <Icon name="shield-check" size={17} />
                Admin
              </NavLink>
            )}
            <p className="meta flex items-center gap-2 px-3 py-2">
              <Icon name="mail" size={15} />
              <span className="truncate">{user.email}</span>
            </p>
            <button
              onClick={() => {
                closeMenu();
                handleLogout();
              }}
              className="btn btn-secondary mt-1"
            >
              <Icon name="log-out" size={16} />
              Logout
            </button>
          </nav>
        )}
      </div>
    </header>
  );
}

export default Header;
