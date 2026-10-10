import { useEffect, useMemo, useRef, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { fetchMe, fetchUserById } from "../api/userApi";
import { fetchConversation, fetchPartners, sendMessage } from "../api/chatApi";
import { getErrorMessage } from "../api/axios";
import { useFetch } from "../hooks/useFetch";
import { markChatRead } from "../api/notificationApi";
import { useChatSocket } from "../hooks/useChatSocket";

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
    <main className="max-w-4xl mx-auto px-4 pt-6 flex gap-4 h-[calc(100vh-90px)]">
      <aside className="w-1/3 rounded-xl bg-slate-800 border border-slate-700 overflow-y-auto">
        <div className="p-3 border-b border-slate-700 flex items-center justify-between">
          <h2 className="font-semibold">Chats</h2>
          <span
            className={`text-xs ${status === "open" ? "text-green-400" : "text-amber-300"}`}
          >
            {status === "open" ? "● Live" : "● Reconnecting…"}
          </span>
        </div>
        {sidebar.length === 0 && (
          <p className="p-3 text-sm text-slate-400">
            No chats yet. Use the Chat button on a match.
          </p>
        )}
        {sidebar.map((p) => (
          <button
            key={p.id}
            onClick={() => openChat(p.id)}
            className={`flex w-full items-center justify-between text-left px-3 py-2 cursor-pointer hover:bg-slate-700 ${
              p.id === activeId ? "bg-slate-700" : ""
            }`}
          >
            <span>{p.name}</span>
            {unread[p.id] > 0 && (
              <span className="rounded-full bg-red-500 text-[10px] font-bold px-2 py-0.5">
                {unread[p.id]}
              </span>
            )}
          </button>
        ))}
      </aside>

      <section className="flex-1 flex flex-col rounded-xl bg-slate-800 border border-slate-700">
        {!activeId ? (
          <p className="m-auto text-slate-400">Select a conversation</p>
        ) : (
          <>
            <h2 className="p-3 font-semibold border-b border-slate-700">
              {active?.name ?? "Chat"}
            </h2>
            {(error || historyError) && (
              <p className="m-3 rounded-lg bg-red-900/50 border border-red-500 px-3 py-2 text-sm">
                {error || historyError}
              </p>
            )}

            <ul className="flex-1 overflow-y-auto p-3 space-y-2">
              {messages.map((m) => {
                const mine = m.senderId === me?.id;
                return (
                  <li
                    key={m.id}
                    className={`flex ${mine ? "justify-end" : "justify-start"}`}
                  >
                    <div
                      className={`max-w-[75%] rounded-2xl px-3 py-2 text-sm ${
                        mine ? "bg-red-200 text-slate-900" : "bg-slate-700"
                      }`}
                    >
                      <p className="wrap-break-word">{m.content}</p>
                      <p className="text-[10px] opacity-60 mt-1">
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
              className="p-3 flex gap-2 border-t border-slate-700"
            >
              <input
                className="flex-1 rounded-lg bg-slate-900 border border-slate-600 px-3 py-2 outline-none focus:border-red-200"
                placeholder="Type a message..."
                value={text}
                maxLength={1000}
                onChange={(e) => setText(e.target.value)}
              />
              <button className="rounded-lg bg-red-200 text-slate-900 font-semibold px-4 cursor-pointer">
                Send
              </button>
            </form>
          </>
        )}
      </section>
    </main>
  );
}
