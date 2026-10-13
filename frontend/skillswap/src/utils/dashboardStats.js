// utils/dashboardStats.js  (NEW FILE)
// Pure functions: everything on the dashboard is computed from the sessions list,
// so it works without any extra backend endpoint.

export const PERIODS = [
  { id: "week", label: "This week" },
  { id: "month", label: "This month" },
  { id: "all", label: "All time" },
];

// sessions you aim to complete in each period (change freely)
export const GOALS = {
  week: { teach: 2, learn: 2 },
  month: { teach: 6, learn: 6 },
  all: { teach: 10, learn: 10 },
};

export const OPEN = ["PENDING", "REQUESTED"];
export const CLOSED = ["REJECTED", "CANCELLED"];

const WEEK_MS = 7 * 86400000;
const DEFAULT_MINUTES = 30;

// "2026-10-07T14:30:00" has no timezone, so the browser reads it as local time (what we want)
export const timeOf = (s) =>
  s?.sessionTime ? new Date(s.sessionTime).getTime() : NaN;

export function weekStart(ms) {
  const d = new Date(ms);
  const sinceMonday = (d.getDay() + 6) % 7;
  return new Date(
    d.getFullYear(),
    d.getMonth(),
    d.getDate() - sinceMonday,
  ).getTime();
}

function periodStart(period, now) {
  if (period === "week") return weekStart(now);
  if (period === "month") {
    const d = new Date(now);
    return new Date(d.getFullYear(), d.getMonth(), 1).getTime();
  }
  return -Infinity;
}

const pct = (done, goal) =>
  goal > 0 ? Math.min(100, Math.round((done / goal) * 100)) : 0;

export const LEVELS = [
  { name: "Newcomer", min: 0 },
  { name: "Explorer", min: 1 },
  { name: "Contributor", min: 4 },
  { name: "Mentor", min: 10 },
  { name: "Master", min: 25 },
];

// level from total completed sessions (taught + learned)
export function getLevel(total) {
  let i = 0;
  LEVELS.forEach((l, idx) => {
    if (total >= l.min) i = idx;
  });
  const cur = LEVELS[i];
  const next = LEVELS[i + 1] ?? null;
  return {
    name: cur.name,
    next,
    remaining: next ? next.min - total : 0,
    pct: next
      ? Math.round(((total - cur.min) / (next.min - cur.min)) * 100)
      : 100,
  };
}

// weeks in a row with at least one completed session (as teacher OR learner)
export function weeklyStreak(completed, now) {
  const thisWeek = weekStart(now);
  const weeks = [
    ...new Set(
      completed
        .map((s) => weekStart(timeOf(s)))
        .filter((w) => Number.isFinite(w) && w <= thisWeek),
    ),
  ].sort((a, b) => a - b);
  const set = new Set(weeks);
  const activeThisWeek = set.has(thisWeek);

  let current = 0;
  let cursor = activeThisWeek ? thisWeek : weekStart(thisWeek - 1); // grace: streak lives until the week ends
  while (set.has(cursor)) {
    current++;
    cursor = weekStart(cursor - 1);
  }

  let longest = 0;
  let run = 0;
  let prev = null;
  for (const w of weeks) {
    run =
      prev !== null && weekStart(prev + WEEK_MS + 3600000) === w ? run + 1 : 1;
    longest = Math.max(longest, run);
    prev = w;
  }
  return { current, longest, activeThisWeek };
}

// completed sessions per week for the last `n` weeks (oldest first)
export function weeklySeries(completed, now, n = 8) {
  const thisWeek = weekStart(now);
  const starts = [];
  let w = thisWeek;
  for (let i = 0; i < n; i++) {
    starts.unshift(w);
    w = weekStart(w - 1);
  }
  const counts = new Map(starts.map((s) => [s, 0]));
  for (const s of completed) {
    const k = weekStart(timeOf(s));
    if (counts.has(k)) counts.set(k, counts.get(k) + 1);
  }
  return starts.map((start) => ({
    start,
    value: counts.get(start),
    label: new Date(start).toLocaleDateString([], {
      day: "numeric",
      month: "short",
    }),
  }));
}

export function computeStats(sessions, meId, period, now = Date.now()) {
  const from = periodStart(period, now);
  const completed = sessions.filter(
    (s) => s.status === "COMPLETED" && Number.isFinite(timeOf(s)),
  );
  const done = completed.filter((s) => timeOf(s) >= from);
  const taught = done.filter((s) => s.providerId === meId);
  const learned = done.filter((s) => s.learnerId === meId);
  const goal = GOALS[period];

  const reviewed = learned.filter((s) => s.reviewed).length;
  const reviewsPct = learned.length ? pct(reviewed, learned.length) : 0;

  const ringValues = [
    pct(learned.length, goal.learn),
    pct(taught.length, goal.teach),
  ];
  if (learned.length) ringValues.push(reviewsPct);
  const overall = Math.round(
    ringValues.reduce((a, b) => a + b, 0) / ringValues.length,
  );

  const totalTaught = completed.filter((s) => s.providerId === meId).length;
  const totalLearned = completed.filter((s) => s.learnerId === meId).length;

  const upcoming = sessions
    .filter(
      (s) =>
        s.status === "ACCEPTED" &&
        Number.isFinite(timeOf(s)) &&
        timeOf(s) + (s.durationMinutes || DEFAULT_MINUTES) * 60000 > now,
    )
    .sort((a, b) => timeOf(a) - timeOf(b));

  return {
    learn: {
      done: learned.length,
      goal: goal.learn,
      pct: pct(learned.length, goal.learn),
    },
    teach: {
      done: taught.length,
      goal: goal.teach,
      pct: pct(taught.length, goal.teach),
    },
    reviews: { done: reviewed, total: learned.length, pct: reviewsPct },
    overall,
    sessions: done.length,
    minutes: done.reduce(
      (sum, s) => sum + (s.durationMinutes || DEFAULT_MINUTES),
      0,
    ),
    requests: sessions.filter(
      (s) => OPEN.includes(s.status) && s.providerId === meId,
    ).length,
    streak: weeklyStreak(completed, now),
    series: weeklySeries(completed, now),
    upcoming,
    next: upcoming[0] ?? null,
    totals: {
      taught: totalTaught,
      learned: totalLearned,
      all: totalTaught + totalLearned,
    },
    level: getLevel(totalTaught + totalLearned),
  };
}

export function countdownText(startMs, now, durationMin = DEFAULT_MINUTES) {
  const diff = startMs - now;
  if (diff <= 0)
    return diff > -durationMin * 60000 ? "Happening now" : "Started";
  const m = Math.floor(diff / 60000);
  if (m < 1) return "Starts in under a minute";
  if (m < 60) return `Starts in ${m} min`;
  const h = Math.floor(m / 60);
  if (h < 24) return `Starts in ${h}h ${m % 60}m`;
  const d = Math.floor(h / 24);
  return `Starts in ${d} day${d > 1 ? "s" : ""}`;
}

export function greeting(now = Date.now()) {
  const h = new Date(now).getHours();
  if (h < 5) return "Burning the midnight oil";
  if (h < 12) return "Good morning";
  if (h < 17) return "Good afternoon";
  return "Good evening";
}

export function ratingLabel(avg, count) {
  if (!count) return "No reviews yet";
  if (avg >= 4.7) return "Outstanding";
  if (avg >= 4.3) return "Very trusted";
  if (avg >= 3.5) return "Trusted";
  return "Growing";
}
