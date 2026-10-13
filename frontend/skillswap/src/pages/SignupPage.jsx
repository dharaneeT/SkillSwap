import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../hooks/useAuth";
import { getErrorMessage } from "../api/axios";
import Icon from "../components/Icon";

const inputClass = "input input-with-icon";

export default function SignupPage() {
  const { signup } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({ name: "", email: "", password: "" });
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const [showPassword, setShowPassword] = useState(false);

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
      navigate("/onboarding", { replace: true }); // NEW: was "/skills"
    } catch (err) {
      setError(getErrorMessage(err)); // e.g. "Email is already registered"
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
          <h1 className="page-title text-4xl">Create account</h1>
          <p className="page-sub text-sm">
            Start trading skills with people near you.
          </p>
        </div>

        {error && (
          <p role="alert" className="alert alert-error">
            <Icon name="alert-circle" size={18} className="mt-0.5" />
            <span>{error}</span>
          </p>
        )}

        <div className="relative">
          <Icon name="user" size={17} className="input-icon" />
          <input
            className={inputClass}
            name="name"
            placeholder="Name"
            aria-label="Name"
            value={form.name}
            onChange={handleChange}
            required
          />
        </div>
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
            placeholder="Password (min 8 chars)"
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
          <Icon name="user-plus" size={16} />
          {loading ? "Creating..." : "Sign up"}
        </button>

        <p className="meta text-center">
          Already registered?{" "}
          <Link to="/login" className="link">
            Log in
          </Link>
        </p>
      </form>
    </main>
  );
}
