import { useEffect, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { Link } from "react-router-dom";
import { fetchMe } from "../api/userApi";
import { addReview } from "../api/reviewApi";
import { getErrorMessage } from "../api/axios";
import { useFetch } from "../hooks/useFetch";
import { changeSessionStatus, loadSessions } from "../store/sessionsSlice";

const card = "rounded-xl bg-slate-800 border border-slate-700 p-4";
const btn = "rounded-lg px-3 py-1 text-sm font-semibold cursor-pointer";

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
    <main className="max-w-4xl mx-auto px-4 pt-10 space-y-10">
      <header className="flex justify-between items-end">
        <h1 className="text-2xl font-bold">Hi {me?.name}</h1>

        <p>
          <span className="text-3xl font-bold text-red-200">{me?.credits}</span>{" "}
          credits
        </p>
      </header>

      {error && (
        <p className="rounded-lg bg-red-900/50 border border-red-500 px-3 py-2 text-sm">
          {error}
        </p>
      )}

      <Link to="/matches" className="text-red-200 underline">
        Find people to learn from →
      </Link>

      <section>
        <h2 className="text-xl font-semibold mb-3">My sessions</h2>

        {sessions.length === 0 && (
          <p className="text-slate-400 text-sm">No sessions yet.</p>
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
                className={`${card} flex flex-wrap justify-between items-center gap-3`}
              >
                <div>
                  <p className="font-semibold">
                    {s.skillName} —{" "}
                    {isProvider
                      ? `teaching ${s.learnerName}`
                      : `learning from ${s.providerName}`}
                  </p>

                  <p className="text-sm text-slate-400">
                    {s.sessionTime
                      ? new Date(s.sessionTime).toLocaleString()
                      : "no time set"}{" "}
                    · {s.status}
                  </p>
                </div>

                <div className="flex gap-2 items-center">
                  {isProvider && open && (
                    <>
                      <button
                        onClick={() => handleStatus(s.id, "ACCEPTED")}
                        className={`${btn} bg-green-300 text-slate-900`}
                      >
                        Accept
                      </button>

                      <button
                        onClick={() => handleStatus(s.id, "REJECTED")}
                        className={`${btn} border border-slate-600`}
                      >
                        Reject
                      </button>
                    </>
                  )}

                  {isProvider && s.status === "ACCEPTED" && (
                    <button
                      onClick={() => handleStatus(s.id, "COMPLETED")}
                      className={`${btn} bg-red-200 text-slate-900`}
                    >
                      Mark completed
                    </button>
                  )}

                  {isLearner && (open || s.status === "ACCEPTED") && (
                    <button
                      onClick={() => handleStatus(s.id, "CANCELLED")}
                      className={`${btn} border border-slate-600`}
                    >
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
                      className={`${btn} bg-red-200 text-slate-900`}
                    >
                      Leave a review
                    </button>
                  )}

                  {isLearner && s.status === "COMPLETED" && s.reviewed && (
                    <span className="text-sm text-green-300">Reviewed ✓</span>
                  )}
                </div>

                {canReview && reviewing === s.id && (
                  <div className="w-full space-y-3 border-t border-slate-700 pt-3">
                    <p className="text-sm text-slate-300">
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
                          onClick={() =>
                            setReview({
                              ...review,
                              rating: n,
                            })
                          }
                          className={`text-3xl leading-none cursor-pointer ${
                            n <= review.rating
                              ? "text-yellow-300"
                              : "text-slate-600"
                          }`}
                        >
                          ★
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
                      className="w-full rounded-lg bg-slate-900 border border-slate-600 px-3 py-2 text-sm outline-none focus:border-red-200"
                    />

                    <div className="flex gap-2">
                      <button
                        onClick={() => submitReview(s.id)}
                        className={`${btn} bg-red-200 text-slate-900`}
                      >
                        Submit review
                      </button>

                      <button
                        onClick={() => setReviewing(null)}
                        className={`${btn} border border-slate-600`}
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
