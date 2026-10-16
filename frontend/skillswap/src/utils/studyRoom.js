// utils/studyRoom.js  (NEW FILE)
export const TOPICS = [
  "Java",
  "Spring Boot",
  "SQL",
  "Data Structures",
  "Interview Prep",
  "Frontend",
  "Python",
  "General",
];

export const DIFFICULTIES = [
  { value: "BEGINNER", label: "Beginner" },
  { value: "INTERMEDIATE", label: "Intermediate" },
  { value: "ADVANCED", label: "Advanced" },
];

export const ROOM_FILTERS = [
  { value: "ALL", label: "All rooms" },
  { value: "AVAILABLE", label: "Has space" },
  { value: "ACTIVE", label: "Active now" },
  { value: "SCHEDULED", label: "Scheduled" },
];

export const PHASES = [
  { value: "FOCUS", label: "Focus", minutes: 25 },
  { value: "SHORT_BREAK", label: "Short break", minutes: 5 },
  { value: "LONG_BREAK", label: "Long break", minutes: 15 },
];

export const difficultyLabel = (v) =>
  DIFFICULTIES.find((d) => d.value === v)?.label ?? "—";

export const phaseLabel = (v) =>
  PHASES.find((p) => p.value === v)?.label ?? "—";

export function formatClock(totalSeconds) {
  const s = Math.max(0, Math.floor(totalSeconds));
  const m = Math.floor(s / 60);
  return `${String(m).padStart(2, "0")}:${String(s % 60).padStart(2, "0")}`;
}

export function formatDateTime(iso) {
  if (!iso) return "—";
  return new Date(iso).toLocaleString([], {
    dateStyle: "medium",
    timeStyle: "short",
  });
}

export function formatTime(iso) {
  if (!iso) return "";
  return new Date(iso).toLocaleTimeString([], {
    hour: "2-digit",
    minute: "2-digit",
  });
}

// ISO string <-> <input type="datetime-local"> value
export function toLocalInput(iso) {
  if (!iso) return "";
  const d = new Date(iso);
  const pad = (n) => String(n).padStart(2, "0");
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
}
export const fromLocalInput = (v) => (v ? new Date(v).toISOString() : null);

/**
 * Seconds left on the shared timer.
 * `timer.offset` = (server clock - this browser's clock) measured when the state arrived,
 * so a wrong clock on this computer does not change what everybody sees.
 */
export function timerRemaining(timer, nowMs) {
  if (!timer) return 0;
  if (timer.status === "RUNNING" && timer.endsAt) {
    return Math.max(
      0,
      Math.ceil((Date.parse(timer.endsAt) - (nowMs + timer.offset)) / 1000),
    );
  }
  if (timer.status === "PAUSED") return timer.remainingSeconds;
  if (timer.status === "FINISHED") return 0;
  return timer.durationSeconds;
}
