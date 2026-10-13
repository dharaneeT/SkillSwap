import { useEffect, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { Link } from "react-router-dom";
import { fetchMe } from "../api/userApi";
import { addReview } from "../api/reviewApi";
import { getErrorMessage } from "../api/axios";
import { useFetch } from "../hooks/useFetch";
import { changeSessionStatus, loadSessions } from "../store/sessionsSlice";
import Icon from "../components/Icon";

const card = "card card-white";
const btn = "btn btn-sm";

// presentational only: maps a session status to a badge colour
const badgeClass = (status) => {
  if (["PENDING", "REQUESTED"].includes(status)) return "badge badge-pending";
  if (status === "ACCEPTED") return "badge badge-accepted";
  if (status === "COMPLETED") return "badge badge-completed";
  if (["REJECTED", "CANCELLED"].includes(status)) return "badge badge-danger";
  return "badge";
};

// .unwrap() throws the rejectWithValue string; axios errors need getErrorMessage
const errMsg = (e) => (typeof e === "string" ? e : getErrorMessage(e));

export default function DashboardPage() {
  const dispatch = useDispatch();

  const { items: sessions, status } = useSelector((s) => s.sessions);

  const {
    data: me,
    error: meError,
    loading: meLoading,
    refetch: refetchMe,
  } = useFetch(fetchMe);

  const [actionError, setActionError] = useState("");
  const [reviewing, setReviewing] = useState(null);
  const [review, setReview] = useState({
    rating: 0,
    comment: "",
  });

  const error = actionError || meError;

  useEffect(() => {
    dispatch(loadSessions());
  }, [dispatch]);

  const loading =
    (meLoading && !me) ||
    status === "idle" ||
    (status === "loading" && sessions.length === 0);

  const handleStatus = async (id, status) => {
    setActionError("");

    try {
      await dispatch(changeSessionStatus({ id, status })).unwrap();
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
      setReview({
        rating: 0,
        comment: "",
      });

      // Refresh the reviewed flag
      dispatch(loadSessions());
    } catch (e) {
      setActionError(getErrorMessage(e));
    }
  };

  return (
    <main className="page max-w-5xl space-y-10">
      <header className="grid gap-4 md:grid-cols-[1fr_auto] md:items-stretch">
        <div className="flex flex-col justify-center gap-2">
          <p className="eyebrow">Dashboard</p>
          <h1 className="page-title">Hi {me?.name}</h1>
        </div>

        <div className="card card-navy flex min-w-56 items-center justify-between gap-6">
          <div>
            <p className="text-4xl font-extrabold leading-none tracking-tight">
              {me?.credits}
            </p>
            <p className="mt-1 text-sm text-white/75">credits</p>
          </div>
          <span className="flex h-12 w-12 items-center justify-center rounded-full bg-white/15">
            <Icon name="coins" size={22} />
          </span>
        </div>
      </header>

      {error && (
        <p role="alert" className="alert alert-error">
          <Icon name="alert-circle" size={18} className="mt-0.5" />
          <span>{error}</span>
        </p>
      )}

      <div className="grid gap-4 sm:grid-cols-2">
        <Link
          to="/matches"
          className="card card-lavender flex items-center justify-between gap-4 transition-colors hover:bg-[#e2cbfb]"
        >
          <span className="flex items-center gap-3 font-semibold">
            <span className="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-white">
              <Icon name="users" size={19} />
            </span>
            Find people to learn from
          </span>
          <Icon name="arrow-right" size={20} />
        </Link>

        <Link
          to="/leaderboard"
          className="card card-periwinkle flex items-center justify-between gap-4 transition-colors hover:bg-[#c3d2ff]"
        >
          <span className="flex items-center gap-3 font-semibold">
            <span className="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-white">
              <Icon name="trophy" size={19} />
            </span>
            Leaderboard &amp; badges
          </span>
          <Icon name="arrow-right" size={20} />
        </Link>
      </div>

      <section>
        <h2 className="section-title mb-4 flex items-center gap-2">
          <Icon name="calendar" size={20} />
          My sessions
        </h2>

        {sessions.length === 0 && (
          <p className="card flex items-center gap-2 text-sm text-muted">
            <Icon name="calendar" size={16} />
            No sessions yet.
          </p>
        )}

        <ul className="space-y-3">
          {sessions.map((s) => {
            const isProvider = s.providerId === me?.id;
            const isLearner = s.learnerId === me?.id;

            const open = ["PENDING", "REQUESTED"].includes(s.status);

            const canReview =
              isLearner && s.status === "COMPLETED" && !s.reviewed;

            return (
              <li
                key={s.id}
                className={`${card} flex flex-wrap items-center justify-between gap-4`}
              >
                <div className="min-w-0">
                  <p className="card-title">
                    {s.skillName} —{" "}
                    {isProvider
                      ? `teaching ${s.learnerName}`
                      : `learning from ${s.providerName}`}
                  </p>

                  <p className="meta mt-1.5 flex flex-wrap items-center gap-x-3 gap-y-1.5">
                    <span className="inline-flex items-center gap-1.5">
                      <Icon name="clock" size={14} />
                      {s.sessionTime
                        ? new Date(s.sessionTime).toLocaleString()
                        : "no time set"}
                    </span>
                    {/* NEW: session length */}
                    {s.durationMinutes && <span>{s.durationMinutes} min</span>}
                    <span className={badgeClass(s.status)}>{s.status}</span>
                  </p>
                </div>

                <div className="flex flex-wrap items-center gap-2">
                  {/* NEW: video call link, created by the backend when the session is accepted */}
                  {s.status === "ACCEPTED" && s.meetingLink && (
                    <a
                      href={s.meetingLink}
                      target="_blank"
                      rel="noopener noreferrer"
                      className={`${btn} btn-tint`}
                    >
                      <Icon name="arrow-right" size={14} />
                      Join call
                    </a>
                  )}

                  {isProvider && open && (
                    <>
                      <button
                        onClick={() => handleStatus(s.id, "ACCEPTED")}
                        className={`${btn} btn-primary`}
                      >
                        <Icon name="check" size={14} />
                        Accept
                      </button>

                      <button
                        onClick={() => handleStatus(s.id, "REJECTED")}
                        className={`${btn} btn-secondary`}
                      >
                        <Icon name="x" size={14} />
                        Reject
                      </button>
                    </>
                  )}

                  {isProvider && s.status === "ACCEPTED" && (
                    <button
                      onClick={() => handleStatus(s.id, "COMPLETED")}
                      className={`${btn} btn-primary`}
                    >
                      <Icon name="circle-check" size={14} />
                      Mark completed
                    </button>
                  )}

                  {isLearner && (open || s.status === "ACCEPTED") && (
                    <button
                      onClick={() => handleStatus(s.id, "CANCELLED")}
                      className={`${btn} btn-secondary`}
                    >
                      <Icon name="x" size={14} />
                      Cancel
                    </button>
                  )}

                  {canReview && reviewing !== s.id && (
                    <button
                      onClick={() => {
                        setReviewing(s.id);
                        setReview({
                          rating: 0,
                          comment: "",
                        });
                      }}
                      className={`${btn} btn-primary`}
                    >
                      <Icon name="star" size={14} />
                      Leave a review
                    </button>
                  )}

                  {isLearner && s.status === "COMPLETED" && s.reviewed && (
                    <span className="badge badge-success">
                      <Icon name="check" size={12} strokeWidth={3} />
                      Reviewed
                    </span>
                  )}
                </div>

                {canReview && reviewing === s.id && (
                  <div className="w-full space-y-3 border-t border-line pt-4">
                    <p className="text-sm text-muted">
                      How was your session with {s.providerName}?
                    </p>

                    <div
                      className="flex gap-1"
                      role="radiogroup"
                      aria-label="Rating"
                    >
                      {[1, 2, 3, 4, 5].map((n) => (
                        <button
                          key={n}
                          type="button"
                          role="radio"
                          aria-checked={review.rating === n}
                          aria-label={`${n} star${n > 1 ? "s" : ""}`}
                          title={`${n} star${n > 1 ? "s" : ""}`}
                          onClick={() =>
                            setReview({
                              ...review,
                              rating: n,
                            })
                          }
                          className={`rounded-full p-1 transition-colors hover:bg-surface ${
                            n <= review.rating ? "text-brand" : "text-[#c9c7c0]"
                          }`}
                        >
                          <Icon name="star" size={28} filled />
                        </button>
                      ))}
                    </div>

                    <textarea
                      rows={3}
                      maxLength={500}
                      placeholder="Say something about the session (optional)"
                      value={review.comment}
                      onChange={(e) =>
                        setReview({
                          ...review,
                          comment: e.target.value,
                        })
                      }
                      className="textarea"
                    />

                    <div className="flex flex-wrap gap-2">
                      <button
                        onClick={() => submitReview(s.id)}
                        className={`${btn} btn-primary`}
                      >
                        <Icon name="send" size={14} />
                        Submit review
                      </button>

                      <button
                        onClick={() => setReviewing(null)}
                        className={`${btn} btn-secondary`}
                      >
                        Cancel
                      </button>
                    </div>
                  </div>
                )}
              </li>
            );
          })}
        </ul>
      </section>
    </main>
  );
}
