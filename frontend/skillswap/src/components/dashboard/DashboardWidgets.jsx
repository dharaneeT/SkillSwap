// components/dashboard/DashboardWidgets.jsx  (NEW FILE)
// The bento cards. Each one is "dumb": it only receives numbers and draws them.
import { useState } from "react";
import { Link } from "react-router-dom";
import Icon from "../Icon";
import Ring from "./Ring";
import Sparkline from "./Sparkline";
import { useCountUp } from "../../hooks/useCountUp";
import {
  PERIODS,
  countdownText,
  ratingLabel,
  timeOf,
} from "../../utils/dashboardStats";

const lift =
  "transition duration-300 motion-safe:hover:-translate-y-1 hover:shadow-lg";

/* ---------------- Progress: rings + period switch ---------------- */
export function ProgressCard({ stats, period, onPeriod }) {
  const [active, setActive] = useState(null);
  const overall = useCountUp(stats.overall);

  const rings = [
    {
      key: "learn",
      label: "Learning",
      value: stats.learn.pct,
      color: "#4169ff",
      track: "#e6ebff",
      sub: `${stats.learn.done}/${stats.learn.goal} sessions learned`,
    },
    {
      key: "teach",
      label: "Teaching",
      value: stats.teach.pct,
      color: "#be86ef",
      track: "#f3e8ff",
      sub: `${stats.teach.done}/${stats.teach.goal} sessions taught`,
    },
    {
      key: "reviews",
      label: "Reviews",
      value: stats.reviews.pct,
      color: "#8fcf2f",
      track: "#eef7d8",
      sub: stats.reviews.total
        ? `${stats.reviews.done}/${stats.reviews.total} sessions reviewed`
        : "Nothing to review yet",
    },
  ];

  return (
    <section
      aria-label="Progress"
      className={`card card-white flex h-full flex-col gap-5 ${lift}`}
    >
      <div className="flex flex-wrap items-center justify-between gap-2">
        <h2 className="section-title">Progress</h2>
        <div role="group" aria-label="Time period" className="flex gap-1">
          {PERIODS.map((p) => (
            <button
              key={p.id}
              type="button"
              className="chip px-3 py-1 text-xs"
              aria-pressed={period === p.id}
              onClick={() => onPeriod(p.id)}
            >
              {p.label}
            </button>
          ))}
        </div>
      </div>

      <Ring rings={rings} active={active}>
        <span className="text-4xl font-extrabold leading-none tracking-tight">
          {overall}%
        </span>
        <span className="meta mt-1">goal completion</span>
      </Ring>

      <ul className="space-y-1">
        {rings.map((r) => (
          <li key={r.key}>
            <button
              type="button"
              onMouseEnter={() => setActive(r.key)}
              onMouseLeave={() => setActive(null)}
              onFocus={() => setActive(r.key)}
              onBlur={() => setActive(null)}
              className="flex w-full items-start justify-between gap-3 rounded-2xl p-2 text-left transition-colors hover:bg-surface"
            >
              <span className="flex min-w-0 items-start gap-3">
                <span
                  className="mt-1.5 h-3 w-3 shrink-0 rounded-full"
                  style={{ background: r.color }}
                />
                <span className="min-w-0">
                  <span className="block font-semibold">{r.label}</span>
                  <span className="meta block truncate">{r.sub}</span>
                </span>
              </span>
              <span className="font-bold tabular-nums">{r.value}%</span>
            </button>
          </li>
        ))}
      </ul>
    </section>
  );
}

/* ---------------- Pulse: lime card + animated line chart ---------------- */
export function PulseCard({ stats, periodLabel }) {
  const count = useCountUp(stats.sessions);
  const { current, activeThisWeek } = stats.streak;
  const mood =
    current >= 3 ? "On a roll" : activeThisWeek ? "Active" : "Warming up";
  const note =
    current >= 2
      ? `${current}-week streak. Keep it alive this week!`
      : activeThisWeek
        ? "Nice, you've been active this week."
        : "Finish a session this week to start a streak.";

  return (
    <section
      aria-label="Activity pulse"
      className={`card flex h-full flex-col gap-3 bg-[#e4f58c] ${lift}`}
    >
      <div className="flex items-center gap-3">
        <Icon name="trending-up" size={22} />
        <h2 className="text-lg font-semibold">Activity pulse</h2>
      </div>
      <div className="flex flex-wrap items-end gap-3">
        <p className="text-5xl font-extrabold leading-none tracking-tight">
          {count}
          <span className="ml-2 text-xl font-medium">sessions</span>
        </p>
        <span className="rounded-full bg-white/70 px-3 py-1 text-sm font-semibold">
          {mood}
        </span>
      </div>
      <p className="text-sm text-ink/80">
        {periodLabel.toLowerCase()} · {note}
      </p>
      <div className="mt-auto pt-2">
        <Sparkline data={stats.series} />
      </div>
    </section>
  );
}

/* ---------------- Reputation: rating + segmented bar ---------------- */
export function ReputationCard({ standing, name }) {
  const avg = Number(standing?.averageRating) || 0;
  const count = Number(standing?.reviewCount) || 0;
  const shown = useCountUp(avg, { decimals: 1 });

  return (
    <section
      aria-label="Reputation"
      className={`card card-periwinkle flex h-full flex-col gap-3 ${lift}`}
    >
      <div className="flex items-center justify-between gap-3">
        <div className="flex items-center gap-3">
          <Icon name="star" size={22} />
          <h2 className="text-lg font-semibold">Reputation</h2>
        </div>
        {standing?.rank && (
          <Link to="/leaderboard" className="tag bg-white/70 hover:bg-white">
            <Icon name="trophy" size={13} />
            Rank #{standing.rank}
          </Link>
        )}
      </div>

      <div className="flex flex-wrap items-end gap-3 pt-2">
        <p className="text-5xl font-extrabold leading-none tracking-tight">
          {count ? shown : "-"}
          <span className="ml-1 text-xl font-medium">/ 5</span>
        </p>
        <span className="rounded-full bg-white/70 px-3 py-1 text-sm font-semibold">
          {ratingLabel(avg, count)}
        </span>
      </div>

      <div
        role="img"
        aria-label={
          count ? `Average rating ${avg.toFixed(1)} out of 5` : "No ratings yet"
        }
        className="mt-3 flex gap-2"
      >
        {[0, 1, 2, 3, 4].map((i) => (
          <span
            key={i}
            className="h-2 flex-1 overflow-hidden rounded-full bg-navy/15"
          >
            <span
              className="block h-full rounded-full bg-navy transition-all duration-1000 ease-out"
              style={{ width: `${Math.max(0, Math.min(1, avg - i)) * 100}%` }}
            />
          </span>
        ))}
      </div>
      <p className="mt-auto pt-2 text-sm text-ink/80">
        {count
          ? `From ${count} review${count > 1 ? "s" : ""}. Keep up your good work, ${name}!`
          : "Teach a session to collect your first review."}
      </p>
    </section>
  );
}

/* ---------------- Next session: live countdown ---------------- */
export function NextSessionCard({ next, meId, now }) {
  if (!next) {
    return (
      <section
        aria-label="Next session"
        className={`card card-white flex h-full flex-col gap-3 ${lift}`}
      >
        <h2 className="flex items-center gap-2 text-lg font-semibold">
          <Icon name="calendar" size={20} />
          Next session
        </h2>
        <p className="text-sm text-muted">
          Nothing booked. Find someone to swap skills with and it will show up
          here.
        </p>
        <Link
          to="/matches"
          className="btn btn-primary btn-sm mt-auto self-start"
        >
          <Icon name="users" size={14} />
          Find people
        </Link>
      </section>
    );
  }

  const teaching = next.providerId === meId;
  const who = teaching ? next.learnerName : next.providerName;
  const start = timeOf(next);
  const live = start <= now;

  return (
    <section
      aria-label="Next session"
      className={`card card-white flex h-full flex-col gap-3 ${lift}`}
    >
      <div className="flex items-center justify-between gap-2">
        <h2 className="flex items-center gap-2 text-lg font-semibold">
          <Icon name="calendar" size={20} />
          Next session
        </h2>
        <span className={`tag ${live ? "bg-[#d9f2e3] text-[#17623a]" : ""}`}>
          {live && <span className="live-dot" />}
          {countdownText(start, now, next.durationMinutes)}
        </span>
      </div>

      <div>
        <p className="card-title text-xl">{next.skillName}</p>
        <p className="meta mt-0.5">
          {teaching ? "Teaching" : "Learning from"} {who}
        </p>
      </div>

      <p className="meta flex flex-wrap items-center gap-x-3 gap-y-1">
        <span className="inline-flex items-center gap-1.5">
          <Icon name="clock" size={14} />
          {new Date(start).toLocaleString([], {
            weekday: "short",
            day: "numeric",
            month: "short",
            hour: "2-digit",
            minute: "2-digit",
          })}
        </span>
        {next.durationMinutes && <span>{next.durationMinutes} min</span>}
      </p>

      {next.meetingLink ? (
        <a
          href={next.meetingLink}
          target="_blank"
          rel="noopener noreferrer"
          className="btn btn-primary mt-auto self-start"
        >
          <Icon name="arrow-right" size={15} />
          Join call
        </a>
      ) : (
        <p className="meta mt-auto">No call link yet.</p>
      )}
    </section>
  );
}

/* ---------------- Small clickable stat tile ---------------- */
export function StatTile({
  icon,
  label,
  value,
  suffix = "",
  onClick,
  tone = "",
}) {
  const n = useCountUp(value);
  const body = (
    <>
      <span className="flex h-9 w-9 items-center justify-center rounded-full bg-white/80">
        <Icon name={icon} size={17} />
      </span>
      <span className="mt-3 block text-2xl font-extrabold leading-none tracking-tight tabular-nums">
        {n}
        <span className="ml-1 text-sm font-medium">{suffix}</span>
      </span>
      <span className="meta mt-1 block">{label}</span>
    </>
  );
  const cls = `card block h-full w-full text-left ${tone || "card-white"} p-4 sm:p-4 ${lift}`;
  return onClick ? (
    <button type="button" onClick={onClick} className={cls}>
      {body}
    </button>
  ) : (
    <div className={cls}>{body}</div>
  );
}

/* ---------------- Profile: avatar, level, key numbers ---------------- */
export function ProfileCard({ me, stats, rank }) {
  const credits = useCountUp(me?.credits);
  const taught = useCountUp(stats.totals.taught);
  const learned = useCountUp(stats.totals.learned);
  const { level } = stats;

  return (
    <section
      aria-label="My profile"
      className={`card card-white flex h-full flex-col gap-4 ${lift}`}
    >
      <div className="flex items-center gap-4">
        <span className="flex h-16 w-16 shrink-0 items-center justify-center rounded-full bg-linear-to-br from-lavender to-periwinkle text-2xl font-extrabold uppercase text-navy">
          {me?.name?.[0] ?? <Icon name="user" size={26} />}
        </span>
        <div className="min-w-0">
          <h2 className="truncate text-2xl font-bold leading-tight tracking-tight">
            {me?.name}
          </h2>
          <p className="meta mt-1 flex flex-wrap items-center gap-x-3 gap-y-1">
            <span className="inline-flex items-center gap-1.5">
              <Icon name="medal" size={15} />
              {level.name}
            </span>
            {rank && (
              <span className="inline-flex items-center gap-1.5">
                <Icon name="trophy" size={15} />
                Rank #{rank}
              </span>
            )}
          </p>
        </div>
      </div>

      <div className="grid grid-cols-3 gap-2 rounded-3xl bg-periwinkle/50 p-4 text-center">
        {[
          ["Credits", credits],
          ["Taught", taught],
          ["Learned", learned],
        ].map(([label, v]) => (
          <div key={label}>
            <p className="meta">{label}</p>
            <p className="mt-1 text-2xl font-extrabold tabular-nums">{v}</p>
          </div>
        ))}
      </div>

      <div>
        <div
          role="progressbar"
          aria-label={
            level.next ? `Progress to ${level.next.name}` : "Top level reached"
          }
          aria-valuemin={0}
          aria-valuemax={100}
          aria-valuenow={level.pct}
          className="h-2 overflow-hidden rounded-full bg-surface"
        >
          <div
            className="h-full rounded-full bg-brand transition-all duration-1000"
            style={{ width: `${level.pct}%` }}
          />
        </div>
        <p className="meta mt-2">
          {level.next
            ? `${level.remaining} more session${level.remaining === 1 ? "" : "s"} to become ${level.next.name}`
            : "You've reached the top level. Legend!"}
        </p>
      </div>
    </section>
  );
}
