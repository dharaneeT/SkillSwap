// pages/OnboardingPage.jsx  (NEW FILE)  route: /onboarding
// Step 1: pick skills you can teach  ->  Step 2: pick skills you want to learn
// ->  Step 3: save them and show your first matches immediately.
import { useEffect, useMemo, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useDispatch, useSelector } from "react-redux";
import { addUserSkillByName } from "../api/userSkillApi";
import { fetchMatches } from "../api/matchApi";
import { fetchMe } from "../api/userApi";
import { getErrorMessage } from "../api/axios";
import { useFetch } from "../hooks/useFetch";
import { loadSkills } from "../store/skillsSlice";
import Icon from "../components/Icon";

const MIN = 2; // required per list
const MAX = 6;

const STEPS = [
  {
    type: "OFFERED",
    icon: "graduation-cap",
    title: "What can you teach?",
    sub: `Pick at least ${MIN} skills you're happy to help others with.`,
  },
  {
    type: "WANTED",
    icon: "book-open",
    title: "What do you want to learn?",
    sub: `Pick at least ${MIN} skills. We'll use them to find your teachers.`,
  },
];

const same = (a, b) => a.toLowerCase() === b.toLowerCase();

function SkillPicker({ picked, setPicked, blocked, suggestions }) {
  const [text, setText] = useState("");
  const has = (n) => picked.some((p) => same(p, n));
  const isBlocked = (n) => blocked.some((b) => same(b, n));

  const toggle = (n) => {
    if (has(n)) setPicked(picked.filter((p) => !same(p, n)));
    else if (picked.length < MAX) setPicked([...picked, n]);
  };

  const addCustom = (e) => {
    e.preventDefault();
    const n = text.trim();
    setText("");
    if (n && !has(n) && !isBlocked(n) && picked.length < MAX) {
      setPicked([...picked, n]);
    }
  };

  // chips = what's picked (incl. custom) + suggestions from existing skills
  const chips = [
    ...new Map(
      [...picked, ...suggestions.filter((s) => !isBlocked(s))]
        .slice(0, 24)
        .map((n) => [n.toLowerCase(), n]),
    ).values(),
  ];

  return (
    <div className="space-y-4">
      <ul className="flex flex-wrap gap-2" aria-label="Skills">
        {chips.map((n) => (
          <li key={n}>
            <button
              type="button"
              className="chip"
              aria-pressed={has(n)}
              onClick={() => toggle(n)}
            >
              {has(n) && <Icon name="check" size={13} strokeWidth={3} />}
              {n}
            </button>
          </li>
        ))}
      </ul>

      <form onSubmit={addCustom} className="flex gap-2">
        <input
          className="input min-w-0 flex-1"
          placeholder="Not listed? Type a skill, e.g. Guitar"
          aria-label="Add another skill"
          maxLength={50}
          value={text}
          onChange={(e) => setText(e.target.value)}
        />
        <button className="btn btn-secondary" disabled={!text.trim()}>
          <Icon name="plus" size={15} />
          Add
        </button>
      </form>

      <p className="meta" aria-live="polite">
        {picked.length} selected
        {picked.length < MIN ? ` · pick ${MIN - picked.length} more` : ""}
        {picked.length >= MAX ? ` · maximum ${MAX}` : ""}
      </p>
    </div>
  );
}

// same grouping idea as MatchesPage.rankMatches
function groupMatches(rows) {
  const byUser = new Map();
  for (const r of rows) {
    const e = byUser.get(r.userId) ?? {
      userId: r.userId,
      userName: r.userName,
      skills: [],
    };
    if (!e.skills.includes(r.matchedSkill)) e.skills.push(r.matchedSkill);
    byUser.set(r.userId, e);
  }
  return [...byUser.values()].sort(
    (a, b) =>
      b.skills.length - a.skills.length || a.userName.localeCompare(b.userName),
  );
}

function FirstMatches() {
  const { data: me, error: meError } = useFetch(fetchMe);
  const {
    data: rows,
    error: matchError,
    loading,
  } = useFetch(fetchMatches, me?.id, {
    enabled: Boolean(me?.id),
  });
  const people = useMemo(() => groupMatches(rows ?? []).slice(0, 3), [rows]);
  const error = meError || matchError;

  return (
    <div className="space-y-5">
      <div className="space-y-2">
        <p className="eyebrow">You&apos;re all set</p>
        <h1 className="page-title text-4xl sm:text-5xl">Your first matches</h1>
        <p className="page-sub">People who can teach what you want to learn.</p>
      </div>

      {error && (
        <p role="alert" className="alert alert-error">
          <Icon name="alert-circle" size={18} className="mt-0.5" />
          <span>{error}</span>
        </p>
      )}
      {!rows && loading && (
        <p className="text-sm text-muted">Finding matches...</p>
      )}

      {rows && people.length === 0 && (
        <p className="card flex items-center gap-2 text-sm text-muted">
          <Icon name="users" size={16} />
          Nobody teaches those skills yet. You&apos;ll show up for others as
          they join.
        </p>
      )}

      <ul className="space-y-3">
        {people.map((p) => (
          <li
            key={p.userId}
            className="card card-white flex flex-wrap items-center justify-between gap-3"
          >
            <div className="flex min-w-0 items-center gap-3">
              <span className="flex h-11 w-11 shrink-0 items-center justify-center rounded-full bg-lavender font-bold uppercase text-navy">
                {p.userName?.[0]}
              </span>
              <div className="min-w-0">
                <p className="card-title truncate">{p.userName}</p>
                <p className="meta truncate">Teaches {p.skills.join(", ")}</p>
              </div>
            </div>
            <Link to="/matches" className="btn btn-primary btn-sm">
              Book a session
              <Icon name="arrow-right" size={14} />
            </Link>
          </li>
        ))}
      </ul>

      <div className="flex flex-wrap gap-2 pt-2">
        <Link to="/matches" className="btn btn-secondary">
          See all matches
        </Link>
        <Link to="/dashboard" className="btn btn-ghost">
          Go to dashboard
        </Link>
      </div>
    </div>
  );
}

export default function OnboardingPage() {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const allSkills = useSelector((s) => s.skills.items);

  const [step, setStep] = useState(0); // 0 teach, 1 learn, 2 matches
  const [teach, setTeach] = useState([]);
  const [learn, setLearn] = useState([]);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");

  // skill suggestions come from skills that already exist (loaded once)
  useEffect(() => {
    dispatch(loadSkills());
  }, [dispatch]);
  const suggestions = useMemo(() => allSkills.map((s) => s.name), [allSkills]);

  const finish = async () => {
    setSaving(true);
    setError("");
    const jobs = [
      ...teach.map((name) => ({ name, type: "OFFERED" })),
      ...learn.map((name) => ({ name, type: "WANTED" })),
    ];
    let saved = 0;
    let firstError = "";
    for (const job of jobs) {
      // one by one: two new skills created at the same moment can collide
      try {
        await addUserSkillByName(job);
        saved++;
      } catch (e) {
        firstError ||= getErrorMessage(e);
      }
    }
    setSaving(false);
    if (saved === 0) {
      setError(firstError || "Could not save your skills");
      return;
    }
    dispatch(loadSkills(true));
    setStep(2);
  };

  if (step === 2) {
    return (
      <main className="page max-w-2xl">
        <FirstMatches />
      </main>
    );
  }

  const cfg = STEPS[step];
  const picked = step === 0 ? teach : learn;
  const setPicked = step === 0 ? setTeach : setLearn;
  const blocked = step === 0 ? learn : teach; // a skill can't be in both lists
  const ready = picked.length >= MIN;

  return (
    <main className="page max-w-2xl space-y-8">
      <div className="space-y-3">
        <div className="flex items-center justify-between gap-3">
          <p className="eyebrow">Step {step + 1} of 3</p>
          <button
            type="button"
            onClick={() => navigate("/dashboard")}
            className="btn btn-ghost btn-sm"
          >
            Skip for now
          </button>
        </div>
        <div
          role="progressbar"
          aria-label="Onboarding progress"
          aria-valuemin={0}
          aria-valuemax={3}
          aria-valuenow={step + 1}
          className="h-1.5 overflow-hidden rounded-full bg-line"
        >
          <div
            className="h-full rounded-full bg-brand transition-all"
            style={{ width: `${((step + 1) / 3) * 100}%` }}
          />
        </div>
      </div>

      <section className="card card-white space-y-5 sm:p-8">
        <div className="flex items-start gap-3">
          <span className="flex h-11 w-11 shrink-0 items-center justify-center rounded-full bg-lavender">
            <Icon name={cfg.icon} size={21} />
          </span>
          <div>
            <h1 className="section-title">{cfg.title}</h1>
            <p className="page-sub text-sm">{cfg.sub}</p>
          </div>
        </div>

        <SkillPicker
          picked={picked}
          setPicked={setPicked}
          blocked={blocked}
          suggestions={suggestions}
        />

        {error && (
          <p role="alert" className="alert alert-error">
            <Icon name="alert-circle" size={18} className="mt-0.5" />
            <span>{error}</span>
          </p>
        )}

        <div className="flex flex-wrap justify-between gap-2 pt-1">
          {step > 0 ? (
            <button
              type="button"
              className="btn btn-ghost"
              onClick={() => setStep(step - 1)}
            >
              <Icon name="chevron-left" size={15} />
              Back
            </button>
          ) : (
            <span />
          )}

          {step === 0 ? (
            <button
              type="button"
              className="btn btn-primary"
              disabled={!ready}
              onClick={() => setStep(1)}
            >
              Next
              <Icon name="arrow-right" size={15} />
            </button>
          ) : (
            <button
              type="button"
              className="btn btn-primary"
              disabled={!ready || saving}
              onClick={finish}
            >
              <Icon name="check" size={15} />
              {saving ? "Saving..." : "Save & see matches"}
            </button>
          )}
        </div>
      </section>
    </main>
  );
}
