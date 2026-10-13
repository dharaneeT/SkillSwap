// components/dashboard/SessionItem.jsx  (NEW FILE)
// One session row with every action (accept / reject / complete / cancel / join / review).
import Icon from "../Icon";
import { OPEN, timeOf } from "../../utils/dashboardStats";

const btn = "btn btn-sm";

const badgeClass = (status) => {
  if (OPEN.includes(status)) return "badge badge-pending";
  if (status === "ACCEPTED") return "badge badge-accepted";
  if (status === "COMPLETED") return "badge badge-completed";
  if (["REJECTED", "CANCELLED"].includes(status)) return "badge badge-danger";
  return "badge";
};

export default function SessionItem({
  s,
  meId,
  onStatus,
  reviewing,
  review,
  setReview,
  onStartReview,
  onCancelReview,
  onSubmitReview,
}) {
  const isProvider = s.providerId === meId;
  const isLearner = s.learnerId === meId;
  const open = OPEN.includes(s.status);
  const canReview = isLearner && s.status === "COMPLETED" && !s.reviewed;
  const t = timeOf(s);
  const d = Number.isFinite(t) ? new Date(t) : null;

  return (
    <li className="card card-white flex flex-wrap items-center justify-between gap-4 transition-shadow hover:shadow-md">
      <div className="flex min-w-0 items-center gap-4">
        <div className="flex h-14 w-14 shrink-0 flex-col items-center justify-center rounded-2xl bg-surface leading-none">
          <span className="text-[10px] font-semibold uppercase tracking-wider text-muted">
            {d ? d.toLocaleDateString([], { month: "short" }) : "-"}
          </span>
          <span className="mt-1 text-xl font-extrabold">
            {d ? d.getDate() : "?"}
          </span>
        </div>

        <div className="min-w-0">
          <p className="card-title">
            {s.skillName} ·{" "}
            <span className="font-medium text-muted">
              {isProvider
                ? `teaching ${s.learnerName}`
                : `learning from ${s.providerName}`}
            </span>
          </p>
          <p className="meta mt-1.5 flex flex-wrap items-center gap-x-3 gap-y-1.5">
            <span className="inline-flex items-center gap-1.5">
              <Icon name="clock" size={14} />
              {d
                ? d.toLocaleTimeString([], {
                    hour: "2-digit",
                    minute: "2-digit",
                  })
                : "no time set"}
            </span>
            {s.durationMinutes && <span>{s.durationMinutes} min</span>}
            <span className={badgeClass(s.status)}>{s.status}</span>
          </p>
        </div>
      </div>

      <div className="flex flex-wrap items-center gap-2">
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
              onClick={() => onStatus(s.id, "ACCEPTED")}
              className={`${btn} btn-primary`}
            >
              <Icon name="check" size={14} />
              Accept
            </button>
            <button
              onClick={() => onStatus(s.id, "REJECTED")}
              className={`${btn} btn-secondary`}
            >
              <Icon name="x" size={14} />
              Reject
            </button>
          </>
        )}

        {isProvider && s.status === "ACCEPTED" && (
          <button
            onClick={() => onStatus(s.id, "COMPLETED")}
            className={`${btn} btn-primary`}
          >
            <Icon name="circle-check" size={14} />
            Mark completed
          </button>
        )}

        {isLearner && (open || s.status === "ACCEPTED") && (
          <button
            onClick={() => onStatus(s.id, "CANCELLED")}
            className={`${btn} btn-secondary`}
          >
            <Icon name="x" size={14} />
            Cancel
          </button>
        )}

        {canReview && reviewing !== s.id && (
          <button
            onClick={() => onStartReview(s.id)}
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

          <div className="flex gap-1" role="radiogroup" aria-label="Rating">
            {[1, 2, 3, 4, 5].map((n) => (
              <button
                key={n}
                type="button"
                role="radio"
                aria-checked={review.rating === n}
                aria-label={`${n} star${n > 1 ? "s" : ""}`}
                title={`${n} star${n > 1 ? "s" : ""}`}
                onClick={() => setReview({ ...review, rating: n })}
                className={`rounded-full p-1 transition-transform hover:scale-110 hover:bg-surface ${
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
            onChange={(e) => setReview({ ...review, comment: e.target.value })}
            className="textarea"
          />

          <div className="flex flex-wrap gap-2">
            <button
              onClick={() => onSubmitReview(s.id)}
              className={`${btn} btn-primary`}
            >
              <Icon name="send" size={14} />
              Submit review
            </button>
            <button onClick={onCancelReview} className={`${btn} btn-secondary`}>
              Cancel
            </button>
          </div>
        </div>
      )}
    </li>
  );
}
