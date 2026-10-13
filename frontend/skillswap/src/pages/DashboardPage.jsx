// pages/DashboardPage.jsx  (EXISTING FILE - replaced. NEW: bento layout, rings, pulse chart,
// next-session countdown, clickable stat tiles, level, session tabs, live refresh.
// All the old actions (accept / reject / complete / cancel / review) still work.)
import { useEffect, useMemo, useRef, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { Link } from "react-router-dom";
import { fetchMe } from "../api/userApi";
import { fetchMyStanding } from "../api/leaderboardApi"; // NEW (optional: rating + rank)
import { addReview } from "../api/reviewApi";
import { getErrorMessage } from "../api/axios";
import { useFetch } from "../hooks/useFetch";
import { usePolling } from "../hooks/usePolling"; // NEW
import { useNow } from "../hooks/useNow"; // NEW
import { useCountUp } from "../hooks/useCountUp"; // NEW
import { changeSessionStatus, loadSessions } from "../store/sessionsSlice";
import Icon from "../components/Icon";
import Reveal from "../components/dashboard/Reveal"; // NEW
import SessionItem from "../components/dashboard/SessionItem"; // NEW
import {
  NextSessionCard,
  ProfileCard,
  ProgressCard,
  PulseCard,
  ReputationCard,
  StatTile,
} from "../components/dashboard/DashboardWidgets"; // NEW
import {
  CLOSED,
  OPEN,
  PERIODS,
  computeStats,
  greeting,
  timeOf,
} from "../utils/dashboardStats"; // NEW

// .unwrap() throws the rejectWithValue string; axios errors need getErrorMessage
const errMsg = (e) => (typeof e === "string" ? e : getErrorMessage(e));

const TABS = [
  { id: "all", label: "All" },
  { id: "pending", label: "Requests" },
  { id: "upcoming", label: "Upcoming" },
  { id: "completed", label: "Completed" },
  { id: "closed", label: "Closed" },
];

const inTab = (tab, s) =>
  tab === "all" ||
  (tab === "pending" && OPEN.includes(s.status)) ||
  (tab === "upcoming" && s.status === "ACCEPTED") ||
  (tab === "completed" && s.status === "COMPLETED") ||
  (tab === "closed" && CLOSED.includes(s.status));

function Skeleton() {
  return (
    <div
      className="grid animate-pulse gap-4 md:grid-cols-2 lg:grid-cols-3"
      aria-hidden="true"
    >
      <div className="h-96 rounded-[1.75rem] bg-surface md:row-span-2" />
      <div className="h-64 rounded-[1.75rem] bg-surface" />
      <div className="h-64 rounded-[1.75rem] bg-surface" />
      <div className="h-64 rounded-[1.75rem] bg-surface" />
      <div className="h-64 rounded-[1.75rem] bg-surface" />
    </div>
  );
}

export default function DashboardPage() {
  const dispatch = useDispatch();
  const { items: sessions, status } = useSelector((s) => s.sessions);

  const {
    data: me,
    error: meError,
    loading: meLoading,
    refetch: refetchMe,
  } = useFetch(fetchMe);
  const { data: standing } = useFetch(fetchMyStanding); // NEW: errors ignored, card shows "-"

  const [actionError, setActionError] = useState("");
  const [reviewing, setReviewing] = useState(null);
  const [review, setReview] = useState({ rating: 0, comment: "" });
  const [period, setPeriod] = useState("week"); // NEW
  const [tab, setTab] = useState("all"); // NEW
  const sessionsRef = useRef(null);

  const now = useNow(30000); // NEW: drives the countdown
  const error = actionError || meError;

  useEffect(() => {
    dispatch(loadSessions());
  }, [dispatch]);

  // NEW: keep the dashboard live (new requests appear without a refresh)
  usePolling(() => dispatch(loadSessions()), 30000);

  const loading =
    (meLoading && !me) ||
    status === "idle" ||
    (status === "loading" && sessions.length === 0);

  const stats = useMemo(
    () => computeStats(sessions, me?.id, period, now),
    [sessions, me?.id, period, now],
  );
  const periodLabel = PERIODS.find((p) => p.id === period).label;

  const counts = useMemo(
    () =>
      Object.fromEntries(
        TABS.map((t) => [t.id, sessions.filter((s) => inTab(t.id, s)).length]),
      ),
    [sessions],
  );
  const shown = useMemo(() => {
    const list = sessions.filter((s) => inTab(tab, s));
    const asc = tab === "upcoming" || tab === "pending";
    return [...list].sort((a, b) => {
      const ta = timeOf(a);
      const tb = timeOf(b);
      if (Number.isNaN(ta)) return 1;
      if (Number.isNaN(tb)) return -1;
      return asc ? ta - tb : tb - ta;
    });
  }, [sessions, tab]);

  const credits = useCountUp(me?.credits);

  const handleStatus = async (id, newStatus) => {
    setActionError("");
    try {
      await dispatch(changeSessionStatus({ id, status: newStatus })).unwrap();
      refetchMe();
    } catch (e) {
      setActionError(errMsg(e));
    }
  };

  const submitReview = async (sessionId) => {
    if (!review.rating) {
      setActionError("Choose a star rating first");
      return;
    }
    setActionError("");
    try {
      await addReview({
        sessionId,
        rating: review.rating,
        comment: review.comment.trim() || null,
      });
      setReviewing(null);
      setReview({ rating: 0, comment: "" });
      dispatch(loadSessions()); // refresh the reviewed flag
    } catch (e) {
      setActionError(getErrorMessage(e));
    }
  };

  // NEW: "Requests" tile jumps to the pending tab
  const goToRequests = () => {
    setTab("pending");
    sessionsRef.current?.scrollIntoView({ behavior: "smooth", block: "start" });
  };

  return (
    <main className="page max-w-6xl space-y-8">
      <Reveal>
        <header className="grid gap-4 md:grid-cols-[1fr_auto] md:items-stretch">
          <div className="flex flex-col justify-center gap-2">
            <p className="eyebrow">
              {new Date(now).toLocaleDateString([], {
                weekday: "long",
                day: "numeric",
                month: "long",
              })}
            </p>
            <h1 className="page-title">
              {greeting(now)}
              {me?.name ? `, ${me.name.split(" ")[0]}` : ""}
            </h1>
            <div className="flex flex-wrap gap-2 pt-1">
              <Link to="/matches" className="btn btn-primary btn-sm">
                <Icon name="users" size={14} />
                Find people
              </Link>
              <Link to="/leaderboard" className="btn btn-secondary btn-sm">
                <Icon name="trophy" size={14} />
                Leaderboard
              </Link>
              <Link to="/certificates" className="btn btn-secondary btn-sm">
                <Icon name="award" size={14} />
                Certificates
              </Link>
            </div>
          </div>

          <div className="card card-navy flex min-w-56 items-center justify-between gap-6 transition duration-300 motion-safe:hover:-translate-y-1 hover:shadow-lg">
            <div>
              <p className="text-4xl font-extrabold leading-none tracking-tight tabular-nums">
                {credits}
              </p>
              <p className="mt-1 text-sm text-white/75">credits</p>
            </div>
            <span className="flex h-12 w-12 items-center justify-center rounded-full bg-white/15">
              <Icon name="coins" size={22} />
            </span>
          </div>
        </header>
      </Reveal>

      {error && (
        <p role="alert" className="alert alert-error">
          <Icon name="alert-circle" size={18} className="mt-0.5" />
          <span>{error}</span>
        </p>
      )}

      {loading ? (
        <Skeleton />
      ) : (
        <>
          {/* quick numbers: click "Requests" to jump to them */}
          <div className="grid grid-cols-2 gap-3 md:grid-cols-4">
            <Reveal delay={50}>
              <StatTile
                icon="circle-check"
                label={`Sessions · ${periodLabel.toLowerCase()}`}
                value={stats.sessions}
                tone="card-lavender"
              />
            </Reveal>
            <Reveal delay={100}>
              <StatTile
                icon="clock"
                label="Minutes learning & teaching"
                value={stats.minutes}
                suffix="min"
                tone="card-periwinkle"
              />
            </Reveal>
            <Reveal delay={150}>
              <StatTile
                icon="bell"
                label="Requests waiting for you"
                value={stats.requests}
                onClick={goToRequests}
              />
            </Reveal>
            <Reveal delay={200}>
              <StatTile
                icon="trending-up"
                label="Week streak"
                value={stats.streak.current}
                suffix="wk"
              />
            </Reveal>
          </div>

          {/* bento grid: 1 column on phones, 2 on tablets, 3 on desktop */}
          <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
            <Reveal delay={100} className="md:row-span-2">
              <ProgressCard
                stats={stats}
                period={period}
                onPeriod={setPeriod}
              />
            </Reveal>
            <Reveal delay={160}>
              <PulseCard stats={stats} periodLabel={periodLabel} />
            </Reveal>
            <Reveal delay={220}>
              <NextSessionCard next={stats.next} meId={me?.id} now={now} />
            </Reveal>
            <Reveal delay={280}>
              <ReputationCard
                standing={standing}
                name={me?.name?.split(" ")[0]}
              />
            </Reveal>
            <Reveal delay={340}>
              <ProfileCard me={me} stats={stats} rank={standing?.rank} />
            </Reveal>
          </div>
        </>
      )}

      <section ref={sessionsRef} className="scroll-mt-24">
        <div className="mb-4 flex flex-wrap items-center justify-between gap-3">
          <h2 className="section-title flex items-center gap-2">
            <Icon name="calendar" size={20} />
            My sessions
          </h2>
          <div
            role="tablist"
            aria-label="Filter sessions"
            className="flex flex-wrap gap-1.5"
          >
            {TABS.map((t) => (
              <button
                key={t.id}
                role="tab"
                type="button"
                aria-selected={tab === t.id}
                aria-pressed={tab === t.id}
                onClick={() => setTab(t.id)}
                className="chip"
              >
                {t.label}
                <span
                  className={`rounded-full px-1.5 text-[11px] ${tab === t.id ? "bg-white/25" : "bg-surface"}`}
                >
                  {counts[t.id]}
                </span>
              </button>
            ))}
          </div>
        </div>

        {shown.length === 0 && (
          <p className="card flex items-center gap-2 text-sm text-muted">
            <Icon name="calendar" size={16} />
            {tab === "all" ? "No sessions yet." : "Nothing here."}
          </p>
        )}

        <ul role="tabpanel" className="space-y-3">
          {shown.map((s) => (
            <SessionItem
              key={s.id}
              s={s}
              meId={me?.id}
              onStatus={handleStatus}
              reviewing={reviewing}
              review={review}
              setReview={setReview}
              onStartReview={(id) => {
                setReviewing(id);
                setReview({ rating: 0, comment: "" });
              }}
              onCancelReview={() => setReviewing(null)}
              onSubmitReview={submitReview}
            />
          ))}
        </ul>
      </section>
    </main>
  );
}
