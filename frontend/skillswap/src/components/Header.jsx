import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

function Header() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate("/login");
  };

  return (
    <header>
      <div className="flex justify-between items-center p-2">
        <h4 className="sedgwick-ave-display-regular font-bold text-red-100 text-2xl ml-2">
          <Link to="/">SKILLSWAP</Link>
        </h4>
        <ul className="flex gap-5 items-center mr-3">
          {user ? (
            <>
              <li>
                <Link to="/skills">Skills</Link>
              </li>
              <li className="text-slate-400 text-sm">{user.email}</li>
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
