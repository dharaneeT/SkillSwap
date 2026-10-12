import { Link } from "react-router-dom";
import Icon from "../Icon";
import BadgeIcon from "../BadgeIcon";
import {
  featuredBadges,
  getNextMilestone,
  normalizeStats,
} from "../../utils/badges";

const RANK_TONE = {
  1: "bg-[#fde7a0] text-[#7a5200]",
  2: "bg-[#e6e8ee] text-[#3d4354]",
  3: "bg-[#f6dcc3] text-[#7a3f12]",
};

const RANK_SIZE = {
  md: "h-9 w-9 text-sm",
  lg: "h-11 w-11 text-base",
  xl: "h-14 w-14 text-xl",
};

export function RankChip({ rank, size = "md" }) {
  return (
    <span
      aria-label={rank ? `Rank ${rank}` : "Unranked"}
      className={`flex shrink-0 items-center justify-center rounded-full font-extrabold ${
        RANK_SIZE[size]
      } ${RANK_TONE[rank] ?? "bg-surface text-muted"}`}
    >
      {rank ?? "-"}
    </span>
  );
}

export function Avatar({ name, size = "md" }) {
  const dim = size === "lg" ? "h-14 w-14 text-xl" : "h-10 w-10 text-base";
  return (
    <span
      aria-hidden="true"
      className={`flex ${dim} shrink-0 items-center justify-center rounded-full bg-lavender font-bold uppercase text-navy`}
    >
      {name?.[0] ?? <Icon name="user" size={18} />}
    </span>
  );
}

function RatingText({ entry }) {
  if (!entry.reviewCount) return <span className="meta">No reviews yet</span>;
  return (
    <span className="meta inline-flex items-center gap-1">
      <Icon name="star" size={13} filled className="text-brand" />
      <span className="font-semibold text-ink">
        {entry.averageRating.toFixed(1)}
      </span>
      ({entry.reviewCount})
    </span>
  );
}

function BadgeStrip({ entry }) {
  const { shown, earnedCount } = featuredBadges(entry);
  if (shown.length === 0) return null;
  const extra = earnedCount - shown.length;
  return (
    <ul className="flex items-center gap-1.5" aria-label="Badges earned">
      {shown.map((b) => (
        <li key={b.id}>
          <BadgeIcon badge={b} size="sm" />
        </li>
      ))}
      {extra > 0 && <li className="meta">+{extra}</li>}
    </ul>
  );
}

function MessageLink({ entry }) {
  return (
    <Link
      to={`/chat/${entry.userId}`}
      className="btn-icon"
      aria-label={`Message ${entry.name}`}
      title={`Message ${entry.name}`}
    >
      <Icon name="message-circle" size={17} />
    </Link>
  );
}

/* Top 3. Mobile: stacked in rank order. >= sm: 2nd | 1st | 3rd, winner raised. */
const PODIUM_ORDER = ["sm:order-2", "sm:order-1", "sm:order-3"];
const PODIUM_COLS = {
  1: "sm:grid-cols-1 sm:max-w-xs sm:mx-auto",
  2: "sm:grid-cols-2",
  3: "sm:grid-cols-3",
};

export function Podium({ entries, myId }) {
  if (entries.length === 0) return null;
  const arranged = entries.length === 3;

  return (
    <ol
      aria-label="Top teachers"
      className={`grid gap-3 sm:items-end ${PODIUM_COLS[entries.length]}`}
    >
      {entries.map((e, i) => {
        const first = e.rank === 1;
        const isMe = e.userId === myId;
        return (
          <li
            key={e.userId}
            className={`card flex flex-col items-center gap-3 text-center ${
              first ? "card-lavender sm:pb-8 sm:pt-8" : "card-white"
            } ${isMe ? "ring-2 ring-brand" : ""} ${arranged ? PODIUM_ORDER[i] : ""}`}
          >
            <RankChip rank={e.rank} size={first ? "lg" : "md"} />
            <Avatar name={e.name} size="lg" />

            <div className="min-w-0 max-w-full">
              <p className="card-title truncate">{e.name}</p>
              {isMe && <span className="tag mt-1 bg-white">You</span>}
            </div>

            <p>
              <span className="text-3xl font-extrabold leading-none tracking-tight">
                {e.sessionsTaught}
              </span>
              <span className="meta ml-1.5">
                session{e.sessionsTaught === 1 ? "" : "s"} taught
              </span>
            </p>

            <RatingText entry={e} />
            <BadgeStrip entry={e} />
            {!isMe && <MessageLink entry={e} />}
          </li>
        );
      })}
    </ol>
  );
}

/* Rank 4+ rows */
export function LeaderRow({ entry, isMe }) {
  return (
    <li
      className={`card card-white flex items-center gap-3 py-3 sm:gap-4 sm:py-4 ${
        isMe ? "ring-2 ring-brand" : ""
      }`}
    >
      <RankChip rank={entry.rank} />
      <Avatar name={entry.name} />

      <div className="min-w-0 flex-1">
        <p className="card-title flex items-center gap-2">
          <span className="truncate">{entry.name}</span>
          {isMe && <span className="tag shrink-0">You</span>}
        </p>
        <div className="mt-1 flex flex-wrap items-center gap-x-3 gap-y-1">
          <BadgeStrip entry={entry} />
          <span className="md:hidden">
            <RatingText entry={entry} />
          </span>
        </div>
      </div>

      <div className="hidden w-28 shrink-0 justify-end md:flex">
        <RatingText entry={entry} />
      </div>

      <div className="w-14 shrink-0 text-right sm:w-20">
        <p className="text-xl font-extrabold leading-none tracking-tight">
          {entry.sessionsTaught}
        </p>
        <p className="meta mt-1">taught</p>
      </div>

      <div className="w-10 shrink-0">
        {!isMe && <MessageLink entry={entry} />}
      </div>
    </li>
  );
}

/* "Your standing" card */
function Stat({ label, children }) {
  return (
    <div className="rounded-2xl bg-white/70 px-3 py-2.5">
      <p className="text-lg font-extrabold leading-none sm:text-xl">
        {children}
      </p>
      <p className="meta mt-1">{label}</p>
    </div>
  );
}

export function StandingCard({ standing }) {
  const s = normalizeStats(standing);
  const next = getNextMilestone(standing);
  const remaining = next ? next.target - s.sessionsTaught : 0;

  return (
    <section
      aria-labelledby="standing-title"
      className="card card-periwinkle grid gap-5 md:grid-cols-[auto_1fr] md:items-center md:gap-8"
    >
      <div className="flex items-center gap-4">
        <RankChip rank={standing.rank} size="xl" />
        <div>
          <h2 id="standing-title" className="eyebrow">
            Your standing
          </h2>
          <p className="text-2xl font-extrabold leading-tight tracking-tight">
            {standing.rank ? `Rank #${standing.rank}` : "Not ranked yet"}
          </p>
        </div>
      </div>

      <div className="space-y-4">
        <div className="grid grid-cols-3 gap-2 sm:gap-3">
          <Stat label="taught">{s.sessionsTaught}</Stat>
          <Stat label="reviews">{s.reviewCount}</Stat>
          <Stat label="avg rating">
            {s.reviewCount ? s.averageRating.toFixed(1) : "-"}
          </Stat>
        </div>

        {next ? (
          <div className="flex items-center gap-3">
            <BadgeIcon badge={next} earned={false} />
            <div className="min-w-0 flex-1">
              <p className="text-sm font-semibold">
                {remaining} more session{remaining === 1 ? "" : "s"} to unlock
                &ldquo;{next.label}&rdquo;
              </p>
              <div
                role="progressbar"
                aria-label={`Progress towards ${next.label}`}
                aria-valuemin={0}
                aria-valuemax={next.target}
                aria-valuenow={next.current}
                className="mt-1.5 h-2 overflow-hidden rounded-full bg-white/70"
              >
                <div
                  className="h-full rounded-full bg-brand"
                  style={{ width: `${next.pct}%` }}
                />
              </div>
            </div>
          </div>
        ) : (
          <p className="flex items-center gap-2 text-sm font-semibold">
            <Icon name="trophy" size={16} />
            You&apos;ve unlocked every teaching milestone.
          </p>
        )}
      </div>
    </section>
  );
}
