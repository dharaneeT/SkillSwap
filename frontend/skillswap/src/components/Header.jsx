import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../hooks/useAuth";
import NotificationCenter from "./NotificationCenter";

function Header() {
  const { user, isAdmin, logout } = useAuth();
  //note Gives the component a function that can programmatically change routes.
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate("/login");
  };

  return (
    <header>
      <div className="flex justify-between items-center p-2">
        <h4 className=" font-bold text-red-100 text-2xl ml-2">
          <Link to="/">SKILLSWAP</Link>
        </h4>
        <ul className="flex gap-5 items-center mr-3">
          {user ? (
            <>
              <li>
                <Link to="/dashboard">Dashboard</Link>
              </li>
              <li>
                <Link to="/skills">Skills</Link>
              </li>
              <li>
                <Link to="/chat">Chat</Link>
              </li>
              <li>
                <Link to="/matches">Matches</Link>
              </li>
              <li>
                <Link to="/profile">Profile</Link>
              </li>
              <li>
                <NotificationCenter />
              </li>
              <li className="text-slate-400 text-sm">{user.email}</li>
              {isAdmin && (
                <li>
                  <Link to="/admin" className="text-red-200">
                    Admin
                  </Link>
                </li>
              )}

              <li>
                <button onClick={handleLogout} className="cursor-pointer">
                  Logout
                </button>
              </li>
            </>
          ) : (
            <>
              <li>
                <Link to="/login">Login</Link>
              </li>
              <li>
                <Link to="/signup">Sign up</Link>
              </li>
            </>
          )}
        </ul>
      </div>
    </header>
  );
}

export default Header;
