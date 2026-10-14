// utils/roadmap.js  (NEW FILE)
export const LEVELS = [
  { value: "BEGINNER", label: "Beginner" },
  { value: "INTERMEDIATE", label: "Intermediate" },
  { value: "ADVANCED", label: "Advanced" },
];

export const levelLabel = (v) =>
  LEVELS.find((l) => l.value === v)?.label ?? "—";

export const PREFERENCES = [
  { value: "THEORY", label: "Theory" },
  { value: "CODING_EXERCISES", label: "Coding exercises" },
  { value: "PROJECTS", label: "Projects" },
  { value: "MENTOR_LED", label: "Mentor-led" },
];

export const TASK_TYPES = [
  { value: "LEARN", label: "Learn" },
  { value: "PRACTICE", label: "Practice" },
  { value: "PROJECT", label: "Project" },
  { value: "REVIEW", label: "Review" },
  { value: "MENTOR_SESSION", label: "Mentor session" },
];

export const taskTypeLabel = (v) =>
  TASK_TYPES.find((t) => t.value === v)?.label ?? "Task";

// "2026-11-30" -> "30 Nov 2026" (parsed as a local date, so no timezone shift)
export function formatDate(iso) {
  if (!iso) return "—";
  const d = new Date(String(iso).length === 10 ? `${iso}T00:00:00` : iso);
  return d.toLocaleDateString([], { dateStyle: "medium" });
}

export function formatMinutes(min) {
  if (!min) return "";
  if (min < 60) return `${min} min`;
  const h = Math.floor(min / 60);
  const m = min % 60;
  return m ? `${h}h ${m}m` : `${h}h`;
}

export function dueLabel(daysLeft) {
  if (daysLeft === null || daysLeft === undefined) return "";
  if (daysLeft < 0)
    return `Overdue by ${-daysLeft} day${daysLeft === -1 ? "" : "s"}`;
  if (daysLeft === 0) return "Due today";
  return `${daysLeft} day${daysLeft === 1 ? "" : "s"} left`;
}

const pad = (n) => String(n).padStart(2, "0");
const toISO = (d) =>
  `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;

// call these from event handlers only (not while rendering)
export const todayISO = () => toISO(new Date());
export function addWeeksISO(weeks) {
  const d = new Date();
  d.setDate(d.getDate() + weeks * 7);
  return toISO(d);
}

const toInt = (v) => {
  if (v === "" || v === null || v === undefined) return null;
  const n = Number(v);
  return Number.isFinite(n) ? Math.round(n) : null;
};
const lines = (arr) => (arr ?? []).map((s) => String(s).trim()).filter(Boolean);

/** Turns the editable draft (strings everywhere) into what the backend expects. */
export function cleanDraft(d) {
  return {
    title: d.title.trim(),
    description: d.description?.trim() || null,
    learningGoal: d.learningGoal.trim(),
    targetSkill: d.targetSkill?.trim() || null,
    currentLevel: d.currentLevel,
    targetLevel: d.targetLevel || null,
    hoursPerWeek: toInt(d.hoursPerWeek),
    targetDate: d.targetDate || null,
    preferences: d.preferences ?? [],
    prerequisites: lines(d.prerequisites),
    source: d.source ?? "MANUAL",
    milestones: d.milestones.map((m) => ({
      title: m.title.trim(),
      description: m.description?.trim() || null,
      estimatedHours: toInt(m.estimatedHours),
      learningObjectives: lines(m.learningObjectives),
      completionCriteria: m.completionCriteria?.trim() || null,
      dependsOn: m.dependsOn ?? [],
      tasks: m.tasks.map((t) => ({
        title: t.title.trim(),
        description: t.description?.trim() || null,
        type: t.type || "LEARN",
        estimatedMinutes: toInt(t.estimatedMinutes),
      })),
    })),
  };
}

/** Returns an error message, or "" when the draft is fine to save. */
export function validateDraft(d) {
  if (!d.title.trim()) return "Give your roadmap a title.";
  if (d.milestones.length === 0) return "Add at least one milestone.";
  for (let i = 0; i < d.milestones.length; i++) {
    const m = d.milestones[i];
    if (!m.title.trim()) return `Milestone ${i + 1} needs a title.`;
    if (m.tasks.length === 0)
      return `Milestone ${i + 1} needs at least one task.`;
    if (m.tasks.some((t) => !t.title.trim()))
      return `Every task in milestone ${i + 1} needs a title.`;
    const hours = toInt(m.estimatedHours);
    if (hours !== null && (hours < 1 || hours > 300))
      return `Milestone ${i + 1}: hours must be between 1 and 300.`;
    for (const t of m.tasks) {
      const min = toInt(t.estimatedMinutes);
      if (min !== null && (min < 5 || min > 2400))
        return `Milestone ${i + 1}: task time must be between 5 and 2400 minutes.`;
    }
  }
  return "";
}
