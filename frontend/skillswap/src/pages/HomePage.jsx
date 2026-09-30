import { Link } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export default function HomePage() {
  const { user } = useAuth();
  return (
    <main className="max-w-3xl mx-auto px-4 pt-20 text-center space-y-6">
      <h1 className="text-4xl font-bold">Trade skills, not money.</h1>
      <p className="text-slate-400">
        List what you can teach, what you want to learn, and get matched with
        people who complete the swap. Sessions are paid in credits.
      </p>
      <div className="flex justify-center gap-4">
        {user ? (
          <Link
            to="/dashboard"
            className="rounded-lg bg-red-200 text-slate-900 font-semibold px-5 py-2"
          >
            Go to dashboard
          </Link>
        ) : (
          <>
            <Link
              to="/signup"
              className="rounded-lg bg-red-200 text-slate-900 font-semibold px-5 py-2"
            >
              Get started
            </Link>
            <Link
              to="/login"
              className="rounded-lg border border-slate-600 px-5 py-2"
            >
              Log in
            </Link>
          </>
        )}
      </div>
    </main>
  );
}
