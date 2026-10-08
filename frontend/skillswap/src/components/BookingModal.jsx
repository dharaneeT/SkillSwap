import { useEffect, useState } from "react";
import { useDispatch } from "react-redux";
import { bookNewSession } from "../store/sessionsSlice";

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
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 p-4"
      onClick={onClose}
    >
      <div
        role="dialog"
        aria-modal="true"
        aria-labelledby="booking-title"
        className="w-full max-w-md rounded-xl bg-slate-800 border border-slate-700 p-5"
        onClick={(e) => e.stopPropagation()} // clicks inside shouldn't close it
      >
        <h2 id="booking-title" className="text-lg font-semibold">
          Book {skill.name} with {provider.name}
        </h2>

        {done ? (
          <>
            <p className="mt-4 text-sm text-green-300">
              Request sent! {provider.name} will see it in their notifications.
            </p>
            <button
              onClick={onClose}
              className="mt-4 rounded-lg bg-red-200 text-slate-900 font-semibold px-4 py-2 cursor-pointer"
            >
              Done
            </button>
          </>
        ) : (
          <form onSubmit={handleSubmit} className="mt-4 space-y-3">
            <label className="block text-sm">
              Date and time
              <input
                type="datetime-local"
                autoFocus
                min={min}
                value={time}
                onChange={(e) => setTime(e.target.value)}
                className="mt-1 w-full rounded-lg bg-slate-900 border border-slate-600 px-3 py-2 outline-none focus:border-red-200"
              />
            </label>

            {error && (
              <div
                role="alert"
                className={`rounded-lg border px-3 py-2 text-sm ${
                  conflict
                    ? "bg-amber-900/40 border-amber-400"
                    : "bg-red-900/50 border-red-500"
                }`}
              >
                <p>{error.message}</p>
                {conflict && (
                  <p className="mt-1 opacity-80">
                    Choose a different time and try again.
                  </p>
                )}
              </div>
            )}

            <div className="flex justify-end gap-2 pt-1">
              <button
                type="button"
                onClick={onClose}
                className="rounded-lg px-4 py-2 text-sm cursor-pointer hover:bg-slate-700"
              >
                Cancel
              </button>
              <button
                disabled={submitting}
                className="rounded-lg bg-red-200 text-slate-900 font-semibold px-4 py-2 cursor-pointer disabled:opacity-50"
              >
                {submitting ? "Sending..." : "Request session"}
              </button>
            </div>
          </form>
        )}
      </div>
    </div>
  );
}
