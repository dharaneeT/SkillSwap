import { useCallback, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { fetchMe } from "../api/userApi";
import { fetchSkills } from "../api/skillApi";
import { fetchMatches } from "../api/matchApi";
import { bookSession, fetchMySessions, updateSession } from "../api/sessionApi";
import { getErrorMessage } from "../api/axios";

const card = "rounded-xl bg-slate-800 border border-slate-700 p-4";
const btn = "rounded-lg px-3 py-1 text-sm font-semibold cursor-pointer";

export default function DashboardPage() {
  const [me, setMe] = useState(null);
  const [sessions, setSessions] = useState([]);
  const [matches, setMatches] = useState([]);
  const [skills, setSkills] = useState([]);
  const [times, setTimes] = useState({}); // "userId-skill" -> datetime-local value
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);

  const loadAll = useCallback(async () => {
    const meRes = await fetchMe();
    const profile = meRes.data.data;
    const [s, m, sk] = await Promise.all([
      fetchMySessions(),
      fetchMatches(profile.id),
      fetchSkills(),
    ]);
    setMe(profile);
    setSessions(s.data.data);
    setMatches(m.data.data);
    setSkills(sk.data.data);
  }, []);

  useEffect(() => {
    loadAll()
      .catch((e) => setError(getErrorMessage(e)))
      .finally(() => setLoading(false));
  }, [loadAll]);

  const handleBook = async (match) => {
    setError("");
    const key = `${match.userId}-${match.matchedSkill}`;
    const skill = skills.find((s) => s.name === match.matchedSkill);
    if (!skill || !times[key]) {
      setError("Pick a date and time first");
      return;
    }
    try {
      await bookSession({
        providerId: match.userId,
        skillId: skill.id,
        sessionTime: times[key], // e.g. 2026-10-05T11:00
      });
      await loadAll();
    } catch (e) {
      setError(getErrorMessage(e));
    }
  };

  const handleStatus = async (id, status) => {
    setError("");
    try {
      await updateSession(id, status);
      await loadAll();
    } catch (e) {
      setError(getErrorMessage(e));
    }
  };

  if (loading)
    return (
      <main className="max-w-4xl mx-auto px-4 pt-10 text-slate-400">
        Loading...
      </main>
    );

  return (
    <main className="max-w-4xl mx-auto px-4 pt-10 space-y-10">
      <header className="flex justify-between items-end">
        <h1 className="text-2xl font-bold">Hi {me?.name}</h1>
        <p>
          <span className="text-3xl font-bold text-red-200">{me?.credits}</span>{" "}
          credits
        </p>
      </header>

      {error && (
        <p className="rounded-lg bg-red-900/50 border border-red-500 px-3 py-2 text-sm">
          {error}
        </p>
      )}

      <section>
        <h2 className="text-xl font-semibold mb-3">
          Matches for what you want to learn
        </h2>
        {matches.length === 0 && (
          <p className="text-slate-400 text-sm">
            No matches yet. Add skills you want on your{" "}
            <Link to="/profile" className="underline text-red-200">
              profile
            </Link>
            .
          </p>
        )}
        <ul className="grid sm:grid-cols-2 gap-4">
          {matches.map((m) => {
            const key = `${m.userId}-${m.matchedSkill}`;
            return (
              <li key={key} className={`${card} space-y-3`}>
                <p>
                  <b>{m.userName}</b> teaches{" "}
                  <span className="text-red-200">{m.matchedSkill}</span>
                </p>
                <input
                  type="datetime-local"
                  className="w-full rounded-lg bg-slate-900 border border-slate-600 px-2 py-1 text-sm"
                  value={times[key] || ""}
                  onChange={(e) =>
                    setTimes({ ...times, [key]: e.target.value })
                  }
                />
                <div className="flex gap-2">
                  <button
                    onClick={() => handleBook(m)}
                    className={`${btn} bg-red-200 text-slate-900`}
                  >
                    Book
                  </button>
                  <Link
                    to={`/chat/${m.userId}`}
                    className={`${btn} border border-slate-600`}
                  >
                    Chat
                  </Link>
                </div>
              </li>
            );
          })}
        </ul>
      </section>

      <section>
        <h2 className="text-xl font-semibold mb-3">My sessions</h2>
        {sessions.length === 0 && (
          <p className="text-slate-400 text-sm">No sessions yet.</p>
        )}
        <ul className="space-y-3">
          {sessions.map((s) => {
            const isProvider = s.providerId === me?.id;
            const isLearner = s.learnerId === me?.id;
            const open = ["PENDING", "REQUESTED"].includes(s.status);
            return (
              <li
                key={s.id}
                className={`${card} flex flex-wrap justify-between items-center gap-3`}
              >
                <div>
                  <p className="font-semibold">
                    {s.skillName} —{" "}
                    {isProvider
                      ? `teaching ${s.learnerName}`
                      : `learning from ${s.providerName}`}
                  </p>
                  <p className="text-sm text-slate-400">
                    {s.sessionTime
                      ? new Date(s.sessionTime).toLocaleString()
                      : "no time set"}{" "}
                    · {s.status}
                  </p>
                </div>
                <div className="flex gap-2">
                  {isProvider && open && (
                    <button
                      onClick={() => handleStatus(s.id, "ACCEPTED")}
                      className={`${btn} bg-green-300 text-slate-900`}
                    >
                      Accept
                    </button>
                  )}
                  {isProvider && s.status === "ACCEPTED" && (
                    <button
                      onClick={() => handleStatus(s.id, "COMPLETED")}
                      className={`${btn} bg-red-200 text-slate-900`}
                    >
                      Mark completed
                    </button>
                  )}
                  {isLearner && (open || s.status === "ACCEPTED") && (
                    <button
                      onClick={() => handleStatus(s.id, "CANCELLED")}
                      className={`${btn} border border-slate-600`}
                    >
                      Cancel
                    </button>
                  )}
                </div>
              </li>
            );
          })}
        </ul>
      </section>
    </main>
  );
}
