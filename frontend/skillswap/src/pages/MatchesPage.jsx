import { useEffect, useMemo, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { Link } from "react-router-dom";
import { fetchMe } from "../api/userApi";
import { fetchMatches } from "../api/matchApi";
import { useFetch } from "../hooks/useFetch";
import { loadSkills } from "../store/skillsSlice";
import BookingModal from "../components/BookingModal";
import Icon from "../components/Icon";

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

const inputClass = "input select";

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
    <main className="page max-w-5xl">
      <div className="space-y-2">
        <p className="eyebrow">Matches</p>
        <h1 className="page-title">Your matches</h1>
        <p className="page-sub">
          People who offer skills you want to learn, best match first.
        </p>
      </div>

      {error && (
        <p role="alert" className="alert alert-error mt-6">
          <Icon name="alert-circle" size={18} className="mt-0.5" />
          <span>{error}</span>
        </p>
      )}
      {notice && (
        <p role="status" className="alert alert-info mt-6">
          <Icon name="circle-check" size={18} className="mt-0.5" />
          <span>{notice}</span>
        </p>
      )}

      {/* live filters */}
      <section
        aria-label="Filters"
        className="card card-lavender mt-8 space-y-4"
      >
        <div className="flex flex-wrap items-center gap-3">
          <div className="relative min-w-52 flex-1">
            <Icon name="search" size={17} className="input-icon" />
            <input
              className="input input-with-icon"
              placeholder="Search by name or skill..."
              aria-label="Search by name or skill"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
            />
          </div>
          <select
            className={inputClass}
            aria-label="Minimum matching skills"
            value={minSkills}
            onChange={(e) => setMinSkills(Number(e.target.value))}
          >
            <option value={1}>Any number of skills</option>
            <option value={2}>2+ matching skills</option>
            <option value={3}>3+ matching skills</option>
          </select>
          <select
            className={inputClass}
            aria-label="Sort"
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
                className="chip"
              >
                {picked.includes(name) && (
                  <Icon name="check" size={13} strokeWidth={3} />
                )}
                {name}
              </button>
            ))}
          </div>
        )}
      </section>

      {loading && <p className="mt-8 text-muted">Loading matches...</p>}

      {!loading && ranked.length === 0 && !error && (
        <p className="card mt-8 flex flex-wrap items-center gap-x-1 text-sm text-muted">
          No matches yet. Add skills you want to learn on your{" "}
          <Link to="/profile" className="link">
            profile
          </Link>
          .
        </p>
      )}

      {ranked.length > 0 && (
        <p className="meta mt-6 flex items-center gap-2">
          Showing {visible.length} of {ranked.length}
          {visible.length === 0 && (
            <button
              onClick={clearFilters}
              className="link inline-flex items-center gap-1"
            >
              <Icon name="x" size={13} />
              Clear filters
            </button>
          )}
        </p>
      )}

      <ul className="mt-4 space-y-3">
        {visible.map((m) => (
          <li key={m.userId} className="card card-white">
            <div className="flex items-center justify-between gap-3">
              <div className="flex min-w-0 items-center gap-3">
                <span className="flex h-11 min-w-11 items-center justify-center rounded-full bg-lavender px-2 text-sm font-bold">
                  #{m.rank}
                </span>
                <div className="min-w-0">
                  <p className="card-title truncate">{m.userName}</p>
                  <p className="meta">
                    Offers {m.skills.length} skill
                    {m.skills.length > 1 ? "s" : ""} you want
                  </p>
                </div>
              </div>
              <Link
                to={`/chat/${m.userId}`}
                className="btn btn-secondary btn-sm shrink-0"
              >
                <Icon name="message-circle" size={15} />
                Chat
              </Link>
            </div>

            <div className="mt-4 flex flex-wrap gap-2">
              {m.skills.map((name) => (
                <button
                  key={name}
                  onClick={() => startBooking(m, name)}
                  className="btn btn-tint btn-sm"
                  title={`Book ${name} with ${m.userName}`}
                >
                  <Icon name="calendar" size={14} />
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
