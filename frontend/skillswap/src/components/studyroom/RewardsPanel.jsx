// components/studyroom/RewardsPanel.jsx  (NEW FILE)
import { useEffect } from "react";
import { fetchMyAttendance } from "../../api/studyRoomApi";
import { useFetch } from "../../hooks/useFetch";
import { usePolling } from "../../hooks/usePolling";
import { formatDateTime } from "../../utils/studyRoom";

export default function RewardsPanel({
  roomId,
  reward,
  openCount,
  live,
  minutes,
}) {
  const att = useFetch(fetchMyAttendance, roomId);
  const { refetch } = att;
  usePolling(refetch, 30000, { enabled: live });
  useEffect(() => {
    refetch();
  }, [reward, openCount, refetch]);

  const cur = att.data?.current;
  const history = att.data?.history ?? [];
  const pct = cur
    ? Math.min(100, Math.round((cur.activeSeconds / cur.requiredSeconds) * 100))
    : 0;

  return (
    <section
      aria-label="Attendance and rewards"
      className="card card-white space-y-4"
    >
      <h2 className="section-title">Attendance & rewards</h2>
      {att.error && (
        <p role="alert" className="alert alert-error">
          {att.error}
        </p>
      )}

      {live ? (
        <div className="space-y-2">
          <p className="text-sm">
            Stay in the live session for <strong>{minutes} minutes</strong> (tab
            visible, with at least one other person) to earn a credit.
          </p>
          <div
            className="h-2 overflow-hidden rounded-full bg-surface"
            role="progressbar"
            aria-valuemin={0}
            aria-valuemax={100}
            aria-valuenow={pct}
            aria-label="Attendance progress"
          >
            <div
              className="h-full rounded-full bg-brand transition-all"
              style={{ width: `${pct}%` }}
            />
          </div>
          <p className="meta">
            {cur
              ? `${Math.floor(cur.activeSeconds / 60)} of ${minutes} min counted`
              : "Your time starts counting in a few seconds"}
            {cur?.rewardIssued ? " · reward received" : ""}
            {cur && cur.focusCycles > 0
              ? ` · ${cur.focusCycles} focus block(s)`
              : ""}
          </p>
        </div>
      ) : (
        <p className="meta">
          Attendance counts only while the host has a live session running.
        </p>
      )}

      <div>
        <h3 className="card-title mb-2">Your sessions in this room</h3>
        {history.length === 0 ? (
          <p className="meta">No sessions yet.</p>
        ) : (
          <ul className="divide-y divide-line rounded-2xl bg-cream text-sm">
            {history.map((h) => (
              <li
                key={h.sessionNo}
                className="flex flex-wrap items-center justify-between gap-2 px-3 py-2"
              >
                <span>
                  Session {h.sessionNo} · {formatDateTime(h.firstJoinedAt)}
                </span>
                <span className="flex items-center gap-2">
                  {Math.floor(h.activeSeconds / 60)} min
                  {h.rewardIssued ? (
                    <span className="badge badge-success">
                      {h.rewardCredits > 0
                        ? `+${h.rewardCredits} credit`
                        : "Done (daily limit)"}
                    </span>
                  ) : h.completed ? (
                    <span className="badge badge-success">Completed</span>
                  ) : (
                    <span className="badge">Not completed</span>
                  )}
                </span>
              </li>
            ))}
          </ul>
        )}
      </div>
      <p className="meta">
        The server decides rewards. Credits go into your normal SkillSwap
        balance, with a daily limit.
      </p>
    </section>
  );
}
