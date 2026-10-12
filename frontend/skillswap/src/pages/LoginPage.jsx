import { useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "../hooks/useAuth";
import { getErrorMessage } from "../api/axios";
import Icon from "../components/Icon";

const inputClass = "input input-with-icon";

export default function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const from = useLocation().state?.from?.pathname || "/dashboard";
  const [form, setForm] = useState({ email: "", password: "" });
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const [showPassword, setShowPassword] = useState(false);

  const handleChange = (e) =>
    setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setLoading(true);
    try {
      await login(form);
      navigate(from, { replace: true }); // only after login succeeds; goes back to the page they wanted
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  return (
    <main className="page flex max-w-md justify-center pt-8 sm:pt-16">
      <form
        onSubmit={handleSubmit}
        className="card card-white w-full space-y-4 sm:p-8"
      >
        <div className="space-y-1 pb-2">
          <h1 className="page-title text-4xl">Log in</h1>
          <p className="page-sub text-sm">Welcome back to SkillSwap.</p>
        </div>

        {error && (
          <p role="alert" className="alert alert-error">
            <Icon name="alert-circle" size={18} className="mt-0.5" />
            <span>{error}</span>
          </p>
        )}

        <div className="relative">
          <Icon name="mail" size={17} className="input-icon" />
          <input
            className={inputClass}
            type="email"
            name="email"
            placeholder="Email"
            aria-label="Email"
            value={form.email}
            onChange={handleChange}
            required
          />
        </div>
        <div className="relative">
          <Icon name="lock" size={17} className="input-icon" />
          <input
            className={`${inputClass} pr-12`}
            type={showPassword ? "text" : "password"}
            name="password"
            placeholder="Password"
            aria-label="Password"
            value={form.password}
            onChange={handleChange}
            required
          />
          <button
            type="button"
            onClick={() => setShowPassword((v) => !v)}
            aria-label={showPassword ? "Hide password" : "Show password"}
            title={showPassword ? "Hide password" : "Show password"}
            className="input-action"
          >
            <Icon name={showPassword ? "eye-off" : "eye"} size={17} />
          </button>
        </div>

        <button disabled={loading} className="btn btn-primary w-full">
          <Icon name="log-in" size={16} />
          {loading ? "Logging in..." : "Log in"}
        </button>

        <p className="meta text-center">
          No account?{" "}
          <Link to="/signup" className="link">
            Sign up
          </Link>
        </p>
      </form>
    </main>
  );
}
