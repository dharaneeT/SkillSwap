// components/studyroom/ChatPanel.jsx  (NEW FILE)
import { useEffect, useRef, useState } from "react";
import { deleteMessage } from "../../api/studyRoomApi";
import { getErrorMessage } from "../../api/axios";
import Icon from "../Icon";
import { formatTime } from "../../utils/studyRoom";

const MAX = 1000;

const STATUS_TEXT = {
  connecting: "Connecting...",
  open: "Connected",
  reconnecting: "Reconnecting...",
  closed: "Disconnected",
  kicked: "Removed",
  full: "Room full",
  ended: "Room closed",
};

export default function ChatPanel({ roomId, rt, myUserId, isHost }) {
  const {
    messages,
    status,
    hasMore,
    sendChat,
    retryChat,
    loadEarlier,
    lastError,
  } = rt;
  const [text, setText] = useState("");
  const [error, setError] = useState("");
  const listRef = useRef(null);
  const stickRef = useRef(true);

  // keep the newest message in view, unless the reader scrolled up on purpose
  useEffect(() => {
    const el = listRef.current;
    if (el && stickRef.current) el.scrollTop = el.scrollHeight;
  }, [messages]);

  const onScroll = (e) => {
    const el = e.currentTarget;
    stickRef.current = el.scrollHeight - el.scrollTop - el.clientHeight < 80;
  };

  const submit = (e) => {
    e.preventDefault();
    const value = text.trim();
    if (!value || value.length > MAX) return;
    stickRef.current = true;
    sendChat(value);
    setText("");
  };

  const onKeyDown = (e) => {
    if (e.key === "Enter" && !e.shiftKey) submit(e);
  };

  const remove = async (m) => {
    setError("");
    try {
      await deleteMessage(roomId, m.id);
    } catch (err) {
      setError(getErrorMessage(err));
    }
  };

  const online = status === "open";

  return (
    <section
      aria-label="Room chat"
      className="card card-white flex h-[70vh] min-h-96 flex-col gap-3 lg:h-136"
    >
      <div className="flex items-center justify-between">
        <h2 className="section-title">Chat</h2>
        <span
          className={`badge ${online ? "badge-success" : "badge-danger"}`}
          role="status"
        >
          <span className="live-dot" /> {STATUS_TEXT[status] ?? status}
        </span>
      </div>

      <div
        ref={listRef}
        onScroll={onScroll}
        role="log"
        aria-live="polite"
        aria-label="Messages"
        className="flex-1 space-y-2 overflow-y-auto rounded-2xl bg-cream p-3"
      >
        {hasMore && (
          <button
            type="button"
            className="btn btn-ghost btn-sm mx-auto flex"
            onClick={loadEarlier}
          >
            Load earlier messages
          </button>
        )}
        {messages.length === 0 && (
          <p className="meta py-6 text-center">No messages yet. Say hello!</p>
        )}
        {messages.map((m) => {
          const mine = m.senderId === myUserId;
          return (
            <div
              key={m.id ?? m.clientId}
              className={`flex flex-col ${mine ? "items-end" : "items-start"}`}
            >
              <div
                className={`max-w-[85%] rounded-2xl px-3 py-2 text-sm ${mine ? "bg-brand text-white" : "bg-surface text-ink"} ${m.status === "sending" ? "opacity-70" : ""}`}
              >
                {!mine && (
                  <p className="text-xs font-semibold text-muted">
                    {m.senderName}
                  </p>
                )}
                <p className="whitespace-pre-wrap wrap-break-word">
                  {m.content}
                </p>
              </div>
              <p className="meta mt-0.5 flex items-center gap-2 text-xs">
                {m.status === "sending" && "Sending..."}
                {m.status === "failed" && (
                  <>
                    <span className="text-red-700">
                      Not delivered{m.error ? `: ${m.error}` : ""}
                    </span>
                    <button
                      type="button"
                      className="link"
                      onClick={() => retryChat(m.clientId, m.content)}
                    >
                      Retry
                    </button>
                  </>
                )}
                {!m.status && formatTime(m.createdAt)}
                {isHost && m.id != null && !mine && (
                  <button
                    type="button"
                    className="link"
                    onClick={() => remove(m)}
                    aria-label={`Remove message from ${m.senderName}`}
                  >
                    <Icon name="trash-2" size={12} />
                  </button>
                )}
              </p>
            </div>
          );
        })}
      </div>

      {(error || lastError) && (
        <p role="alert" className="alert alert-error py-2">
          {error || lastError}
        </p>
      )}

      <form onSubmit={submit} className="flex items-end gap-2">
        <label className="flex-1">
          <span className="sr-only">Message</span>
          <textarea
            className="textarea"
            rows={1}
            maxLength={MAX}
            value={text}
            onChange={(e) => setText(e.target.value)}
            onKeyDown={onKeyDown}
            placeholder={
              online
                ? "Write a message (Enter to send)"
                : "Waiting for connection..."
            }
          />
        </label>
        <button
          type="submit"
          className="btn btn-primary"
          disabled={!text.trim()}
          aria-label="Send message"
        >
          <Icon name="send" size={16} />
        </button>
      </form>
      {text.length > MAX - 100 && (
        <p className="meta text-right">
          {text.length} / {MAX}
        </p>
      )}
    </section>
  );
}
