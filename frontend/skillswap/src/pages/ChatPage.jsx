import { useEffect, useMemo, useRef, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { fetchMe, fetchUserById } from "../api/userApi";
import { fetchConversation, fetchPartners, sendMessage } from "../api/chatApi";
import { getErrorMessage } from "../api/axios";
import { useFetch } from "../hooks/useFetch";
import { markChatRead } from "../api/notificationApi";
import { useChatSocket } from "../hooks/useChatSocket";
import Icon from "../components/Icon";

export default function ChatPage() {
  const { userId } = useParams(); // undefined on /chat
  const navigate = useNavigate();
  const activeId = userId ? Number(userId) : null;

  const { data: me } = useFetch(fetchMe);
  const { data: partnersData, refetch: refetchPartners } =
    useFetch(fetchPartners);
  const partners = partnersData ?? [];
  const known = partners.some((p) => p.id === activeId);

  // /chat/:id for someone we've never messaged → look up their name
  const { data: urlUser } = useFetch(fetchUserById, activeId, {
    enabled: Boolean(activeId) && partnersData !== null && !known,
  });
  const {
    data: history,
    error: historyError,
    refetch: refetchHistory,
  } = useFetch(fetchConversation, activeId, { enabled: Boolean(activeId) });

  const [live, setLive] = useState([]); // messages pushed over the socket
  const [unread, setUnread] = useState({}); // partnerId -> count
  const [text, setText] = useState("");
  const [error, setError] = useState("");
  const bottomRef = useRef(null);

  const { status, send } = useChatSocket((msg) => {
    if (msg.error) {
      setError(msg.error);
      return;
    }
    setLive((prev) => [...prev, msg]);

    const fromMe = msg.senderId === me?.id;
    const partnerId = fromMe ? msg.receiverId : msg.senderId;
    if (!fromMe && partnerId === activeId)
      markChatRead(partnerId).catch(() => {});
    if (!partners.some((p) => p.id === partnerId)) refetchPartners(); // brand-new contact
    if (!fromMe && partnerId !== activeId) {
      setUnread((u) => ({ ...u, [partnerId]: (u[partnerId] ?? 0) + 1 }));
    }
  });

  // history + live, de-duplicated by id, only the open conversation
  const messages = useMemo(() => {
    const byId = new Map();
    for (const m of [...(history ?? []), ...live]) {
      if (m.senderId === activeId || m.receiverId === activeId)
        byId.set(m.id, m);
    }
    return [...byId.values()].sort((a, b) => a.id - b.id);
  }, [history, live, activeId]);

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages]);

  // opening a chat clears that person's bell notification
  useEffect(() => {
    if (activeId) markChatRead(activeId).catch(() => {});
  }, [activeId]);

  const pinned =
    !known && urlUser?.id === activeId
      ? [{ id: urlUser.id, name: urlUser.name }]
      : [];
  const sidebar = [...pinned, ...partners];
  const active = sidebar.find((p) => p.id === activeId);

  const openChat = (id) => {
    setUnread((u) => ({ ...u, [id]: 0 }));
    setError("");
    navigate(`/chat/${id}`);
  };

  const handleSend = async (e) => {
    e.preventDefault();
    const content = text.trim();
    if (!content || !activeId) return;
    setError("");

    // normal path: the saved message echoes back through the socket
    if (send({ receiverId: activeId, content })) {
      setText("");
      return;
    }
    // socket is down → fall back to REST (the other person sees it on their next load)
    try {
      await sendMessage({ receiverId: activeId, content });
      setText("");
      refetchHistory();
    } catch (err) {
      setError(getErrorMessage(err));
    }
  };

  return (
    <main className="mx-auto flex h-[calc(100dvh-5.5rem)] w-full max-w-5xl flex-col gap-4 px-3 pb-4 pt-2 sm:px-6 md:flex-row">
      <aside className="flex max-h-48 shrink-0 flex-col overflow-hidden rounded-[1.75rem] bg-surface md:max-h-none md:w-1/3">
        <div className="flex items-center justify-between px-5 py-4">
          <h2 className="flex items-center gap-2 font-bold">
            <Icon name="message-circle" size={18} />
            Chats
          </h2>
          <span
            className={`inline-flex items-center gap-1.5 rounded-full px-2.5 py-1 text-xs font-semibold ${
              status === "open"
                ? "bg-[#d9f2e3] text-[#17623a]"
                : "bg-[#fff4dd] text-[#7a5200]"
            }`}
          >
            <span className="live-dot" />
            {status === "open" ? "Live" : "Reconnecting…"}
          </span>
        </div>
        <div className="flex-1 overflow-y-auto px-2 pb-2">
          {sidebar.length === 0 && (
            <p className="px-3 py-2 text-sm text-muted">
              No chats yet. Use the Chat button on a match.
            </p>
          )}
          {sidebar.map((p) => (
            <button
              key={p.id}
              onClick={() => openChat(p.id)}
              className={`flex w-full items-center justify-between gap-3 rounded-full px-3 py-2 text-left text-sm font-medium transition-colors ${
                p.id === activeId
                  ? "bg-periwinkle text-navy"
                  : "hover:bg-white"
              }`}
            >
              <span className="flex min-w-0 items-center gap-2.5">
                <span className="flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-lavender text-xs font-bold uppercase">
                  {p.name?.[0] ?? <Icon name="user" size={14} />}
                </span>
                <span className="truncate">{p.name}</span>
              </span>
              {unread[p.id] > 0 && (
                <span className="rounded-full bg-brand px-2 py-0.5 text-[10px] font-bold text-white">
                  {unread[p.id]}
                </span>
              )}
            </button>
          ))}
        </div>
      </aside>

      <section className="card card-white flex min-h-0 flex-1 flex-col overflow-hidden p-0 sm:p-0">
        {!activeId ? (
          <div className="m-auto flex flex-col items-center gap-3 p-6 text-center text-muted">
            <span className="flex h-14 w-14 items-center justify-center rounded-full bg-lavender text-ink">
              <Icon name="message-circle" size={24} />
            </span>
            <p>Select a conversation</p>
          </div>
        ) : (
          <>
            <h2 className="flex items-center gap-3 border-b border-line px-5 py-4 font-bold">
              <span className="flex h-9 w-9 items-center justify-center rounded-full bg-lavender text-sm font-bold uppercase">
                {active?.name?.[0] ?? <Icon name="user" size={16} />}
              </span>
              {active?.name ?? "Chat"}
            </h2>
            {(error || historyError) && (
              <p role="alert" className="alert alert-error m-3">
                <Icon name="alert-circle" size={18} className="mt-0.5" />
                <span>{error || historyError}</span>
              </p>
            )}

            <ul className="flex-1 space-y-2 overflow-y-auto bg-cream p-4">
              {messages.map((m) => {
                const mine = m.senderId === me?.id;
                return (
                  <li
                    key={m.id}
                    className={`flex ${mine ? "justify-end" : "justify-start"}`}
                  >
                    <div
                      className={`max-w-[80%] rounded-3xl px-4 py-2.5 text-sm ${
                        mine
                          ? "rounded-br-lg bg-brand text-white"
                          : "rounded-bl-lg bg-surface text-ink"
                      }`}
                    >
                      <p className="wrap-break-word">{m.content}</p>
                      <p className="mt-1 text-[10px] opacity-70">
                        {new Date(m.sentAt).toLocaleTimeString([], {
                          hour: "2-digit",
                          minute: "2-digit",
                        })}
                      </p>
                    </div>
                  </li>
                );
              })}
              <li ref={bottomRef} />
            </ul>

            <form
              onSubmit={handleSend}
              className="flex gap-2 border-t border-line p-3"
            >
              <input
                className="input"
                placeholder="Type a message..."
                aria-label="Message"
                value={text}
                maxLength={1000}
                onChange={(e) => setText(e.target.value)}
              />
              <button className="btn btn-primary shrink-0">
                <Icon name="send" size={15} />
                <span className="max-sm:sr-only">Send</span>
              </button>
            </form>
          </>
        )}
      </section>
    </main>
  );
}
