// components/studyroom/TimerPanel.jsx  (NEW FILE)
// The countdown is calculated from the server's end time (timer.endsAt), never from a local counter.
import { useState } from "react";
import {
  timerPause,
  timerReset,
  timerResume,
  timerStart,
} from "../../api/studyRoomApi";
import { getErrorMessage } from "../../api/axios";
import { useNow } from "../../hooks/useNow";
import {
  PHASES,
  formatClock,
  phaseLabel,
  timerRemaining,
} from "../../utils/studyRoom";

export default function TimerPanel({
  roomId,
  timer,
  applyTimer,
  isHost,
  membersCanPause,
}) {
  const now = useNow(250);
  const [phase, setPhase] = useState("FOCUS");
  const [custom, setCustom] = useState("");
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);

  const canPause = isHost || membersCanPause;
  const remaining = timerRemaining(timer, now);
  const total = timer?.durationSeconds || 1;
  const pct = timer
    ? Math.min(100, Math.round(((total - remaining) / total) * 100))
    : 0;
  const status = timer?.status ?? "IDLE";

  const run = async (call) => {
    setError("");
    setBusy(true);
    try {
      const res = await call();
      applyTimer(res.data.data); // instant feedback; everybody else gets it over the websocket
    } catch (e) {
      setError(getErrorMessage(e));
    } finally {
      setBusy(false);
    }
  };

  const start = () => {
    const minutes = custom === "" ? null : Number(custom);
    if (
      minutes !== null &&
      (!Number.isFinite(minutes) || minutes < 1 || minutes > 180)
    ) {
      setError("Custom length must be between 1 and 180 minutes");
      return;
    }
    run(() =>
      timerStart(roomId, {
        phase,
        durationSeconds: minutes ? Math.round(minutes * 60) : null,
      }),
    );
  };

  return (
    <section
      aria-label="Shared focus timer"
      className="card card-white space-y-4"
    >
      <div className="flex items-center justify-between">
        <h2 className="section-title">Focus timer</h2>
        <span className="badge">
          {status === "RUNNING"
            ? "Running"
            : status === "PAUSED"
              ? "Paused"
              : status === "FINISHED"
                ? "Finished"
                : "Ready"}
        </span>
      </div>

      <div className="text-center">
        <p className="eyebrow">{timer ? phaseLabel(timer.phase) : "Focus"}</p>
        <p
          className="text-6xl font-extrabold tabular-nums tracking-tight"
          role="timer"
          aria-live="off"
          aria-label={`${formatClock(remaining)} remaining`}
        >
          {formatClock(remaining)}
        </p>
        <div
          className="mx-auto mt-3 h-2 max-w-xs overflow-hidden rounded-full bg-surface"
          aria-hidden="true"
        >
          <div
            className="h-full rounded-full bg-brand transition-all"
            style={{ width: `${pct}%` }}
          />
        </div>
        {status === "FINISHED" && (
          <p className="mt-2 text-sm font-medium text-brand">
            Time is up. Take a breather!
          </p>
        )}
        <p className="meta mt-2">Everyone in the room sees the same time.</p>
      </div>

      {isHost && (
        <div className="space-y-3">
          <div
            className="flex flex-wrap justify-center gap-2"
            role="group"
            aria-label="Timer phase"
          >
            {PHASES.map((p) => (
              <button
                key={p.value}
                type="button"
                className="chip"
                aria-pressed={phase === p.value}
                onClick={() => setPhase(p.value)}
              >
                {p.label} · {p.minutes}m
              </button>
            ))}
          </div>
          <label className="mx-auto flex max-w-xs items-center gap-2 text-sm">
            <span className="whitespace-nowrap">Custom (min)</span>
            <input
              className="input"
              type="number"
              min={1}
              max={180}
              value={custom}
              onChange={(e) => setCustom(e.target.value)}
              placeholder="optional"
            />
          </label>
        </div>
      )}

      <div className="flex flex-wrap justify-center gap-2">
        {isHost && (
          <button
            type="button"
            className="btn btn-primary"
            disabled={busy}
            onClick={start}
          >
            {status === "RUNNING" || status === "PAUSED" ? "Restart" : "Start"}
          </button>
        )}
        {canPause && status === "RUNNING" && (
          <button
            type="button"
            className="btn btn-secondary"
            disabled={busy}
            onClick={() => run(() => timerPause(roomId))}
          >
            Pause
          </button>
        )}
        {canPause && status === "PAUSED" && (
          <button
            type="button"
            className="btn btn-secondary"
            disabled={busy}
            onClick={() => run(() => timerResume(roomId))}
          >
            Resume
          </button>
        )}
        {isHost && status !== "IDLE" && (
          <button
            type="button"
            className="btn btn-ghost"
            disabled={busy}
            onClick={() => run(() => timerReset(roomId))}
          >
            Reset
          </button>
        )}
      </div>

      {!isHost && (
        <p className="meta text-center">
          {membersCanPause
            ? "The host starts and resets the timer. You can pause and resume."
            : "Only the host controls the timer."}
        </p>
      )}
      {error && (
        <p role="alert" className="alert alert-error">
          {error}
        </p>
      )}
    </section>
  );
}
