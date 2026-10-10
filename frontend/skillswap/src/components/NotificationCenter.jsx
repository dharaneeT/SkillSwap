import { useEffect, useRef, useState } from "react";
import { Link } from "react-router-dom";
import {
  fetchNotifications,
  markAllNotificationsRead,
  markNotificationRead,
} from "../api/notificationApi";
import { useFetch } from "../hooks/useFetch";
import { usePolling } from "../hooks/usePolling";

const POLL_MS = 15_000;

const linkFor = (n) => {
  if (n.type === "REVIEW_RECEIVED") return "/profile";
  if (n.type === "CHAT_MESSAGE") return `/chat/${n.fromUserId}`;
  return "/dashboard";
};

function timeAgo(iso) {
  const mins = Math.floor((Date.now() - new Date(iso).getTime()) / 60_000);
  if (mins < 1) return "just now";
  if (mins < 60) return `${mins}m ago`;
  const hrs = Math.floor(mins / 60);
  if (hrs < 24) return `${hrs}h ago`;
  return `${Math.floor(hrs / 24)}d ago`;
}

export default function NotificationCenter() {
  const { data, refetch } = useFetch(fetchNotifications);
  const items = data?.items ?? [];
  const unreadCount = data?.unreadCount ?? 0;

  const [open, setOpen] = useState(false);
  const rootRef = useRef(null);

  usePolling(refetch, POLL_MS);

  const markRead = async (id) => {
    try {
      await markNotificationRead(id);
    } finally {
      refetch();
    }
  };
  const markAllRead = async () => {
    try {
      await markAllNotificationsRead();
    } finally {
      refetch();
    }
  };

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
        onClick={() => {
          setOpen((o) => !o);
          if (!open) refetch();
        }}
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
              {items.map((n) => (
                <li key={n.id}>
                  <Link
                    to={linkFor(n)}
                    onClick={() => {
                      if (!n.seen) markRead(n.id);
                      setOpen(false);
                    }}
                    className={`block px-3 py-2 text-sm hover:bg-slate-700 ${
                      n.seen ? "text-slate-400" : "bg-slate-700/50"
                    }`}
                  >
                    {!n.seen && (
                      <span className="inline-block w-2 h-2 rounded-full bg-red-400 mr-2" />
                    )}
                    {n.message}
                    <span className="block text-xs text-slate-500 mt-0.5">
                      {timeAgo(n.createdAt)}
                    </span>
                  </Link>
                </li>
              ))}
            </ul>
          )}
        </div>
      )}
    </div>
  );
}
