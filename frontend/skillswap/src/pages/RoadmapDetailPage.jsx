// pages/RoadmapDetailPage.jsx  (NEW FILE)  route: /roadmaps/:id
import { useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import {
  deleteRoadmap,
  fetchRoadmap,
  setMilestoneDone,
  setTaskDone,
  updateMilestone,
  updateRoadmap,
} from "../api/roadmapApi";
import { getErrorMessage } from "../api/axios";
import Icon from "../components/Icon";
import ProgressBar from "../components/roadmap/ProgressBar";
import {
  dueLabel,
  formatDate,
  formatMinutes,
  levelLabel,
  taskTypeLabel,
} from "../utils/roadmap";

export default function RoadmapDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();

  const [roadmap, setRoadmap] = useState(null);
  const [error, setError] = useState(""); // loading error
  const [actionError, setActionError] = useState("");
  const [busyKey, setBusyKey] = useState(null); // which button/checkbox is saving
  const [openOverride, setOpenOverride] = useState({}); // milestoneId -> true/false
  const [editing, setEditing] = useState(false);
  const [meta, setMeta] = useState({
    title: "",
    description: "",
    targetDate: "",
    hoursPerWeek: "",
  });
  const [editingMilestone, setEditingMilestone] = useState(null); // { id, title, description, estimatedHours }

  useEffect(() => {
    let ignore = false;
    fetchRoadmap(id)
      .then((res) => {
        if (ignore) return;
        setRoadmap(res.data.data);
        setError("");
      })
      .catch((e) => {
        if (!ignore) setError(getErrorMessage(e));
      });
    return () => {
      ignore = true;
    };
  }, [id]);

  // runs an API call that returns the fresh roadmap and shows it right away
  const run = async (key, fn) => {
    setBusyKey(key);
    setActionError("");
    try {
      const res = await fn();
      setRoadmap(res.data.data);
      return true;
    } catch (e) {
      setActionError(getErrorMessage(e));
      return false;
    } finally {
      setBusyKey(null);
    }
  };

  const toggleTask = (t) =>
    run(`t${t.id}`, () => setTaskDone(id, t.id, !t.completed));
  const toggleMilestone = (m) =>
    run(`m${m.id}`, () => setMilestoneDone(id, m.id, !m.completed));

  const startEdit = () => {
    setMeta({
      title: roadmap.title,
      description: roadmap.description ?? "",
      targetDate: roadmap.targetDate ?? "",
      hoursPerWeek: roadmap.hoursPerWeek ?? "",
    });
    setEditing(true);
  };

  const saveMeta = async (e) => {
    e.preventDefault();
    if (!meta.title.trim()) return setActionError("The title can't be empty.");
    const ok = await run("meta", () =>
      updateRoadmap(id, {
        title: meta.title.trim(),
        description: meta.description.trim() || null,
        targetDate: meta.targetDate || null,
        hoursPerWeek:
          meta.hoursPerWeek === "" ? null : Number(meta.hoursPerWeek),
      }),
    );
    if (ok) setEditing(false);
  };

  const saveMilestone = async (e) => {
    e.preventDefault();
    if (!editingMilestone.title.trim())
      return setActionError("The milestone title can't be empty.");
    const ok = await run(`me${editingMilestone.id}`, () =>
      updateMilestone(id, editingMilestone.id, {
        title: editingMilestone.title.trim(),
        description: editingMilestone.description.trim() || null,
        estimatedHours:
          editingMilestone.estimatedHours === ""
            ? null
            : Number(editingMilestone.estimatedHours),
      }),
    );
    if (ok) setEditingMilestone(null);
  };

  const handleDelete = async () => {
    if (
      !window.confirm(
        `Delete "${roadmap.title}"? Its progress will be removed too. This can't be undone.`,
      )
    )
      return;
    setBusyKey("delete");
    try {
      await deleteRoadmap(id);
      navigate("/roadmaps");
    } catch (e) {
      setActionError(getErrorMessage(e));
      setBusyKey(null);
    }
  };

  if (error) {
    return (
      <main className="page max-w-3xl space-y-4">
        <p role="alert" className="alert alert-error">
          <Icon name="alert-circle" size={18} className="mt-0.5" />
          <span>{error}</span>
        </p>
        <Link to="/roadmaps" className="btn btn-secondary btn-sm">
          Back to roadmaps
        </Link>
      </main>
    );
  }
  if (!roadmap) {
    return (
      <main className="page max-w-3xl">
        <p className="text-sm text-muted">Loading roadmap...</p>
      </main>
    );
  }

  const next = roadmap.next;
  const currentMilestoneId = next?.milestoneId ?? null;
  const isOpen = (m) => openOverride[m.id] ?? m.id === currentMilestoneId;
  const toggleOpen = (m) =>
    setOpenOverride((o) => ({ ...o, [m.id]: !isOpen(m) }));

  const resume = () => {
    if (!next) return;
    setOpenOverride((o) => ({ ...o, [next.milestoneId]: true }));
    requestAnimationFrame(() => {
      const el = document.getElementById(
        next.taskId ? `task-${next.taskId}` : `milestone-${next.milestoneId}`,
      );
      el?.scrollIntoView({ behavior: "smooth", block: "center" });
    });
  };

  const completed = roadmap.status === "COMPLETED";

  return (
    <main className="page max-w-4xl space-y-6">
      <Link to="/roadmaps" className="link text-sm">
        ← All roadmaps
      </Link>

      {actionError && (
        <p role="alert" className="alert alert-error">
          <Icon name="alert-circle" size={18} className="mt-0.5" />
          <span>{actionError}</span>
        </p>
      )}

      {/* ---------------- overview ---------------- */}
      <section className="card space-y-4">
        {editing ? (
          <form onSubmit={saveMeta} className="space-y-3">
            <input
              aria-label="Title"
              className="input"
              maxLength={150}
              value={meta.title}
              onChange={(e) => setMeta({ ...meta, title: e.target.value })}
            />
            <textarea
              aria-label="Description"
              className="textarea"
              rows={3}
              maxLength={4000}
              value={meta.description}
              onChange={(e) =>
                setMeta({ ...meta, description: e.target.value })
              }
            />
            <div className="grid gap-3 sm:grid-cols-2">
              <div>
                <label className="meta mb-1 block">Target date</label>
                <input
                  type="date"
                  className="input"
                  value={meta.targetDate}
                  onChange={(e) =>
                    setMeta({ ...meta, targetDate: e.target.value })
                  }
                />
              </div>
              <div>
                <label className="meta mb-1 block">Hours per week</label>
                <input
                  type="number"
                  min={1}
                  max={80}
                  className="input"
                  value={meta.hoursPerWeek}
                  onChange={(e) =>
                    setMeta({ ...meta, hoursPerWeek: e.target.value })
                  }
                />
              </div>
            </div>
            <div className="flex gap-2">
              <button
                type="submit"
                className="btn btn-primary btn-sm"
                disabled={busyKey === "meta"}
              >
                {busyKey === "meta" ? "Saving..." : "Save"}
              </button>
              <button
                type="button"
                className="btn btn-secondary btn-sm"
                onClick={() => setEditing(false)}
              >
                Cancel
              </button>
            </div>
          </form>
        ) : (
          <>
            <div className="flex flex-wrap items-start justify-between gap-3">
              <div className="min-w-0 space-y-1">
                <p className="eyebrow">
                  {levelLabel(roadmap.currentLevel)}
                  {roadmap.targetLevel
                    ? ` → ${levelLabel(roadmap.targetLevel)}`
                    : ""}
                  {roadmap.source === "GEMINI" ? " · AI generated" : ""}
                  {roadmap.source === "TEMPLATE" ? " · Template" : ""}
                </p>
                <h1 className="page-title">{roadmap.title}</h1>
                {roadmap.description && (
                  <p className="page-sub max-w-2xl">{roadmap.description}</p>
                )}
              </div>
              <span
                className={`badge ${completed ? "badge-completed" : "badge-accepted"}`}
              >
                {completed ? "Completed" : "Active"}
              </span>
            </div>

            <div className="flex flex-wrap gap-x-5 gap-y-1 text-sm text-muted">
              <span className="inline-flex items-center gap-1.5">
                <Icon name="calendar" size={15} />
                {roadmap.targetDate
                  ? `${formatDate(roadmap.targetDate)} · ${dueLabel(roadmap.daysLeft)}`
                  : "No target date"}
              </span>
              <span className="inline-flex items-center gap-1.5">
                <Icon name="clock" size={15} />
                {roadmap.estimatedHoursCompleted}h of{" "}
                {roadmap.estimatedHoursTotal}h
                {roadmap.hoursPerWeek ? ` · ${roadmap.hoursPerWeek}h/week` : ""}
              </span>
            </div>

            <div className="space-y-1.5">
              <div className="flex items-center justify-between text-sm font-semibold">
                <span>{roadmap.progressPercent}% complete</span>
                <span className="meta">
                  {roadmap.completedTasks}/{roadmap.totalTasks} tasks ·{" "}
                  {roadmap.completedMilestones}/{roadmap.totalMilestones}{" "}
                  milestones
                </span>
              </div>
              <ProgressBar
                value={roadmap.progressPercent}
                label="Roadmap progress"
              />
            </div>

            <div className="flex flex-wrap gap-2">
              {!completed && next && (
                <button
                  type="button"
                  className="btn btn-primary btn-sm"
                  onClick={resume}
                >
                  Resume learning
                  <Icon name="arrow-right" size={14} />
                </button>
              )}
              <button
                type="button"
                className="btn btn-secondary btn-sm"
                onClick={startEdit}
              >
                Edit
              </button>
              <button
                type="button"
                className="btn btn-secondary btn-sm"
                disabled={busyKey === "delete"}
                onClick={handleDelete}
              >
                {busyKey === "delete" ? "Deleting..." : "Delete"}
              </button>
            </div>
          </>
        )}
      </section>

      {/* ---------------- next step ---------------- */}
      {completed ? (
        <p className="alert alert-success">
          <Icon name="circle-check" size={18} className="mt-0.5" />
          <span>You completed this roadmap. Well done!</span>
        </p>
      ) : (
        next && (
          <section
            className="card card-navy space-y-2"
            aria-label="Recommended next step"
          >
            <p className="eyebrow text-white/70">Recommended next step</p>
            <p className="text-lg font-bold">
              {next.taskTitle ?? next.milestoneTitle}
            </p>
            <p className="text-sm text-white/80">
              Milestone: {next.milestoneTitle}
              {next.taskType ? ` · ${taskTypeLabel(next.taskType)}` : ""}
              {next.estimatedMinutes
                ? ` · ${formatMinutes(next.estimatedMinutes)}`
                : ""}
            </p>
            <div className="flex flex-wrap gap-2 pt-1">
              <button
                type="button"
                className="btn btn-tint btn-sm"
                onClick={resume}
              >
                Go to task
              </button>
              <Link
                to="/matches"
                className="btn btn-secondary btn-sm border-white/40 text-white hover:bg-white/10"
              >
                Find a mentor
              </Link>
              <Link
                to="/skills"
                className="btn btn-secondary btn-sm border-white/40 text-white hover:bg-white/10"
              >
                Browse skills
              </Link>
            </div>
          </section>
        )
      )}

      {/* ---------------- prerequisites ---------------- */}
      {roadmap.prerequisites.length > 0 && (
        <section className="card card-white space-y-2">
          <h2 className="card-title">Before you start</h2>
          <ul className="list-disc space-y-1 pl-5 text-sm">
            {roadmap.prerequisites.map((p) => (
              <li key={p}>{p}</li>
            ))}
          </ul>
        </section>
      )}

      {/* ---------------- milestones timeline ---------------- */}
      <section className="space-y-3" aria-label="Milestones">
        <h2 className="section-title">Milestones</h2>
        <ol className="space-y-3">
          {roadmap.milestones.map((m) => {
            const current = m.id === currentMilestoneId && !completed;
            const open = isOpen(m);
            return (
              <li
                key={m.id}
                id={`milestone-${m.id}`}
                className="card card-white space-y-3"
              >
                <div className="flex items-start gap-3">
                  <span
                    className={`medal medal-sm ${m.completed ? "medal-gold" : current ? "medal-periwinkle" : "medal-locked"}`}
                  >
                    {m.completed ? (
                      <Icon name="circle-check" size={16} />
                    ) : (
                      <span className="text-sm font-bold">
                        {m.sequenceNumber}
                      </span>
                    )}
                  </span>

                  <button
                    type="button"
                    className="min-w-0 flex-1 text-left"
                    aria-expanded={open}
                    onClick={() => toggleOpen(m)}
                  >
                    <span className="card-title block">{m.title}</span>
                    <span className="meta block">
                      {m.completedTasks}/{m.totalTasks} tasks
                      {m.estimatedHours ? ` · ~${m.estimatedHours}h` : ""}
                      {current ? " · Current" : ""}
                      {m.dependsOn.length > 0
                        ? ` · After milestone ${m.dependsOn.join(", ")}`
                        : ""}
                    </span>
                  </button>

                  <span className="meta shrink-0">{m.progressPercent}%</span>
                </div>

                <ProgressBar
                  value={m.progressPercent}
                  label={`${m.title} progress`}
                />

                {open && (
                  <div className="space-y-4 border-t border-line pt-3">
                    {editingMilestone?.id === m.id ? (
                      <form onSubmit={saveMilestone} className="space-y-3">
                        <input
                          aria-label="Milestone title"
                          className="input"
                          maxLength={150}
                          value={editingMilestone.title}
                          onChange={(e) =>
                            setEditingMilestone({
                              ...editingMilestone,
                              title: e.target.value,
                            })
                          }
                        />
                        <textarea
                          aria-label="Milestone description"
                          className="textarea"
                          rows={2}
                          value={editingMilestone.description}
                          onChange={(e) =>
                            setEditingMilestone({
                              ...editingMilestone,
                              description: e.target.value,
                            })
                          }
                        />
                        <input
                          aria-label="Estimated hours"
                          type="number"
                          min={1}
                          max={300}
                          className="input"
                          value={editingMilestone.estimatedHours}
                          onChange={(e) =>
                            setEditingMilestone({
                              ...editingMilestone,
                              estimatedHours: e.target.value,
                            })
                          }
                        />
                        <div className="flex gap-2">
                          <button
                            type="submit"
                            className="btn btn-primary btn-sm"
                            disabled={busyKey === `me${m.id}`}
                          >
                            Save
                          </button>
                          <button
                            type="button"
                            className="btn btn-secondary btn-sm"
                            onClick={() => setEditingMilestone(null)}
                          >
                            Cancel
                          </button>
                        </div>
                      </form>
                    ) : (
                      <>
                        {m.description && (
                          <p className="text-sm">{m.description}</p>
                        )}

                        {m.learningObjectives.length > 0 && (
                          <div>
                            <p className="eyebrow mb-1">Learning objectives</p>
                            <ul className="list-disc space-y-0.5 pl-5 text-sm">
                              {m.learningObjectives.map((o) => (
                                <li key={o}>{o}</li>
                              ))}
                            </ul>
                          </div>
                        )}

                        {m.completionCriteria && (
                          <p className="text-sm">
                            <span className="eyebrow mr-1">Done when</span>
                            {m.completionCriteria}
                          </p>
                        )}
                      </>
                    )}

                    <ul className="space-y-1.5">
                      {m.tasks.map((t) => (
                        <li
                          key={t.id}
                          id={`task-${t.id}`}
                          className="flex items-start gap-3 rounded-2xl bg-surface px-3 py-2"
                        >
                          <input
                            id={`chk-${t.id}`}
                            type="checkbox"
                            className="mt-1 h-4 w-4 shrink-0"
                            style={{ accentColor: "var(--color-brand)" }}
                            checked={t.completed}
                            disabled={busyKey === `t${t.id}`}
                            onChange={() => toggleTask(t)}
                          />
                          <label
                            htmlFor={`chk-${t.id}`}
                            className="min-w-0 flex-1 cursor-pointer"
                          >
                            <span
                              className={`block text-sm font-medium ${t.completed ? "text-muted line-through" : ""}`}
                            >
                              {t.title}
                            </span>
                            {t.description && (
                              <span className="meta block">
                                {t.description}
                              </span>
                            )}
                          </label>
                          <span className="tag tag-periwinkle shrink-0">
                            {taskTypeLabel(t.type)}
                            {t.estimatedMinutes
                              ? ` · ${formatMinutes(t.estimatedMinutes)}`
                              : ""}
                          </span>
                        </li>
                      ))}
                    </ul>

                    <div className="flex flex-wrap gap-2">
                      <button
                        type="button"
                        className="btn btn-secondary btn-sm"
                        disabled={busyKey === `m${m.id}`}
                        onClick={() => toggleMilestone(m)}
                      >
                        {m.completed ? "Reopen milestone" : "Mark all done"}
                      </button>
                      {editingMilestone?.id !== m.id && (
                        <button
                          type="button"
                          className="btn btn-ghost btn-sm"
                          onClick={() =>
                            setEditingMilestone({
                              id: m.id,
                              title: m.title,
                              description: m.description ?? "",
                              estimatedHours: m.estimatedHours ?? "",
                            })
                          }
                        >
                          Edit milestone
                        </button>
                      )}
                    </div>
                  </div>
                )}
              </li>
            );
          })}
        </ol>
      </section>
    </main>
  );
}
