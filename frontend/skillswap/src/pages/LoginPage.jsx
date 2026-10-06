import { useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "../hooks/useAuth";
import { getErrorMessage } from "../api/axios";

const inputClass =
  "w-full rounded-lg bg-slate-800 border border-slate-600 px-3 py-2 outline-none focus:border-red-200";

export default function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const from = useLocation().state?.from?.pathname || "/dashboard";
  const [form, setForm] = useState({ email: "", password: "" });
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

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
    <main className="flex justify-center px-4 pt-16">
      <form onSubmit={handleSubmit} className="w-full max-w-sm space-y-4">
        <h1 className="text-2xl font-bold">Log in</h1>

        {error && (
          <p className="rounded-lg bg-red-900/50 border border-red-500 px-3 py-2 text-sm">
            {error}
          </p>
        )}

        <input
          className={inputClass}
          type="email"
          name="email"
          placeholder="Email"
          value={form.email}
          onChange={handleChange}
          required
        />
        <input
          className={inputClass}
          type="password"
          name="password"
          placeholder="Password"
          value={form.password}
          onChange={handleChange}
          required
        />

        <button
          disabled={loading}
          className="w-full rounded-lg bg-red-200 text-slate-900 font-semibold py-2 disabled:opacity-50 cursor-pointer"
        >
          {loading ? "Logging in..." : "Log in"}
        </button>

        <p className="text-sm text-slate-400">
          No account?{" "}
          <Link to="/signup" className="text-red-200 underline">
            Sign up
          </Link>
        </p>
      </form>
    </main>
  );
}
