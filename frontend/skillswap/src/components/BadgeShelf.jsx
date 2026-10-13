// components/BadgeShelf.jsx   (EXISTING FILE - replaced. NEW: fetches learner progress itself,
// groups badges under headings, shows the streak banner and a link to certificates.)
import { Link } from "react-router-dom";
import BadgeIcon from "./BadgeIcon";
import Icon from "./Icon";
import { useFetch } from "../hooks/useFetch"; // NEW
import { fetchMyProgress } from "../api/progressApi"; // NEW
import { BADGE_GROUPS, getBadgeStatus } from "../utils/badges";

// `stats` = leaderboard row ({ sessionsTaught, reviewCount, averageRating }).
// Learner stats + streaks are fetched here, so ProfilePage / LeaderboardPage need no change.
export default function BadgeShelf({ stats }) {
  const { data: progress } = useFetch(fetchMyProgress); // NEW (errors are ignored on purpose)
  const merged = { ...stats, ...(progress ?? {}) };

  const badges = getBadgeStatus(merged);
  const earnedCount = badges.filter((b) => b.earned).length;

  return (
    <div className="space-y-6">
      {/* NEW: streak banner */}
      {progress && (
        <div className="card card-periwinkle flex flex-wrap items-center gap-x-6 gap-y-2">
          <p className="flex items-center gap-2 text-lg font-extrabold">
            <Icon name="trending-up" size={20} />
            {progress.currentStreakWeeks}-week streak
          </p>
          <p className="meta">
            Longest: {progress.longestStreakWeeks} weeks.{" "}
            {progress.activeThisWeek
              ? "You're active this week."
              : "Finish a session this week to keep it going."}
          </p>
        </div>
      )}

      <p className="meta" aria-live="polite">
        {earnedCount} of {badges.length} badges earned
      </p>

      {BADGE_GROUPS.map((g) => {
        const items = badges.filter((b) => b.group === g.id);
        if (items.length === 0) return null;
        return (
          <section key={g.id} aria-label={`${g.label} badges`}>
            <h3 className="eyebrow mb-3">{g.label}</h3>
            <ul className="grid grid-cols-1 gap-3 min-[420px]:grid-cols-2 lg:grid-cols-3">
              {items.map((b) => (
                <li
                  key={b.id}
                  className={`card card-white flex items-start gap-3 p-4 ${
                    b.earned ? "" : "bg-surface/60"
                  }`}
                >
                  <BadgeIcon badge={b} earned={b.earned} size="lg" />

                  <div className="min-w-0 flex-1">
                    <p className="card-title leading-tight">{b.label}</p>
                    <p className="meta mt-0.5">{b.description}</p>

                    {b.earned ? (
                      <p className="mt-2 text-xs font-semibold text-[#17623a]">
                        Earned
                      </p>
                    ) : (
                      <div className="mt-2">
                        <div
                          role="progressbar"
                          aria-label={`Progress towards ${b.label}`}
                          aria-valuemin={0}
                          aria-valuemax={b.target}
                          aria-valuenow={b.current}
                          className="h-1.5 overflow-hidden rounded-full bg-line"
                        >
                          <div
                            className="h-full rounded-full bg-brand"
                            style={{ width: `${b.pct}%` }}
                          />
                        </div>
                        <p className="meta mt-1">
                          {b.blockedByRequirement
                            ? b.hint
                            : `${b.current} / ${b.target}`}
                        </p>
                      </div>
                    )}
                  </div>
                </li>
              ))}
            </ul>
          </section>
        );
      })}

      {/* NEW: certificates link */}
      <Link
        to="/certificates"
        className="card card-lavender flex items-center justify-between gap-4 transition-colors hover:bg-[#e2cbfb]"
      >
        <span className="flex items-center gap-3 font-semibold">
          <span className="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-white">
            <Icon name="award" size={19} />
          </span>
          My certificates{progress ? ` (${progress.certificates})` : ""}
        </span>
        <Icon name="arrow-right" size={20} />
      </Link>
    </div>
  );
}
