// components/studyroom/ChallengesPanel.jsx  (NEW FILE)
import { useEffect, useState } from "react";
import {
  approveSubmission,
  closeChallenge,
  createChallenge,
  fetchChallenges,
  fetchSubmissions,
  rejectSubmission,
  submitChallenge,
} from "../../api/studyRoomApi";
import { getErrorMessage } from "../../api/axios";
import { useFetch } from "../../hooks/useFetch";
import Icon from "../Icon";
import { formatDateTime, fromLocalInput } from "../../utils/studyRoom";

const EMPTY = {
  title: "",
  description: "",
  completionCriteria: "",
  deadline: "",
  rewardCredits: 1,
  evidenceRequired: false,
};

const loadSubmissions = ({ roomId, cid }) => fetchSubmissions(roomId, cid);

function StatusBadge({ status }) {
  if (status === "APPROVED")
    return <span className="badge badge-success">Approved</span>;
  if (status === "REJECTED")
    return <span className="badge badge-danger">Rejected</span>;
  return <span className="badge badge-pending">Waiting for host</span>;
}

function Submissions({ roomId, challenge, onChanged }) {
  const subs = useFetch(loadSubmissions, { roomId, cid: challenge.id });
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(null);

  const review = async (s, approve) => {
    setBusy(s.id);
    setError("");
    try {
      await (approve ? approveSubmission : rejectSubmission)(
        roomId,
        challenge.id,
        s.id,
      );
      subs.refetch();
      onChanged();
    } catch (e) {
      setError(getErrorMessage(e));
    } finally {
      setBusy(null);
    }
  };

  const list = subs.data ?? [];
  return (
    <div className="mt-3 space-y-2 rounded-2xl bg-cream p-3">
      <p className="eyebrow">Submissions</p>
      {subs.loading && !subs.data && <p className="meta">Loading...</p>}
      {subs.error && (
        <p role="alert" className="alert alert-error">
          {subs.error}
        </p>
      )}
      {error && (
        <p role="alert" className="alert alert-error">
          {error}
        </p>
      )}
      {subs.data && list.length === 0 && (
        <p className="meta">Nobody has submitted yet.</p>
      )}
      {list.map((s) => (
        <div key={s.id} className="rounded-xl bg-white p-3 text-sm">
          <div className="flex flex-wrap items-center justify-between gap-2">
            <span className="font-semibold">{s.userName}</span>
            <StatusBadge status={s.status} />
          </div>
          {s.evidence && (
            <p className="mt-1 whitespace-pre-wrap wrap-break-word text-muted">
              {s.evidence}
            </p>
          )}
          {s.status === "PENDING" && (
            <div className="mt-2 flex gap-2">
              <button
                type="button"
                className="btn btn-primary btn-sm"
                disabled={busy === s.id}
                onClick={() => review(s, true)}
              >
                Approve
              </button>
              <button
                type="button"
                className="btn btn-secondary btn-sm"
                disabled={busy === s.id}
                onClick={() => review(s, false)}
              >
                Reject
              </button>
            </div>
          )}
          {s.status === "APPROVED" && (
            <p className="meta mt-1">{s.rewardPaid} credit(s) paid</p>
          )}
        </div>
      ))}
    </div>
  );
}

function ChallengeItem({ roomId, c, isHost, onChanged }) {
  const [evidence, setEvidence] = useState("");
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);
  const [showSubs, setShowSubs] = useState(false);
  const closed = c.status === "CLOSED";
  const mine = c.mine;
  const canSubmit = !isHost && !closed && (!mine || mine.status === "REJECTED");

  const submit = async (e) => {
    e.preventDefault();
    setBusy(true);
    setError("");
    try {
      await submitChallenge(roomId, c.id, evidence.trim());
      setEvidence("");
      onChanged();
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setBusy(false);
    }
  };

  const close = async () => {
    try {
      await closeChallenge(roomId, c.id);
      onChanged();
    } catch (err) {
      setError(getErrorMessage(err));
    }
  };

  return (
    <li className="rounded-2xl border border-line bg-white p-4">
      <div className="flex flex-wrap items-start justify-between gap-2">
        <h3 className="card-title">{c.title}</h3>
        <div className="flex flex-wrap gap-2">
          {closed && <span className="badge">Closed</span>}
          {c.rewardCredits > 0 && (
            <span className="badge badge-accepted">
              <Icon name="coins" size={11} /> up to {c.rewardCredits}
            </span>
          )}
        </div>
      </div>
      {c.description && (
        <p className="mt-1 text-sm text-muted">{c.description}</p>
      )}
      <p className="mt-2 text-sm">
        <span className="font-semibold">To complete:</span>{" "}
        {c.completionCriteria}
      </p>
      <p className="meta mt-1">
        {c.deadline ? `Due ${formatDateTime(c.deadline)} · ` : ""}
        {c.approvedCount} completed
        {c.evidenceRequired ? " · evidence required" : ""}
      </p>

      {mine && (
        <p className="mt-2 flex items-center gap-2 text-sm">
          Your submission: <StatusBadge status={mine.status} />
          {mine.status === "APPROVED" && (
            <span className="meta">{mine.rewardPaid} credit(s) earned</span>
          )}
        </p>
      )}

      {canSubmit && (
        <form onSubmit={submit} className="mt-3 space-y-2">
          <label className="block">
            <span className="sr-only">Your answer or link</span>
            <textarea
              className="textarea"
              rows={2}
              maxLength={1000}
              value={evidence}
              required={c.evidenceRequired}
              onChange={(e) => setEvidence(e.target.value)}
              placeholder={
                c.evidenceRequired
                  ? "Short answer or link (required)"
                  : "Short answer or link (optional)"
              }
            />
          </label>
          <button
            type="submit"
            className="btn btn-primary btn-sm"
            disabled={busy}
          >
            {mine?.status === "REJECTED" ? "Submit again" : "Mark as done"}
          </button>
        </form>
      )}
      {isHost && (
        <div className="mt-3 flex flex-wrap gap-2">
          <button
            type="button"
            className="btn btn-secondary btn-sm"
            onClick={() => setShowSubs((s) => !s)}
          >
            Submissions
            {c.pendingCount > 0 ? ` (${c.pendingCount} waiting)` : ""}
          </button>
          {!closed && (
            <button
              type="button"
              className="btn btn-ghost btn-sm"
              onClick={close}
            >
              Close challenge
            </button>
          )}
        </div>
      )}
      {isHost && showSubs && (
        <Submissions roomId={roomId} challenge={c} onChanged={onChanged} />
      )}
      {error && (
        <p role="alert" className="alert alert-error mt-2">
          {error}
        </p>
      )}
    </li>
  );
}

export default function ChallengesPanel({
  roomId,
  isHost,
  challengeTick,
  openCount,
}) {
  const list = useFetch(fetchChallenges, roomId);
  const [form, setForm] = useState(EMPTY);
  const [creating, setCreating] = useState(false);
  const [open, setOpen] = useState(false);
  const [error, setError] = useState("");
  const { refetch } = list;

  // somebody created / submitted / reviewed, or we reconnected
  useEffect(() => {
    if (challengeTick > 0 || openCount > 1) refetch();
  }, [challengeTick, openCount, refetch]);

  const set = (k) => (e) =>
    setForm((f) => ({
      ...f,
      [k]: e.target.type === "checkbox" ? e.target.checked : e.target.value,
    }));

  const create = async (e) => {
    e.preventDefault();
    setError("");
    setCreating(true);
    try {
      await createChallenge(roomId, {
        title: form.title.trim(),
        description: form.description.trim() || null,
        completionCriteria: form.completionCriteria.trim(),
        deadline: fromLocalInput(form.deadline),
        rewardCredits: Number(form.rewardCredits),
        evidenceRequired: form.evidenceRequired,
      });
      setForm(EMPTY);
      setOpen(false);
      refetch();
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setCreating(false);
    }
  };

  const items = list.data ?? [];

  return (
    <section
      aria-label="Group challenges"
      className="card card-white space-y-4"
    >
      <div className="flex items-center justify-between">
        <h2 className="section-title">Challenges</h2>
        {isHost && (
          <button
            type="button"
            className="btn btn-secondary btn-sm"
            onClick={() => setOpen((o) => !o)}
            aria-expanded={open}
          >
            <Icon name="plus" size={14} /> New challenge
          </button>
        )}
      </div>

      {isHost && open && (
        <form onSubmit={create} className="space-y-3 rounded-2xl bg-cream p-4">
          <input
            className="input"
            required
            maxLength={100}
            placeholder="Title, e.g. Solve 5 SQL queries"
            value={form.title}
            onChange={set("title")}
            aria-label="Title"
          />
          <textarea
            className="textarea"
            rows={2}
            maxLength={500}
            placeholder="Description (optional)"
            value={form.description}
            onChange={set("description")}
            aria-label="Description"
          />
          <input
            className="input"
            required
            maxLength={300}
            placeholder="How does someone complete it?"
            value={form.completionCriteria}
            onChange={set("completionCriteria")}
            aria-label="Completion criteria"
          />
          <div className="grid gap-3 sm:grid-cols-2">
            <label className="space-y-1">
              <span className="meta">Deadline (optional)</span>
              <input
                className="input"
                type="datetime-local"
                value={form.deadline}
                onChange={set("deadline")}
              />
            </label>
            <label className="space-y-1">
              <span className="meta">Reward credits (0-3)</span>
              <input
                className="input"
                type="number"
                min={0}
                max={3}
                value={form.rewardCredits}
                onChange={set("rewardCredits")}
              />
            </label>
          </div>
          <label className="flex items-center gap-2 text-sm">
            <input
              type="checkbox"
              checked={form.evidenceRequired}
              onChange={set("evidenceRequired")}
            />
            Participants must add a short answer or link
          </label>
          <p className="meta">
            Credits are paid once, after you approve a submission. A daily
            credit limit applies to every learner.
          </p>
          {error && (
            <p role="alert" className="alert alert-error">
              {error}
            </p>
          )}
          <button
            type="submit"
            className="btn btn-primary btn-sm"
            disabled={creating}
          >
            {creating ? "Creating..." : "Create challenge"}
          </button>
        </form>
      )}

      {list.error && (
        <div role="alert" className="alert alert-error">
          <span className="flex-1">{list.error}</span>
          <button
            type="button"
            className="btn btn-secondary btn-sm"
            onClick={refetch}
          >
            Retry
          </button>
        </div>
      )}
      {!list.data && list.loading && (
        <p className="meta">Loading challenges...</p>
      )}
      {list.data && items.length === 0 && (
        <p className="meta">
          {isHost
            ? "No challenges yet. Create one to get everyone working together."
            : "The host has not posted any challenges yet."}
        </p>
      )}
      <ul className="space-y-3">
        {items.map((c) => (
          <ChallengeItem
            key={c.id}
            roomId={roomId}
            c={c}
            isHost={isHost}
            onChanged={refetch}
          />
        ))}
      </ul>
    </section>
  );
}
