import { useEffect, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { fetchMe } from "../api/userApi";
import { addUserSkillByName } from "../api/userSkillApi";
import { fetchMyWrittenReviews, fetchReviewsForUser } from "../api/reviewApi";
import { getErrorMessage } from "../api/axios";
import { useFetch } from "../hooks/useFetch";
import { loadSkills } from "../store/skillsSlice";

const inputClass =
  "rounded-lg bg-slate-800 border border-slate-600 px-3 py-2 outline-none focus:border-red-200";

function Chips({ items, empty }) {
  if (!items?.length) return <p className="text-slate-500 text-sm">{empty}</p>;

  return (
    <ul className="flex flex-wrap gap-2">
      {items.map((s, i) => (
        <li key={i} className="rounded-full bg-slate-700 px-3 py-1 text-sm">
          {s}
        </li>
      ))}
    </ul>
  );
}

// star row that can show fractions, e.g. 4.3 = 4 full stars + 30% of the 5th
function Stars({ value, size = "text-lg" }) {
  const pct = (Math.max(0, Math.min(5, value)) / 5) * 100;
  return (
    <span
      className={`relative inline-block leading-none ${size}`}
      role="img"
      aria-label={`${value.toFixed(1)} out of 5 stars`}
    >
      <span className="text-slate-600">★★★★★</span>
      <span
        className="absolute inset-0 overflow-hidden whitespace-nowrap text-yellow-300"
        style={{ width: `${pct}%` }}
      >
        ★★★★★
      </span>
    </span>
  );
}

const fmtDate = (iso) =>
  iso ? new Date(iso).toLocaleDateString([], { dateStyle: "medium" }) : "";

// useFetch passes ONE argument to the fetcher, so wrap the two-argument APIs
const loadReceived = ({ userId, page }) => fetchReviewsForUser(userId, page);
const loadWritten = ({ page }) => fetchMyWrittenReviews(page);

// mode = "received" (about me) | "written" (I submitted)
function ReviewList({ mode, userId }) {
  const [page, setPage] = useState(0);
  const { data, error, loading } = useFetch(
    mode === "received" ? loadReceived : loadWritten,
    { userId, page },
  );
  const reviews = data?.content ?? [];

  if (error) return <p className="text-red-300 text-sm">{error}</p>;
  if (!data && loading)
    return <p className="text-slate-400 text-sm">Loading...</p>;
  if (reviews.length === 0 && page === 0) {
    return (
      <p className="text-slate-500 text-sm">
        {mode === "received"
          ? "No one has reviewed you yet"
          : "You haven't written any reviews yet"}
      </p>
    );
  }

  return (
    <div className="space-y-3">
      <ul className="space-y-3">
        {reviews.map((r) => (
          <li
            key={r.id}
            className="rounded-xl bg-slate-800 border border-slate-700 p-4"
          >
            <div className="flex justify-between items-center">
              <Stars value={r.rating} />
              <span className="text-xs text-slate-500">
                {fmtDate(r.createdAt)}
              </span>
            </div>
            {r.comment && <p className="text-sm mt-2">{r.comment}</p>}
            <p className="text-xs text-slate-400 mt-2">
              {mode === "received"
                ? `— ${r.name}${r.skillName ? ` · ${r.skillName}` : ""}`
                : `For ${r.providerName}${r.skillName ? ` · ${r.skillName}` : ""}`}
            </p>
          </li>
        ))}
      </ul>

      {data && data.totalPages > 1 && (
        <div className="flex items-center justify-between text-sm">
          <button
            disabled={!data.hasPrevious}
            onClick={() => setPage((p) => p - 1)}
            className="rounded-lg border border-slate-600 px-3 py-1 cursor-pointer disabled:opacity-40 disabled:cursor-default"
          >
            ← Newer
          </button>
          <span className="text-slate-400">
            Page {data.page + 1} of {data.totalPages}
          </span>
          <button
            disabled={!data.hasNext}
            onClick={() => setPage((p) => p + 1)}
            className="rounded-lg border border-slate-600 px-3 py-1 cursor-pointer disabled:opacity-40 disabled:cursor-default"
          >
            Older →
          </button>
        </div>
      )}
    </div>
  );
}

export default function ProfilePage() {
  const dispatch = useDispatch();
  const skills = useSelector((s) => s.skills.items);

  const {
    data: profile,
    error: loadError,
    refetch: refetchProfile,
  } = useFetch(fetchMe);

  const [tab, setTab] = useState("received");

  const [form, setForm] = useState({ name: "", type: "OFFERED" });
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");

  useEffect(() => {
    dispatch(loadSkills());
  }, [dispatch]);

  const handleAdd = async (e) => {
    e.preventDefault();
    setError("");
    setNotice("");

    const name = form.name.trim();

    if (!name) {
      setError("Type the name of a skill");
      return;
    }

    try {
      await addUserSkillByName({
        name,
        type: form.type,
      });

      refetchProfile();
      dispatch(loadSkills(true));

      setForm({ ...form, name: "" });
      setNotice(`${name} added`);
    } catch (err) {
      setError(getErrorMessage(err));
    }
  };

  if (!profile) {
    return (
      <main className="max-w-3xl mx-auto px-4 pt-10">
        {error || loadError ? (
          <p className="text-red-300">{error || loadError}</p>
        ) : (
          <p className="text-slate-400">Loading...</p>
        )}
      </main>
    );
  }

  return (
    <main className="max-w-3xl mx-auto px-4 pt-10 space-y-8">
      <section className="rounded-xl bg-slate-800 border border-slate-700 p-5 flex justify-between items-center">
        <div>
          <h1 className="text-2xl font-bold">{profile.name}</h1>
          <p className="text-slate-400">{profile.email}</p>
          <div className="mt-2 flex items-center gap-2 text-sm text-slate-300">
            {profile.reviewCount > 0 ? (
              <>
                <Stars value={profile.averageRating} />
                <span className="font-semibold">
                  {profile.averageRating.toFixed(1)}
                </span>
                <span className="text-slate-400">
                  ({profile.reviewCount} review
                  {profile.reviewCount === 1 ? "" : "s"})
                </span>
              </>
            ) : (
              "No reviews yet"
            )}
          </div>
        </div>

        <div className="text-right">
          <p className="text-3xl font-bold text-red-200">{profile.credits}</p>
          <p className="text-slate-400 text-sm">credits</p>
        </div>
      </section>

      <section className="grid sm:grid-cols-2 gap-6">
        <div>
          <h2 className="font-semibold mb-2">I can teach</h2>
          <Chips items={profile.offeredSkills} empty="Nothing added yet" />
        </div>

        <div>
          <h2 className="font-semibold mb-2">I want to learn</h2>
          <Chips items={profile.wantedSkills} empty="Nothing added yet" />
        </div>
      </section>

      <form onSubmit={handleAdd} className="space-y-3">
        <h2 className="font-semibold">Add a skill</h2>

        {error && (
          <p className="rounded-lg bg-red-900/50 border border-red-500 px-3 py-2 text-sm">
            {error}
          </p>
        )}

        {notice && <p className="text-green-300 text-sm">{notice}</p>}

        <div className="flex flex-wrap gap-3">
          <input
            className={`${inputClass} flex-1 min-w-48`}
            list="skill-suggestions"
            placeholder="Type a skill, e.g. Guitar"
            value={form.name}
            maxLength={50}
            required
            onChange={(e) => setForm({ ...form, name: e.target.value })}
          />

          <datalist id="skill-suggestions">
            {skills.map((s) => (
              <option key={s.id} value={s.name} />
            ))}
          </datalist>

          <select
            className={inputClass}
            value={form.type}
            onChange={(e) => setForm({ ...form, type: e.target.value })}
          >
            <option value="OFFERED">I can teach this</option>
            <option value="WANTED">I want to learn this</option>
          </select>

          <button className="rounded-lg bg-red-200 text-slate-900 font-semibold px-4 py-2 cursor-pointer">
            Add
          </button>
        </div>
      </form>

      <section>
        <div className="flex gap-2 mb-3" role="tablist">
          {[
            ["received", "Reviews I've received"],
            ["written", "Reviews I've written"],
          ].map(([key, label]) => (
            <button
              key={key}
              role="tab"
              aria-selected={tab === key}
              onClick={() => setTab(key)}
              className={`rounded-lg px-3 py-1 text-sm font-semibold cursor-pointer ${
                tab === key
                  ? "bg-red-200 text-slate-900"
                  : "border border-slate-600 text-slate-300"
              }`}
            >
              {label}
            </button>
          ))}
        </div>

        {/* key = remount on tab change, so each tab starts at page 0 */}
        <ReviewList key={tab} mode={tab} userId={profile.id} />
      </section>
    </main>
  );
}
