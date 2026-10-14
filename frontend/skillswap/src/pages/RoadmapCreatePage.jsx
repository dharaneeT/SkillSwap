// pages/RoadmapCreatePage.jsx  (NEW FILE)  route: /roadmaps/new
import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import {
  fetchRoadmapTemplates,
  fetchTemplateDraft,
  generateRoadmap,
  saveRoadmap,
} from "../api/roadmapApi";
import { getErrorMessage } from "../api/axios";
import Icon from "../components/Icon";
import RoadmapReviewEditor from "../components/roadmap/RoadmapReviewEditor";
import {
  LEVELS,
  PREFERENCES,
  addWeeksISO,
  cleanDraft,
  todayISO,
  validateDraft,
} from "../utils/roadmap";

const EMPTY = {
  goal: "",
  targetSkill: "",
  currentLevel: "BEGINNER",
  targetLevel: "",
  desiredOutcome: "",
  hoursPerWeek: "",
  targetDate: "",
  durationWeeks: "",
  preferences: [],
};

const num = (v) => (v === "" || v === null ? null : Number(v));

function validateForm(f) {
  if (f.goal.trim().length < 5)
    return "Describe your learning goal (at least 5 characters).";
  if (f.goal.trim().length > 500)
    return "Your goal is too long (max 500 characters).";
  if (
    f.hoursPerWeek !== "" &&
    (num(f.hoursPerWeek) < 1 || num(f.hoursPerWeek) > 80)
  )
    return "Hours per week must be between 1 and 80.";
  if (
    f.durationWeeks !== "" &&
    (num(f.durationWeeks) < 1 || num(f.durationWeeks) > 104)
  )
    return "Duration must be between 1 and 104 weeks.";
  if (f.targetDate && f.targetDate < todayISO())
    return "The target date can't be in the past.";
  return "";
}

function buildPayload(f) {
  const p = {
    goal: f.goal.trim(),
    currentLevel: f.currentLevel,
    preferences: f.preferences,
  };
  if (f.targetSkill.trim()) p.targetSkill = f.targetSkill.trim();
  if (f.targetLevel) p.targetLevel = f.targetLevel;
  if (f.desiredOutcome.trim()) p.desiredOutcome = f.desiredOutcome.trim();
  if (f.hoursPerWeek !== "") p.hoursPerWeek = num(f.hoursPerWeek);
  if (f.targetDate) p.targetDate = f.targetDate;
  else if (f.durationWeeks !== "") p.durationWeeks = num(f.durationWeeks);
  return p;
}

export default function RoadmapCreatePage() {
  const navigate = useNavigate();
  const [form, setForm] = useState(EMPTY);
  const [draft, setDraft] = useState(null);
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(""); // "", "generate", "template", "save"
  const [templates, setTemplates] = useState(null);
  const [showTemplates, setShowTemplates] = useState(false);

  const set = (patch) => setForm((f) => ({ ...f, ...patch }));
  const togglePref = (value) =>
    set({
      preferences: form.preferences.includes(value)
        ? form.preferences.filter((p) => p !== value)
        : [...form.preferences, value],
    });

  const loadTemplates = async () => {
    setShowTemplates(true);
    if (templates) return;
    try {
      const res = await fetchRoadmapTemplates();
      setTemplates(res.data.data);
    } catch (e) {
      setError(getErrorMessage(e));
    }
  };

  const handleGenerate = async (e) => {
    e.preventDefault();
    const problem = validateForm(form);
    if (problem) return setError(problem);
    setError("");
    setBusy("generate");
    try {
      const res = await generateRoadmap(buildPayload(form));
      setDraft(res.data.data);
    } catch (err) {
      // show the real reason AND offer templates as a fallback
      setError(getErrorMessage(err));
      loadTemplates();
    } finally {
      setBusy("");
    }
  };

  const useTemplate = async (t) => {
    setError("");
    setBusy("template");
    try {
      const res = await fetchTemplateDraft(t.id, form.targetSkill.trim());
      const d = res.data.data;
      // keep whatever the user already typed
      setDraft({
        ...d,
        learningGoal:
          form.goal.trim().length >= 5 ? form.goal.trim() : d.learningGoal,
        currentLevel: form.currentLevel,
        targetLevel: form.targetLevel || d.targetLevel,
        hoursPerWeek:
          form.hoursPerWeek !== "" ? num(form.hoursPerWeek) : d.hoursPerWeek,
        targetDate:
          form.targetDate ||
          (form.durationWeeks !== ""
            ? addWeeksISO(num(form.durationWeeks))
            : d.targetDate),
        preferences: form.preferences,
      });
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setBusy("");
    }
  };

  const handleSave = async () => {
    const problem = validateDraft(draft);
    if (problem) return setError(problem);
    setError("");
    setBusy("save");
    try {
      const res = await saveRoadmap(cleanDraft(draft));
      navigate(`/roadmaps/${res.data.data.id}`);
    } catch (err) {
      setError(getErrorMessage(err));
      setBusy("");
    }
  };

  const errorBox = error && (
    <p role="alert" className="alert alert-error">
      <Icon name="alert-circle" size={18} className="mt-0.5" />
      <span>{error}</span>
    </p>
  );

  // ------------------------------------------------ review step
  if (draft) {
    return (
      <main className="page max-w-4xl space-y-6">
        <header className="flex flex-col gap-2">
          <p className="eyebrow">Step 2 of 2</p>
          <h1 className="page-title">Review your roadmap</h1>
          <p className="page-sub max-w-xl">
            {draft.source === "GEMINI"
              ? "Generated by AI. "
              : draft.source === "TEMPLATE"
                ? "Started from a template. "
                : ""}
            Change anything you like, then save it. Nothing is stored until you
            press Save.
          </p>
        </header>

        {errorBox}

        <RoadmapReviewEditor draft={draft} onChange={setDraft} />

        <div className="flex flex-wrap gap-3">
          <button
            type="button"
            className="btn btn-primary"
            disabled={busy === "save"}
            onClick={handleSave}
          >
            {busy === "save" ? "Saving..." : "Save roadmap"}
          </button>
          <button
            type="button"
            className="btn btn-secondary"
            disabled={busy === "save"}
            onClick={() => {
              setDraft(null);
              setError("");
            }}
          >
            Back to form
          </button>
        </div>
      </main>
    );
  }

  // ------------------------------------------------ form step
  return (
    <main className="page max-w-3xl space-y-6">
      <header className="flex flex-col gap-2">
        <p className="eyebrow">Step 1 of 2</p>
        <h1 className="page-title">New roadmap</h1>
        <p className="page-sub max-w-xl">
          Tell us what you want to learn. Only the goal and your current level
          are required.
        </p>
      </header>

      {errorBox}

      <form
        onSubmit={handleGenerate}
        noValidate
        className="card card-white space-y-5"
      >
        <div>
          <label className="mb-1 block text-sm font-semibold" htmlFor="rf-goal">
            Learning goal
          </label>
          <textarea
            id="rf-goal"
            className="textarea"
            rows={3}
            maxLength={500}
            placeholder="e.g. Become a backend Java developer who can build and deploy REST APIs"
            value={form.goal}
            onChange={(e) => set({ goal: e.target.value })}
          />
          <p className="meta mt-1">
            Please don&apos;t include personal details. Your goal is sent to
            Google&apos;s Gemini to build the roadmap.
          </p>
        </div>

        <div className="grid gap-4 sm:grid-cols-3">
          <div className="sm:col-span-1">
            <label
              className="mb-1 block text-sm font-semibold"
              htmlFor="rf-skill"
            >
              Target skill <span className="meta font-normal">(optional)</span>
            </label>
            <input
              id="rf-skill"
              className="input"
              maxLength={100}
              placeholder="Java"
              value={form.targetSkill}
              onChange={(e) => set({ targetSkill: e.target.value })}
            />
          </div>
          <div>
            <label
              className="mb-1 block text-sm font-semibold"
              htmlFor="rf-current"
            >
              Current level
            </label>
            <select
              id="rf-current"
              className="input select"
              value={form.currentLevel}
              onChange={(e) => set({ currentLevel: e.target.value })}
            >
              {LEVELS.map((l) => (
                <option key={l.value} value={l.value}>
                  {l.label}
                </option>
              ))}
            </select>
          </div>
          <div>
            <label
              className="mb-1 block text-sm font-semibold"
              htmlFor="rf-target"
            >
              Target level <span className="meta font-normal">(optional)</span>
            </label>
            <select
              id="rf-target"
              className="input select"
              value={form.targetLevel}
              onChange={(e) => set({ targetLevel: e.target.value })}
            >
              <option value="">Not set</option>
              {LEVELS.map((l) => (
                <option key={l.value} value={l.value}>
                  {l.label}
                </option>
              ))}
            </select>
          </div>
        </div>

        <div>
          <label
            className="mb-1 block text-sm font-semibold"
            htmlFor="rf-outcome"
          >
            Desired outcome <span className="meta font-normal">(optional)</span>
          </label>
          <input
            id="rf-outcome"
            className="input"
            maxLength={500}
            placeholder="e.g. Ready for a junior backend job interview"
            value={form.desiredOutcome}
            onChange={(e) => set({ desiredOutcome: e.target.value })}
          />
        </div>

        <div className="grid gap-4 sm:grid-cols-3">
          <div>
            <label
              className="mb-1 block text-sm font-semibold"
              htmlFor="rf-hours"
            >
              Hours per week{" "}
              <span className="meta font-normal">(optional)</span>
            </label>
            <input
              id="rf-hours"
              type="number"
              min={1}
              max={80}
              className="input"
              value={form.hoursPerWeek}
              onChange={(e) => set({ hoursPerWeek: e.target.value })}
            />
          </div>
          <div>
            <label
              className="mb-1 block text-sm font-semibold"
              htmlFor="rf-date"
            >
              Target date <span className="meta font-normal">(optional)</span>
            </label>
            <input
              id="rf-date"
              type="date"
              className="input"
              value={form.targetDate}
              onChange={(e) => set({ targetDate: e.target.value })}
            />
          </div>
          <div>
            <label
              className="mb-1 block text-sm font-semibold"
              htmlFor="rf-weeks"
            >
              …or duration (weeks)
            </label>
            <input
              id="rf-weeks"
              type="number"
              min={1}
              max={104}
              className="input"
              disabled={Boolean(form.targetDate)}
              value={form.durationWeeks}
              onChange={(e) => set({ durationWeeks: e.target.value })}
            />
          </div>
        </div>

        <fieldset>
          <legend className="mb-2 text-sm font-semibold">
            How do you like to learn?{" "}
            <span className="meta font-normal">(optional)</span>
          </legend>
          <div className="flex flex-wrap gap-2">
            {PREFERENCES.map((p) => (
              <button
                key={p.value}
                type="button"
                className="chip"
                aria-pressed={form.preferences.includes(p.value)}
                onClick={() => togglePref(p.value)}
              >
                {p.label}
              </button>
            ))}
          </div>
        </fieldset>

        <div className="flex flex-wrap items-center gap-3">
          <button
            type="submit"
            className="btn btn-primary"
            disabled={busy !== ""}
          >
            {busy === "generate"
              ? "Generating (this can take up to a minute)..."
              : "Generate with AI"}
          </button>
          <button
            type="button"
            className="btn btn-secondary"
            disabled={busy !== ""}
            onClick={loadTemplates}
          >
            Use a template instead
          </button>
          <Link to="/roadmaps" className="btn btn-ghost">
            Cancel
          </Link>
        </div>
      </form>

      {showTemplates && (
        <section className="space-y-3" aria-label="Templates">
          <h2 className="section-title">Ready-made templates</h2>
          <p className="meta">
            These are fixed outlines, not AI-generated. You can still edit
            everything before saving.
          </p>
          {!templates && (
            <p className="text-sm text-muted">Loading templates...</p>
          )}
          <ul className="grid gap-3 sm:grid-cols-3">
            {(templates ?? []).map((t) => (
              <li key={t.id} className="card flex flex-col gap-2">
                <p className="card-title">{t.title}</p>
                <p className="meta">{t.description}</p>
                <p className="meta">{t.milestoneCount} milestones</p>
                <button
                  type="button"
                  className="btn btn-secondary btn-sm mt-auto self-start"
                  disabled={busy !== ""}
                  onClick={() => useTemplate(t)}
                >
                  {busy === "template" ? "Loading..." : "Use this template"}
                </button>
              </li>
            ))}
          </ul>
        </section>
      )}
    </main>
  );
}
