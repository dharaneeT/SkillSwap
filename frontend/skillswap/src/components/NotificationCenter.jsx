import { useEffect, useRef, useState } from "react";
import { Link } from "react-router-dom";
import {
  fetchNotifications,
  markAllNotificationsRead,
  markNotificationRead,
} from "../api/notificationApi";
import { useFetch } from "../hooks/useFetch";
import { usePolling } from "../hooks/usePolling";
import Icon from "./Icon";

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
        title="Notifications"
        className="btn-icon relative"
      >
        <Icon name="bell" size={19} />
        {unreadCount > 0 && (
          <span className="absolute -right-1 -top-1 flex h-4 min-w-4 items-center justify-center rounded-full bg-brand px-1 text-[10px] font-bold text-white">
            {unreadCount > 9 ? "9+" : unreadCount}
          </span>
        )}
      </button>

      {open && (
        <div className="z-40 max-h-96 overflow-y-auto rounded-3xl border border-line bg-white shadow-lg max-sm:fixed max-sm:inset-x-3 max-sm:top-20 sm:absolute sm:right-0 sm:mt-2 sm:w-80">
          <div className="flex items-center justify-between border-b border-line px-4 py-3">
            <h3 className="flex items-center gap-2 text-sm font-bold">
              <Icon name="bell" size={15} />
              Notifications
            </h3>
            {unreadCount > 0 && (
              <button
                onClick={markAllRead}
                className="flex items-center gap-1 text-xs font-semibold text-brand hover:text-brand-dark"
              >
                <Icon name="check" size={13} />
                Mark all read
              </button>
            )}
          </div>

          {items.length === 0 ? (
            <p className="flex items-center gap-2 p-4 text-sm text-muted">
              <Icon name="circle-check" size={16} />
              You're all caught up.
            </p>
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
                    className={`block px-4 py-3 text-sm hover:bg-surface ${
                      n.seen ? "text-muted" : "bg-periwinkle/40 text-ink"
                    }`}
                  >
                    {!n.seen && (
                      <span className="mr-2 inline-block h-2 w-2 rounded-full bg-brand" />
                    )}
                    {n.message}
                    <span className="mt-0.5 block text-xs text-muted">
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
