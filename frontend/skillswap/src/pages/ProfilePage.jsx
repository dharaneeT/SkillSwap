import { useEffect, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { fetchMe } from "../api/userApi";
import { addUserSkillByName } from "../api/userSkillApi";
import { fetchMyWrittenReviews, fetchReviewsForUser } from "../api/reviewApi";
import { getErrorMessage } from "../api/axios";
import { useFetch } from "../hooks/useFetch";
import { loadSkills } from "../store/skillsSlice";
import Icon from "../components/Icon";
import { fetchMyStanding } from "../api/leaderboardApi";
import BadgeShelf from "../components/BadgeShelf";

const inputClass = "input select";

function Chips({ items, empty, tone = "" }) {
  if (!items?.length) return <p className="text-muted text-sm">{empty}</p>;

  return (
    <ul className="flex flex-wrap gap-2">
      {items.map((s, i) => (
        <li key={i} className={`tag ${tone}`}>
          {s}
        </li>
      ))}
    </ul>
  );
}

// star row that can show fractions, e.g. 4.3 = 4 full stars + 30% of the 5th
function Stars({ value, size = 18 }) {
  const pct = (Math.max(0, Math.min(5, value)) / 5) * 100;
  return (
    <span
      className="relative inline-flex leading-none"
      role="img"
      aria-label={`${value.toFixed(1)} out of 5 stars`}
    >
      <span className="inline-flex gap-0.5 text-[#c9c7c0]">
        {[0, 1, 2, 3, 4].map((i) => (
          <Icon key={i} name="star" size={size} filled strokeWidth={1.5} />
        ))}
      </span>
      <span
        className="absolute inset-y-0 left-0 inline-flex gap-0.5 overflow-hidden text-brand"
        style={{ width: `${pct}%` }}
      >
        {[0, 1, 2, 3, 4].map((i) => (
          <Icon key={i} name="star" size={size} filled strokeWidth={1.5} />
        ))}
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

  if (error)
    return (
      <p role="alert" className="alert alert-error">
        <Icon name="alert-circle" size={18} className="mt-0.5" />
        <span>{error}</span>
      </p>
    );
  if (!data && loading) return <p className="text-muted text-sm">Loading...</p>;
  if (reviews.length === 0 && page === 0) {
    return (
      <p className="card flex items-center gap-2 text-sm text-muted">
        <Icon name="star" size={16} />
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
          <li key={r.id} className="card card-white">
            <div className="flex items-center justify-between">
              <Stars value={r.rating} />
              <span className="meta inline-flex items-center gap-1.5">
                <Icon name="calendar" size={13} />
                {fmtDate(r.createdAt)}
              </span>
            </div>
            {r.comment && <p className="mt-3 text-sm">{r.comment}</p>}
            <p className="meta mt-3">
              {mode === "received"
                ? `— ${r.name}${r.skillName ? ` · ${r.skillName}` : ""}`
                : `For ${r.providerName}${r.skillName ? ` · ${r.skillName}` : ""}`}
            </p>
          </li>
        ))}
      </ul>

      {data && data.totalPages > 1 && (
        <div className="flex items-center justify-between gap-3 text-sm">
          <button
            disabled={!data.hasPrevious}
            onClick={() => setPage((p) => p - 1)}
            className="btn btn-secondary btn-sm"
          >
            <Icon name="chevron-left" size={15} />
            Newer
          </button>
          <span className="meta">
            Page {data.page + 1} of {data.totalPages}
          </span>
          <button
            disabled={!data.hasNext}
            onClick={() => setPage((p) => p + 1)}
            className="btn btn-secondary btn-sm"
          >
            Older
            <Icon name="chevron-right" size={15} />
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
  const { data: standing, error: standingError } = useFetch(fetchMyStanding);

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
      <main className="page max-w-4xl">
        {error || loadError ? (
          <p role="alert" className="alert alert-error">
            <Icon name="alert-circle" size={18} className="mt-0.5" />
            <span>{error || loadError}</span>
          </p>
        ) : (
          <p className="text-muted">Loading...</p>
        )}
      </main>
    );
  }

  return (
    <main className="page max-w-4xl space-y-10">
      <section className="grid gap-4 md:grid-cols-[1fr_auto]">
        <div className="card card-periwinkle flex items-center gap-4 sm:gap-5">
          <span className="flex h-16 w-16 shrink-0 items-center justify-center rounded-full bg-white text-2xl font-extrabold uppercase text-navy">
            {profile.name?.[0] ?? <Icon name="user" size={26} />}
          </span>
          <div className="min-w-0">
            <h1 className="page-title text-3xl sm:text-4xl">{profile.name}</h1>
            <p className="mt-1 flex items-center gap-2 text-sm text-ink/70">
              <Icon name="mail" size={14} />
              <span className="truncate">{profile.email}</span>
            </p>
            <div className="mt-2 flex flex-wrap items-center gap-2 text-sm">
              {profile.reviewCount > 0 ? (
                <>
                  <Stars value={profile.averageRating} />
                  <span className="font-semibold">
                    {profile.averageRating.toFixed(1)}
                  </span>
                  <span className="text-ink/70">
                    ({profile.reviewCount} review
                    {profile.reviewCount === 1 ? "" : "s"})
                  </span>
                </>
              ) : (
                <span className="text-ink/70">No reviews yet</span>
              )}
            </div>
          </div>
        </div>

        <div className="card card-navy flex min-w-48 items-center justify-between gap-5">
          <div>
            <p className="text-4xl font-extrabold leading-none tracking-tight">
              {profile.credits}
            </p>
            <p className="mt-1 text-sm text-white/75">credits</p>
          </div>
          <span className="flex h-12 w-12 items-center justify-center rounded-full bg-white/15">
            <Icon name="coins" size={22} />
          </span>
        </div>
      </section>

      <section className="grid gap-4 sm:grid-cols-2">
        <div className="card card-lavender">
          <h2 className="section-title mb-4 flex items-center gap-2 text-lg">
            <Icon name="graduation-cap" size={19} />I can teach
          </h2>
          <Chips
            items={profile.offeredSkills}
            empty="Nothing added yet"
            tone="bg-white"
          />
        </div>

        <div className="card">
          <h2 className="section-title mb-4 flex items-center gap-2 text-lg">
            <Icon name="book-open" size={19} />I want to learn
          </h2>
          <Chips
            items={profile.wantedSkills}
            empty="Nothing added yet"
            tone="tag-periwinkle"
          />
        </div>
      </section>
      <section>
        <h2 className="section-title mb-4 flex items-center gap-2">
          <Icon name="medal" size={20} />
          Badges
        </h2>
        {standing ? (
          <BadgeShelf stats={standing} />
        ) : (
          <p className="text-sm text-muted">{standingError || "Loading..."}</p>
        )}
      </section>

      <form onSubmit={handleAdd} className="card card-white space-y-4">
        <h2 className="section-title flex items-center gap-2 text-lg">
          <Icon name="plus" size={19} />
          Add a skill
        </h2>

        {error && (
          <p role="alert" className="alert alert-error">
            <Icon name="alert-circle" size={18} className="mt-0.5" />
            <span>{error}</span>
          </p>
        )}

        {notice && (
          <p role="status" className="alert alert-success">
            <Icon name="circle-check" size={18} className="mt-0.5" />
            <span>{notice}</span>
          </p>
        )}

        <div className="flex flex-wrap gap-3">
          <input
            className="input min-w-48 flex-1"
            list="skill-suggestions"
            placeholder="Type a skill, e.g. Guitar"
            aria-label="Skill name"
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
            aria-label="Skill type"
            value={form.type}
            onChange={(e) => setForm({ ...form, type: e.target.value })}
          >
            <option value="OFFERED">I can teach this</option>
            <option value="WANTED">I want to learn this</option>
          </select>

          <button className="btn btn-primary">
            <Icon name="plus" size={16} />
            Add
          </button>
        </div>
      </form>

      <section>
        <div className="mb-4 flex flex-wrap gap-2" role="tablist">
          {[
            ["received", "Reviews I've received"],
            ["written", "Reviews I've written"],
          ].map(([key, label]) => (
            <button
              key={key}
              role="tab"
              aria-selected={tab === key}
              onClick={() => setTab(key)}
              className={`chip px-4 py-2 text-sm font-semibold ${
                tab === key ? "chip-active" : ""
              }`}
            >
              <Icon name="star" size={14} />
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
