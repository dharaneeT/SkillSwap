// components/studyroom/RoomFormModal.jsx  (NEW FILE)
// Used for "Create room" and "Edit room". The backend validates everything again.
import { useEffect, useRef, useState } from "react";
import Icon from "../Icon";
import { getErrorMessage } from "../../api/axios";
import {
  DIFFICULTIES,
  TOPICS,
  fromLocalInput,
  toLocalInput,
} from "../../utils/studyRoom";

export default function RoomFormModal({
  title,
  initial,
  submitLabel,
  onSubmit,
  onClose,
}) {
  const [form, setForm] = useState(() => ({
    name: initial?.room.name ?? "",
    topic: initial?.room.topic ?? "",
    description: initial?.room.description ?? "",
    difficulty: initial?.room.difficulty ?? "BEGINNER",
    maxParticipants: initial?.room.maxParticipants ?? 10,
    visibility: initial?.room.visibility ?? "PUBLIC",
    scheduledStart: toLocalInput(initial?.room.scheduledStart),
    durationMinutes: initial?.room.durationMinutes ?? "",
    membersCanPause: initial?.membersCanPause ?? false,
  }));
  const [error, setError] = useState("");
  const [saving, setSaving] = useState(false);
  const firstField = useRef(null);

  useEffect(() => {
    firstField.current?.focus();
    const onKey = (e) => e.key === "Escape" && onClose();
    document.addEventListener("keydown", onKey);
    return () => document.removeEventListener("keydown", onKey);
  }, [onClose]);

  const set = (k) => (e) =>
    setForm((f) => ({
      ...f,
      [k]: e.target.type === "checkbox" ? e.target.checked : e.target.value,
    }));

  const submit = async (e) => {
    e.preventDefault();
    setError("");
    const max = Number(form.maxParticipants);
    if (!Number.isInteger(max) || max < 2 || max > 50) {
      setError("Maximum participants must be a whole number from 2 to 50");
      return;
    }
    setSaving(true);
    try {
      await onSubmit({
        name: form.name.trim(),
        topic: form.topic.trim(),
        description: form.description.trim() || null,
        difficulty: form.difficulty,
        maxParticipants: max,
        visibility: form.visibility,
        scheduledStart: fromLocalInput(form.scheduledStart),
        durationMinutes: form.durationMinutes
          ? Number(form.durationMinutes)
          : null,
        membersCanPause: form.membersCanPause,
      });
    } catch (err) {
      setError(getErrorMessage(err));
      setSaving(false);
    }
  };

  return (
    <div
      className="fixed inset-0 z-50 flex items-end justify-center bg-ink/40 p-0 sm:items-center sm:p-4"
      role="presentation"
      onMouseDown={(e) => e.target === e.currentTarget && onClose()}
    >
      <form
        onSubmit={submit}
        role="dialog"
        aria-modal="true"
        aria-labelledby="room-form-title"
        className="max-h-[92vh] w-full max-w-lg space-y-4 overflow-y-auto rounded-t-3xl bg-cream p-5 sm:rounded-3xl sm:p-6"
      >
        <div className="flex items-center justify-between">
          <h2 id="room-form-title" className="section-title">
            {title}
          </h2>
          <button
            type="button"
            className="btn-icon"
            onClick={onClose}
            aria-label="Close"
          >
            <Icon name="x" size={18} />
          </button>
        </div>

        {error && (
          <p role="alert" className="alert alert-error">
            <Icon name="alert-circle" size={18} className="mt-0.5" />
            <span>{error}</span>
          </p>
        )}

        <label className="block space-y-1">
          <span className="text-sm font-medium">Room name</span>
          <input
            ref={firstField}
            className="input"
            required
            minLength={3}
            maxLength={80}
            value={form.name}
            onChange={set("name")}
            placeholder="Java Beginners"
          />
        </label>

        <label className="block space-y-1">
          <span className="text-sm font-medium">Topic or skill</span>
          <input
            className="input"
            required
            maxLength={60}
            list="room-topics"
            value={form.topic}
            onChange={set("topic")}
            placeholder="Java"
          />
          <datalist id="room-topics">
            {TOPICS.map((t) => (
              <option key={t} value={t} />
            ))}
          </datalist>
        </label>

        <label className="block space-y-1">
          <span className="text-sm font-medium">Description (optional)</span>
          <textarea
            className="textarea"
            rows={3}
            maxLength={500}
            value={form.description}
            onChange={set("description")}
          />
        </label>

        <div className="grid gap-3 sm:grid-cols-2">
          <label className="block space-y-1">
            <span className="text-sm font-medium">Difficulty</span>
            <select
              className="input select w-full"
              value={form.difficulty}
              onChange={set("difficulty")}
            >
              {DIFFICULTIES.map((d) => (
                <option key={d.value} value={d.value}>
                  {d.label}
                </option>
              ))}
            </select>
          </label>
          <label className="block space-y-1">
            <span className="text-sm font-medium">Max participants (2-50)</span>
            <input
              className="input"
              type="number"
              min={2}
              max={50}
              required
              value={form.maxParticipants}
              onChange={set("maxParticipants")}
            />
          </label>
        </div>

        <fieldset className="space-y-2">
          <legend className="text-sm font-medium">Who can join?</legend>
          <div className="flex flex-wrap gap-2">
            {[
              ["PUBLIC", "Public (anyone)"],
              ["PRIVATE", "Private (access code)"],
            ].map(([v, label]) => (
              <button
                key={v}
                type="button"
                className="chip"
                aria-pressed={form.visibility === v}
                onClick={() => setForm((f) => ({ ...f, visibility: v }))}
              >
                {label}
              </button>
            ))}
          </div>
          {form.visibility === "PRIVATE" && (
            <p className="meta">
              A code is generated for you. Only you (the host) can see it.
            </p>
          )}
        </fieldset>

        <div className="grid gap-3 sm:grid-cols-2">
          <label className="block space-y-1">
            <span className="text-sm font-medium">
              Scheduled start (optional)
            </span>
            <input
              className="input"
              type="datetime-local"
              value={form.scheduledStart}
              onChange={set("scheduledStart")}
            />
          </label>
          <label className="block space-y-1">
            <span className="text-sm font-medium">
              Session length, min (optional)
            </span>
            <input
              className="input"
              type="number"
              min={5}
              max={480}
              value={form.durationMinutes}
              onChange={set("durationMinutes")}
              placeholder="e.g. 60"
            />
          </label>
        </div>
        <p className="meta -mt-2">
          If you set a session length, a live session ends by itself after that
          time.
        </p>

        <label className="flex items-center gap-2 text-sm">
          <input
            type="checkbox"
            checked={form.membersCanPause}
            onChange={set("membersCanPause")}
          />
          Let every participant pause and resume the shared timer
        </label>

        <div className="flex justify-end gap-2 pt-1">
          <button type="button" className="btn btn-secondary" onClick={onClose}>
            Cancel
          </button>
          <button type="submit" className="btn btn-primary" disabled={saving}>
            {saving ? "Saving..." : submitLabel}
          </button>
        </div>
      </form>
    </div>
  );
}
