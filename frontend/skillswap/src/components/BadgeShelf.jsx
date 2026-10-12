import BadgeIcon from "./BadgeIcon";
import { getBadgeStatus } from "../utils/badges";

export default function BadgeShelf({ stats }) {
  const badges = getBadgeStatus(stats);
  const earnedCount = badges.filter((b) => b.earned).length;

  return (
    <div>
      <p className="meta mb-3" aria-live="polite">
        {earnedCount} of {badges.length} badges earned
      </p>

      <ul className="grid grid-cols-1 gap-3 min-[420px]:grid-cols-2 lg:grid-cols-3">
        {badges.map((b) => (
          <li
            key={b.id}
            className={`card card-white flex items-start gap-3 p-4 ${b.earned ? "" : "bg-surface/60"}`}
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
    </div>
  );
}
