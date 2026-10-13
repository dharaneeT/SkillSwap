// utils/badges.js   (EXISTING FILE - replaced. NEW: learning + streak badges, BADGE_GROUPS)
//
// This file is the ONLY place thresholds are defined.
// Stats come from GET /leaderboard/me  (sessionsTaught, reviewCount, averageRating)
// merged with GET /progress/me          (sessionsLearned, skillsLearned,
//                                        longestStreakWeeks, certificates)

export const BADGE_GROUPS = [
  { id: "teaching", label: "Teaching" }, // NEW (headings used by BadgeShelf)
  { id: "learning", label: "Learning" },
  { id: "streak", label: "Streaks" },
  { id: "reputation", label: "Reputation" },
];

export const BADGES = [
  // ---- teaching (unchanged) ----
  {
    id: "first-lesson",
    label: "First Lesson",
    description: "Taught your first session",
    icon: "graduation-cap",
    tone: "periwinkle",
    group: "teaching",
    metric: "sessionsTaught",
    target: 1,
  },
  {
    id: "sessions-5",
    label: "5 Sessions Taught",
    description: "Completed 5 sessions as a teacher",
    icon: "medal",
    tone: "lavender",
    group: "teaching",
    metric: "sessionsTaught",
    target: 5,
  },
  {
    id: "sessions-10",
    label: "10 Sessions Taught",
    description: "Completed 10 sessions as a teacher",
    icon: "award",
    tone: "orchid",
    group: "teaching",
    metric: "sessionsTaught",
    target: 10,
  },
  {
    id: "sessions-25",
    label: "25 Sessions Taught",
    description: "Completed 25 sessions as a teacher",
    icon: "trophy",
    tone: "navy",
    group: "teaching",
    metric: "sessionsTaught",
    target: 25,
  },
  {
    id: "sessions-50",
    label: "Master Mentor",
    description: "Completed 50 sessions as a teacher",
    icon: "crown",
    tone: "gold",
    group: "teaching",
    metric: "sessionsTaught",
    target: 50,
  },

  // ---- NEW: learning ----
  {
    id: "learned-1",
    label: "First Lesson Learned",
    description: "Finished your first session as a learner",
    icon: "book-open",
    tone: "periwinkle",
    group: "learning",
    metric: "sessionsLearned",
    target: 1,
  },
  {
    id: "learned-5",
    label: "5 Sessions Learned",
    description: "Finished 5 sessions as a learner",
    icon: "graduation-cap",
    tone: "lavender",
    group: "learning",
    metric: "sessionsLearned",
    target: 5,
  },
  {
    id: "explorer",
    label: "Skill Explorer",
    description: "Learned 3 different skills",
    icon: "search",
    tone: "orchid",
    group: "learning",
    metric: "skillsLearned",
    target: 3,
  },
  {
    id: "certified",
    label: "Certified Learner",
    description: "Earned your first certificate",
    icon: "shield-check",
    tone: "gold",
    group: "learning",
    metric: "certificates",
    target: 1,
  },

  // ---- NEW: streaks (a week counts if you finish a session as teacher OR learner) ----
  {
    id: "streak-3",
    label: "3-Week Streak",
    description: "A session every week for 3 weeks",
    icon: "trending-up",
    tone: "periwinkle",
    group: "streak",
    metric: "longestStreakWeeks",
    target: 3,
  },
  {
    id: "streak-8",
    label: "8-Week Streak",
    description: "A session every week for 8 weeks",
    icon: "calendar",
    tone: "navy",
    group: "streak",
    metric: "longestStreakWeeks",
    target: 8,
  },

  // ---- reputation (unchanged) ----
  {
    id: "reviews-5",
    label: "5 Reviews Earned",
    description: "Received 5 reviews from learners",
    icon: "message-circle",
    tone: "periwinkle",
    group: "reputation",
    metric: "reviewCount",
    target: 5,
  },
  {
    id: "top-rated",
    label: "Top Rated",
    description: "4.5+ average rating across at least 5 reviews",
    icon: "star",
    filled: true,
    tone: "gold",
    group: "reputation",
    metric: "reviewCount",
    target: 5,
    requires: (s) => s.averageRating >= 4.5,
    hint: "Needs a 4.5+ average rating",
  },
];

const num = (v) => (Number.isFinite(Number(v)) ? Number(v) : 0);

export function normalizeStats(raw) {
  const s = raw ?? {};
  return {
    sessionsTaught: num(s.sessionsTaught),
    reviewCount: num(s.reviewCount),
    averageRating: num(s.averageRating),
    // NEW
    sessionsLearned: num(s.sessionsLearned),
    skillsLearned: num(s.skillsLearned),
    longestStreakWeeks: num(s.longestStreakWeeks),
    certificates: num(s.certificates),
  };
}

export function getBadgeStatus(raw) {
  const s = normalizeStats(raw);
  return BADGES.map((b) => {
    const value = s[b.metric];
    const metricMet = value >= b.target;
    const earned = metricMet && (b.requires ? b.requires(s) : true);
    return {
      ...b,
      earned,
      current: Math.min(value, b.target),
      pct: Math.round(Math.min(value / b.target, 1) * 100),
      blockedByRequirement: metricMet && !earned,
    };
  });
}

export function getNextMilestone(raw) {
  return (
    getBadgeStatus(raw).find((b) => b.group === "teaching" && !b.earned) ?? null
  );
}

// only teaching + reputation badges are shown next to names on the leaderboard
// (learning/streak badges are private progress, shown on your own profile)
export function featuredBadges(raw, max = 3) {
  const earned = getBadgeStatus(raw).filter((b) => b.earned);
  const topTeaching = [...earned].reverse().find((b) => b.group === "teaching");
  const reputation = earned.filter((b) => b.group === "reputation");
  return {
    shown: [topTeaching, ...reputation].filter(Boolean).slice(0, max),
    earnedCount: earned.filter(
      (b) => b.group === "teaching" || b.group === "reputation",
    ).length,
  };
}
