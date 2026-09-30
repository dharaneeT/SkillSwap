import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { getErrorMessage } from "../api/axios";

const inputClass =
  "w-full rounded-lg bg-slate-800 border border-slate-600 px-3 py-2 outline-none focus:border-red-200";

export default function SignupPage() {
  const { signup } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({ name: "", email: "", password: "" });
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const handleChange = (e) =>
    setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    if (form.password.length < 8) {
      setError("Password must be at least 8 characters"); // same rule as SignupRequestDTO
      return;
    }
    setLoading(true);
    try {
      await signup(form);
      navigate("/skills", { replace: true });
    } catch (err) {
      setError(getErrorMessage(err)); // e.g. "Email is already registered"
    } finally {
      setLoading(false);
    }
  };

  return (
    <main className="flex justify-center px-4 pt-16">
      <form onSubmit={handleSubmit} className="w-full max-w-sm space-y-4">
        <h1 className="text-2xl font-bold">Create account</h1>

        {error && (
          <p className="rounded-lg bg-red-900/50 border border-red-500 px-3 py-2 text-sm">
            {error}
          </p>
        )}

        <input
          className={inputClass}
          name="name"
          placeholder="Name"
          value={form.name}
          onChange={handleChange}
          required
        />
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
          placeholder="Password (min 8 chars)"
          value={form.password}
          onChange={handleChange}
          required
        />

        <button
          disabled={loading}
          className="w-full rounded-lg bg-red-200 text-slate-900 font-semibold py-2 disabled:opacity-50 cursor-pointer"
        >
          {loading ? "Creating..." : "Sign up"}
        </button>

        <p className="text-sm text-slate-400">
          Already registered?{" "}
          <Link to="/login" className="text-red-200 underline">
            Log in
          </Link>
        </p>
      </form>
    </main>
  );
}
