import { useEffect, useState } from "react";
import { useDispatch } from "react-redux";
import { bookNewSession } from "../store/sessionsSlice";
import Icon from "./Icon";

// "2026-10-07T14:30" in local time, which is what <input type="datetime-local"> expects
function nowForInput() {
  const d = new Date();
  d.setMinutes(d.getMinutes() - d.getTimezoneOffset());
  return d.toISOString().slice(0, 16);
}

export default function BookingModal({ provider, skill, onClose, onBooked }) {
  const dispatch = useDispatch();
  const [min] = useState(nowForInput);
  const [time, setTime] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null); // { message, status }
  const [done, setDone] = useState(false);

  useEffect(() => {
    const onKey = (e) => e.key === "Escape" && onClose();
    document.addEventListener("keydown", onKey);
    return () => document.removeEventListener("keydown", onKey);
  }, [onClose]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!time || time < min) {
      setError({ message: "Pick a date and time in the future", status: null });
      return;
    }
    setError(null);
    setSubmitting(true);
    try {
      await dispatch(
        bookNewSession({
          providerId: provider.id,
          skillId: skill.id,
          sessionTime: time, // e.g. 2026-10-07T14:30, parsed as LocalDateTime
        }),
      ).unwrap();
      setDone(true);
      onBooked?.();
    } catch (err) {
      // err = { message, status } from the thunk's rejectWithValue
      setError({ message: err.message, status: err.status ?? null });
    } finally {
      setSubmitting(false);
    }
  };

  const conflict = error?.status === 409;

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-ink/50 p-4"
      onClick={onClose}
    >
      <div
        role="dialog"
        aria-modal="true"
        aria-labelledby="booking-title"
        className="max-h-full w-full max-w-md overflow-y-auto rounded-[2rem] bg-cream p-6 shadow-xl sm:p-7"
        onClick={(e) => e.stopPropagation()} // clicks inside shouldn't close it
      >
        <div className="flex items-start justify-between gap-3">
          <div className="flex items-start gap-3">
            <span className="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-lavender">
              <Icon name="calendar" size={19} />
            </span>
            <h2
              id="booking-title"
              className="text-xl font-bold leading-snug tracking-tight"
            >
              Book {skill.name} with {provider.name}
            </h2>
          </div>
          <button
            type="button"
            onClick={onClose}
            aria-label="Close"
            title="Close"
            className="btn-icon -mr-1 -mt-1 shrink-0"
          >
            <Icon name="x" size={18} />
          </button>
        </div>

        {done ? (
          <>
            <p className="alert alert-success mt-5">
              <Icon name="circle-check" size={18} className="mt-0.5" />
              <span>
                Request sent! {provider.name} will see it in their
                notifications.
              </span>
            </p>
            <button onClick={onClose} className="btn btn-primary mt-5">
              <Icon name="check" size={16} />
              Done
            </button>
          </>
        ) : (
          <form onSubmit={handleSubmit} className="mt-5 space-y-4">
            <label className="block text-sm font-medium">
              <span className="flex items-center gap-2">
                <Icon name="clock" size={15} />
                Date and time
              </span>
              <input
                type="datetime-local"
                autoFocus
                min={min}
                value={time}
                onChange={(e) => setTime(e.target.value)}
                className="input mt-2"
              />
            </label>

            {error && (
              <div
                role="alert"
                className={`alert ${conflict ? "alert-warning" : "alert-error"}`}
              >
                <Icon name="alert-circle" size={18} className="mt-0.5" />
                <div>
                  <p>{error.message}</p>
                  {conflict && (
                    <p className="mt-1 opacity-80">
                      Choose a different time and try again.
                    </p>
                  )}
                </div>
              </div>
            )}

            <div className="flex flex-wrap justify-end gap-2 pt-1">
              <button
                type="button"
                onClick={onClose}
                className="btn btn-ghost"
              >
                Cancel
              </button>
              <button disabled={submitting} className="btn btn-primary">
                <Icon name="send" size={15} />
                {submitting ? "Sending..." : "Request session"}
              </button>
            </div>
          </form>
        )}
      </div>
    </div>
  );
}
