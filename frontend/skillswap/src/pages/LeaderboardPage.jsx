import { Link } from "react-router-dom";
import { fetchLeaderboard, fetchMyStanding } from "../api/leaderboardApi";
import { useFetch } from "../hooks/useFetch";
import Icon from "../components/Icon";
import BadgeShelf from "../components/BadgeShelf";
import {
  LeaderRow,
  Podium,
  StandingCard,
} from "../components/leaderboard/LeaderboardParts";

const LIMIT = 20;

// useFetch passes ONE argument (the params object) to the fetcher
const loadBoard = ({ limit }) => fetchLeaderboard(limit);

export default function LeaderboardPage() {
  const board = useFetch(loadBoard, { limit: LIMIT });
  const mine = useFetch(fetchMyStanding);

  const rows = board.data ?? [];
  const myId = mine.data?.userId;
  const podium = rows.slice(0, 3);
  const rest = rows.slice(3);
  const error = board.error || mine.error;

  return (
    <main className="page max-w-5xl space-y-10">
      <header className="flex flex-wrap items-end justify-between gap-4">
        <div className="flex flex-col gap-2">
          <p className="eyebrow">Leaderboard</p>
          <h1 className="page-title">Top teachers</h1>
          <p className="page-sub max-w-xl">
            Ranked by completed sessions taught, then rating. Earn badges as you
            hit milestones.
          </p>
        </div>

        <button
          type="button"
          onClick={() => {
            board.refetch();
            mine.refetch();
          }}
          disabled={board.loading}
          className="btn btn-secondary btn-sm"
        >
          <Icon name="repeat" size={15} />
          {board.loading ? "Refreshing..." : "Refresh"}
        </button>
      </header>

      {error && (
        <p role="alert" className="alert alert-error">
          <Icon name="alert-circle" size={18} className="mt-0.5" />
          <span>{error}</span>
        </p>
      )}

      {mine.data && <StandingCard standing={mine.data} />}

      <section aria-label="Rankings" className="space-y-4">
        {!board.data && board.loading && (
          <p className="text-sm text-muted">Loading...</p>
        )}

        {board.data && rows.length === 0 && (
          <div className="card flex flex-wrap items-center justify-between gap-3">
            <p className="flex items-center gap-2 text-sm text-muted">
              <Icon name="trophy" size={16} />
              Nobody is on the board yet. Complete a session to be the first.
            </p>
            <Link to="/matches" className="btn btn-primary btn-sm">
              Find people to swap with
            </Link>
          </div>
        )}

        <Podium entries={podium} myId={myId} />

        {rest.length > 0 && (
          <ol className="space-y-3" aria-label="Other teachers">
            {rest.map((e) => (
              <LeaderRow key={e.userId} entry={e} isMe={e.userId === myId} />
            ))}
          </ol>
        )}

        {rows.length >= LIMIT && (
          <p className="meta text-center">Showing the top {LIMIT}.</p>
        )}
      </section>

      {mine.data && (
        <section>
          <h2 className="section-title mb-4 flex items-center gap-2">
            <Icon name="medal" size={20} />
            Your badges
          </h2>
          <BadgeShelf stats={mine.data} />
        </section>
      )}
    </main>
  );
}
