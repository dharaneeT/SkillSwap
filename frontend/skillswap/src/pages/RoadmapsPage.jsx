// pages/RoadmapsPage.jsx  (NEW FILE)  route: /roadmaps
import { useState } from "react";
import { Link } from "react-router-dom";
import {
  deleteRoadmap,
  fetchRoadmapDashboard,
  fetchRoadmaps,
} from "../api/roadmapApi";
import { getErrorMessage } from "../api/axios";
import { useFetch } from "../hooks/useFetch";
import Icon from "../components/Icon";
import ProgressBar from "../components/roadmap/ProgressBar";
import { dueLabel, formatDate, levelLabel } from "../utils/roadmap";

function Stat({ label, value, hint }) {
  return (
    <div className="card card-white">
      <p className="eyebrow">{label}</p>
      <p className="mt-1 text-3xl font-extrabold tracking-tight">{value}</p>
      {hint && <p className="meta mt-1">{hint}</p>}
    </div>
  );
}

export default function RoadmapsPage() {
  const list = useFetch(fetchRoadmaps);
  const dash = useFetch(fetchRoadmapDashboard);
  const [actionError, setActionError] = useState("");
  const [deletingId, setDeletingId] = useState(null);

  const roadmaps = list.data ?? [];
  const d = dash.data;

  const handleDelete = async (r) => {
    if (
      !window.confirm(
        `Delete "${r.title}"? Its progress will be removed too. This can't be undone.`,
      )
    )
      return;
    setDeletingId(r.id);
    setActionError("");
    try {
      await deleteRoadmap(r.id);
      list.refetch();
      dash.refetch();
    } catch (e) {
      setActionError(getErrorMessage(e));
    } finally {
      setDeletingId(null);
    }
  };

  return (
    <main className="page max-w-6xl space-y-8">
      <header className="flex flex-wrap items-end justify-between gap-4">
        <div className="flex flex-col gap-2">
          <p className="eyebrow">Plan your learning</p>
          <h1 className="page-title">Learning roadmaps</h1>
          <p className="page-sub max-w-xl">
            Turn a goal into a step-by-step plan, tick off tasks and watch your
            progress grow.
          </p>
        </div>
        <Link to="/roadmaps/new" className="btn btn-primary">
          <Icon name="plus" size={16} />
          New roadmap
        </Link>
      </header>

      {(list.error || dash.error || actionError) && (
        <p role="alert" className="alert alert-error">
          <Icon name="alert-circle" size={18} className="mt-0.5" />
          <span>{actionError || list.error || dash.error}</span>
        </p>
      )}

      {!list.data && list.loading && (
        <p className="text-sm text-muted">Loading your roadmaps...</p>
      )}

      {list.data && roadmaps.length === 0 && (
        <div className="card flex flex-wrap items-center justify-between gap-3">
          <p className="flex items-center gap-2 text-sm text-muted">
            <Icon name="trending-up" size={16} />
            You don&apos;t have a roadmap yet. Create one in about a minute.
          </p>
          <Link to="/roadmaps/new" className="btn btn-primary btn-sm">
            Create my first roadmap
          </Link>
        </div>
      )}

      {d && roadmaps.length > 0 && (
        <>
          <section
            aria-label="Overview"
            className="grid gap-3 sm:grid-cols-2 lg:grid-cols-4"
          >
            <Stat
              label="Roadmaps"
              value={d.totalRoadmaps}
              hint={`${d.activeRoadmaps} active · ${d.completedRoadmaps} completed`}
            />
            <Stat
              label="Overall progress"
              value={`${d.overallProgressPercent}%`}
              hint={`${d.completedTasks} of ${d.totalTasks} tasks`}
            />
            <Stat
              label="Milestones"
              value={`${d.completedMilestones} / ${d.completedMilestones + d.remainingMilestones}`}
              hint={`${d.remainingMilestones} remaining`}
            />
            <Stat
              label="Study time"
              value={`${d.hoursCompleted}h`}
              hint={`${d.hoursRemaining}h remaining (estimated)`}
            />
          </section>

          <div className="grid gap-6 lg:grid-cols-2">
            <section className="space-y-3" aria-label="Continue learning">
              <h2 className="section-title">Continue learning</h2>
              {d.inProgress.length === 0 && (
                <p className="meta">Nothing in progress right now.</p>
              )}
              {d.inProgress.map((r) => (
                <Link
                  key={r.id}
                  to={`/roadmaps/${r.id}`}
                  className="card card-periwinkle block space-y-1 text-navy"
                >
                  <p className="eyebrow text-navy/70">{r.title}</p>
                  <p className="card-title">
                    {r.next.taskTitle ?? r.next.milestoneTitle}
                  </p>
                  <p className="meta text-navy/80">
                    Milestone: {r.next.milestoneTitle}
                  </p>
                </Link>
              ))}
            </section>

            <section className="space-y-3" aria-label="Upcoming target dates">
              <h2 className="section-title">Upcoming target dates</h2>
              {d.upcomingDeadlines.length === 0 && (
                <p className="meta">No target dates set.</p>
              )}
              {d.upcomingDeadlines.map((r) => (
                <Link
                  key={r.id}
                  to={`/roadmaps/${r.id}`}
                  className="card card-white flex items-center justify-between gap-3"
                >
                  <span className="min-w-0">
                    <span className="card-title block truncate">{r.title}</span>
                    <span className="meta">{formatDate(r.targetDate)}</span>
                  </span>
                  <span
                    className={`badge ${r.daysLeft < 0 ? "badge-danger" : "badge-accepted"}`}
                  >
                    {dueLabel(r.daysLeft)}
                  </span>
                </Link>
              ))}
            </section>
          </div>
        </>
      )}

      {roadmaps.length > 0 && (
        <section className="space-y-3" aria-label="Your roadmaps">
          <h2 className="section-title">Your roadmaps</h2>
          <ul className="grid gap-4 md:grid-cols-2">
            {roadmaps.map((r) => (
              <li key={r.id} className="card flex flex-col gap-3">
                <div className="flex items-start justify-between gap-3">
                  <div className="min-w-0">
                    <p className="card-title truncate">{r.title}</p>
                    <p className="meta mt-0.5">
                      {levelLabel(r.currentLevel)}
                      {r.targetLevel ? ` → ${levelLabel(r.targetLevel)}` : ""}
                      {r.source === "GEMINI" ? " · AI generated" : ""}
                      {r.source === "TEMPLATE" ? " · Template" : ""}
                    </p>
                  </div>
                  <span
                    className={`badge ${r.status === "COMPLETED" ? "badge-completed" : "badge-accepted"}`}
                  >
                    {r.status === "COMPLETED" ? "Completed" : "Active"}
                  </span>
                </div>

                <ProgressBar
                  value={r.progressPercent}
                  label={`${r.title} progress`}
                />
                <p className="meta">
                  {r.progressPercent}% · {r.completedTasks}/{r.totalTasks} tasks
                  · {r.completedMilestones}/{r.totalMilestones} milestones
                  {r.targetDate ? ` · ${dueLabel(r.daysLeft)}` : ""}
                </p>

                {r.next && (
                  <p className="text-sm">
                    <span className="eyebrow mr-1">Next</span>
                    {r.next.taskTitle ?? r.next.milestoneTitle}
                  </p>
                )}

                <div className="mt-1 flex flex-wrap gap-2">
                  <Link
                    to={`/roadmaps/${r.id}`}
                    className="btn btn-primary btn-sm"
                  >
                    {r.status === "COMPLETED" ? "View" : "Resume"}
                    <Icon name="arrow-right" size={14} />
                  </Link>
                  <button
                    type="button"
                    className="btn btn-secondary btn-sm"
                    disabled={deletingId === r.id}
                    onClick={() => handleDelete(r)}
                  >
                    {deletingId === r.id ? "Deleting..." : "Delete"}
                  </button>
                </div>
              </li>
            ))}
          </ul>
        </section>
      )}
    </main>
  );
}
