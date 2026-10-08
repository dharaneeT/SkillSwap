import { useEffect, useMemo, useRef, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { Link } from "react-router-dom";
import { fetchMe } from "../api/userApi";
import { useAuth } from "../hooks/useAuth";
import { useFetch } from "../hooks/useFetch";
import { usePolling } from "../hooks/usePolling";
import { loadSessions } from "../store/sessionsSlice";

const POLL_MS = 15_000;
const DAY_MS = 24 * 60 * 60 * 1000;
const fmt = (iso) =>
  new Date(iso).toLocaleString([], { dateStyle: "medium", timeStyle: "short" });

function buildNotifications(sessions, myId, now) {
  const items = [];
  for (const s of sessions) {
    const asProvider = s.providerId === myId;
    const other = asProvider ? s.learnerName : s.providerName;
    const when = fmt(s.sessionTime);
    const add = (prefix, text) =>
      items.push({ id: `${prefix}-${s.id}`, sort: s.id, text });

    if (asProvider && s.status === "PENDING")
      add("req", `${other} requested ${s.skillName} on ${when}`);
    if (!asProvider && s.status === "ACCEPTED")
      add("acc", `${other} accepted your ${s.skillName} session (${when})`);
    if (!asProvider && s.status === "REJECTED")
      add("rej", `${other} declined your ${s.skillName} request`);
    if (!asProvider && s.status === "COMPLETED" && !s.reviewed)
      add("rev", `How was ${s.skillName} with ${other}? Leave a review`);

    const start = new Date(s.sessionTime).getTime();
    if (s.status === "ACCEPTED" && start > now && start - now < DAY_MS)
      add("soon", `Reminder: ${s.skillName} with ${other} on ${when}`);
  }
  return items.sort((a, b) => b.sort - a.sort); // newest sessions first
}

export default function NotificationCenter() {
  const dispatch = useDispatch();
  const { user } = useAuth();
  const sessions = useSelector((s) => s.sessions.items);
  const { data: me } = useFetch(fetchMe);

  const storageKey = `skillswap:seen:${user?.email}`;
  const [seen, setSeen] = useState(() => {
    try {
      return new Set(JSON.parse(localStorage.getItem(storageKey)) ?? []);
    } catch {
      return new Set();
    }
  });
  const [now, setNow] = useState(() => Date.now());
  const [open, setOpen] = useState(false);
  const rootRef = useRef(null);

  // initial load + poll every 15s (paused while the tab is hidden)
  useEffect(() => {
    dispatch(loadSessions());
  }, [dispatch]);
  usePolling(() => {
    dispatch(loadSessions());
    setNow(Date.now());
  }, POLL_MS);

  const items = useMemo(
    () => (me ? buildNotifications(sessions, me.id, now) : []),
    [sessions, me, now],
  );
  const unreadCount = items.filter((n) => !seen.has(n.id)).length;

  const persist = (set) => {
    setSeen(set);
    try {
      localStorage.setItem(storageKey, JSON.stringify([...set]));
    } catch {
      /* storage blocked: read-state just won't persist */
    }
  };
  const markRead = (id) => persist(new Set(seen).add(id));
  const markAllRead = () => persist(new Set(items.map((n) => n.id))); // also prunes old ids

  // close on outside click / Escape
  useEffect(() => {
    if (!open) return;
    const onDown = (e) => {
      if (!rootRef.current?.contains(e.target)) setOpen(false);
    };
    const onKey = (e) => e.key === "Escape" && setOpen(false);
    document.addEventListener("mousedown", onDown);
    document.addEventListener("keydown", onKey);
    return () => {
      document.removeEventListener("mousedown", onDown);
      document.removeEventListener("keydown", onKey);
    };
  }, [open]);

  return (
    <div ref={rootRef} className="relative">
      <button
        onClick={() => setOpen((o) => !o)}
        aria-label={`Notifications${unreadCount ? `, ${unreadCount} unread` : ""}`}
        aria-expanded={open}
        className="relative cursor-pointer"
      >
        <svg
          width="22"
          height="22"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          strokeWidth="2"
          strokeLinecap="round"
          strokeLinejoin="round"
        >
          <path d="M18 8a6 6 0 0 0-12 0c0 7-3 9-3 9h18s-3-2-3-9" />
          <path d="M13.7 21a2 2 0 0 1-3.4 0" />
        </svg>
        {unreadCount > 0 && (
          <span className="absolute -top-2 -right-2 min-w-4 h-4 px-1 rounded-full bg-red-500 text-[10px] font-bold flex items-center justify-center">
            {unreadCount > 9 ? "9+" : unreadCount}
          </span>
        )}
      </button>

      {open && (
        <div className="absolute right-0 mt-2 w-80 max-h-96 overflow-y-auto rounded-xl bg-slate-800 border border-slate-700 shadow-xl z-40">
          <div className="flex items-center justify-between p-3 border-b border-slate-700">
            <h3 className="font-semibold text-sm">Notifications</h3>
            {unreadCount > 0 && (
              <button
                onClick={markAllRead}
                className="text-xs text-red-200 cursor-pointer"
              >
                Mark all read
              </button>
            )}
          </div>

          {items.length === 0 ? (
            <p className="p-4 text-sm text-slate-400">You're all caught up.</p>
          ) : (
            <ul>
              {items.map((n) => {
                const isNew = !seen.has(n.id);
                return (
                  <li key={n.id}>
                    <Link
                      to="/dashboard"
                      onClick={() => {
                        markRead(n.id);
                        setOpen(false);
                      }}
                      className={`block px-3 py-2 text-sm hover:bg-slate-700 ${
                        isNew ? "bg-slate-700/50" : "text-slate-400"
                      }`}
                    >
                      {isNew && (
                        <span className="inline-block w-2 h-2 rounded-full bg-red-400 mr-2" />
                      )}
                      {n.text}
                    </Link>
                  </li>
                );
              })}
            </ul>
          )}
        </div>
      )}
    </div>
  );
}
