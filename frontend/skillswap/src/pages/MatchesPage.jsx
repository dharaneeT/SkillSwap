import { useEffect, useMemo, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { Link } from "react-router-dom";
import { fetchMe } from "../api/userApi";
import { fetchMatches } from "../api/matchApi";
import { useFetch } from "../hooks/useFetch";
import { loadSkills } from "../store/skillsSlice";
import BookingModal from "../components/BookingModal";

function rankMatches(rows) {
  const byUser = new Map();
  for (const r of rows) {
    const entry = byUser.get(r.userId) ?? {
      userId: r.userId,
      userName: r.userName,
      skills: [],
    };
    if (!entry.skills.includes(r.matchedSkill))
      entry.skills.push(r.matchedSkill);
    byUser.set(r.userId, entry);
  }
  return [...byUser.values()]
    .sort(
      (a, b) =>
        b.skills.length - a.skills.length ||
        a.userName.localeCompare(b.userName),
    )
    .map((m, i) => ({ ...m, rank: i + 1 })); // rank is fixed before filtering
}

const inputClass =
  "rounded-lg bg-slate-900 border border-slate-600 px-3 py-2 text-sm outline-none focus:border-red-200";

export default function MatchesPage() {
  const dispatch = useDispatch();
  const allSkills = useSelector((s) => s.skills.items);

  const { data: me, error: meError } = useFetch(fetchMe);
  const { data: rows, error: matchError } = useFetch(fetchMatches, me?.id, {
    enabled: Boolean(me?.id),
  });

  const [search, setSearch] = useState("");
  const [picked, setPicked] = useState([]); // selected skill chips
  const [minSkills, setMinSkills] = useState(1);
  const [sortBy, setSortBy] = useState("rank");
  const [booking, setBooking] = useState(null); // { provider, skill }
  const [notice, setNotice] = useState("");

  useEffect(() => {
    dispatch(loadSkills());
  }, [dispatch]);

  const ranked = useMemo(() => rankMatches(rows ?? []), [rows]);
  const skillOptions = useMemo(
    () => [...new Set(ranked.flatMap((m) => m.skills))].sort(),
    [ranked],
  );

  const visible = useMemo(() => {
    const q = search.trim().toLowerCase();
    return ranked
      .filter((m) => m.skills.length >= minSkills)
      .filter(
        (m) => picked.length === 0 || m.skills.some((s) => picked.includes(s)),
      )
      .filter(
        (m) =>
          !q ||
          m.userName.toLowerCase().includes(q) ||
          m.skills.some((s) => s.toLowerCase().includes(q)),
      )
      .sort((a, b) =>
        sortBy === "name"
          ? a.userName.localeCompare(b.userName)
          : a.rank - b.rank,
      );
  }, [ranked, search, picked, minSkills, sortBy]);

  const togglePicked = (name) =>
    setPicked((p) =>
      p.includes(name) ? p.filter((x) => x !== name) : [...p, name],
    );

  const clearFilters = () => {
    setSearch("");
    setPicked([]);
    setMinSkills(1);
    setSortBy("rank");
  };

  const startBooking = (match, skillName) => {
    const skill = allSkills.find((s) => s.name === skillName);
    if (!skill) {
      setNotice("Skills are still loading, try again in a moment");
      return;
    }
    setNotice("");
    setBooking({
      provider: { id: match.userId, name: match.userName },
      skill: { id: skill.id, name: skill.name },
    });
  };

  const error = meError || matchError;
  const loading = !rows && !error;

  return (
    <main className="max-w-4xl mx-auto px-4 pt-6 pb-10">
      <h1 className="text-2xl font-bold">Your matches</h1>
      <p className="text-sm text-slate-400">
        People who offer skills you want to learn, best match first.
      </p>

      {error && (
        <p className="mt-4 rounded-lg bg-red-900/50 border border-red-500 px-3 py-2 text-sm">
          {error}
        </p>
      )}
      {notice && (
        <p className="mt-4 rounded-lg bg-slate-700 px-3 py-2 text-sm">
          {notice}
        </p>
      )}

      {/* live filters */}
      <section className="mt-4 rounded-xl bg-slate-800 border border-slate-700 p-4 space-y-3">
        <div className="flex flex-wrap gap-2">
          <input
            className={`${inputClass} flex-1 min-w-48`}
            placeholder="Search by name or skill..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
          <select
            className={inputClass}
            value={minSkills}
            onChange={(e) => setMinSkills(Number(e.target.value))}
          >
            <option value={1}>Any number of skills</option>
            <option value={2}>2+ matching skills</option>
            <option value={3}>3+ matching skills</option>
          </select>
          <select
            className={inputClass}
            value={sortBy}
            onChange={(e) => setSortBy(e.target.value)}
          >
            <option value="rank">Best match</option>
            <option value="name">Name A-Z</option>
          </select>
        </div>

        {skillOptions.length > 0 && (
          <div className="flex flex-wrap gap-2">
            {skillOptions.map((name) => (
              <button
                key={name}
                onClick={() => togglePicked(name)}
                aria-pressed={picked.includes(name)}
                className={`rounded-full px-3 py-1 text-xs cursor-pointer border ${
                  picked.includes(name)
                    ? "bg-red-200 text-slate-900 border-red-200"
                    : "border-slate-600 hover:bg-slate-700"
                }`}
              >
                {name}
              </button>
            ))}
          </div>
        )}
      </section>

      {loading && <p className="mt-6 text-slate-400">Loading matches...</p>}

      {!loading && ranked.length === 0 && !error && (
        <p className="mt-6 text-slate-400">
          No matches yet. Add skills you want to learn on your{" "}
          <Link to="/profile" className="text-red-200 underline">
            profile
          </Link>
          .
        </p>
      )}

      {ranked.length > 0 && (
        <p className="mt-4 text-sm text-slate-400">
          Showing {visible.length} of {ranked.length}
          {visible.length === 0 && (
            <button
              onClick={clearFilters}
              className="ml-2 text-red-200 underline cursor-pointer"
            >
              Clear filters
            </button>
          )}
        </p>
      )}

      <ul className="mt-3 space-y-3">
        {visible.map((m) => (
          <li
            key={m.userId}
            className="rounded-xl bg-slate-800 border border-slate-700 p-4"
          >
            <div className="flex items-center justify-between gap-3">
              <div className="flex items-center gap-3">
                <span className="rounded-full bg-red-200 text-slate-900 text-xs font-bold px-2 py-1">
                  #{m.rank}
                </span>
                <div>
                  <p className="font-semibold">{m.userName}</p>
                  <p className="text-xs text-slate-400">
                    Offers {m.skills.length} skill
                    {m.skills.length > 1 ? "s" : ""} you want
                  </p>
                </div>
              </div>
              <Link
                to={`/chat/${m.userId}`}
                className="rounded-lg border border-slate-600 px-3 py-1 text-sm hover:bg-slate-700"
              >
                Chat
              </Link>
            </div>

            <div className="mt-3 flex flex-wrap gap-2">
              {m.skills.map((name) => (
                <button
                  key={name}
                  onClick={() => startBooking(m, name)}
                  className="rounded-lg bg-slate-700 hover:bg-slate-600 px-3 py-1 text-sm cursor-pointer"
                  title={`Book ${name} with ${m.userName}`}
                >
                  Book {name}
                </button>
              ))}
            </div>
          </li>
        ))}
      </ul>

      {booking && (
        <BookingModal
          provider={booking.provider}
          skill={booking.skill}
          onClose={() => setBooking(null)}
          onBooked={() => setNotice(`Request sent to ${booking.provider.name}`)}
        />
      )}
    </main>
  );
}
