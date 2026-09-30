import { useEffect, useRef, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { fetchMe, fetchUserById } from "../api/userApi";
import { fetchConversation, fetchPartners, sendMessage } from "../api/chatApi";
import { getErrorMessage } from "../api/axios";

export default function ChatPage() {
  const { userId } = useParams(); // undefined on /chat
  const navigate = useNavigate();
  const activeId = userId ? Number(userId) : null;

  const [me, setMe] = useState(null);
  const [partners, setPartners] = useState([]);
  const [messages, setMessages] = useState([]);
  const [text, setText] = useState("");
  const [error, setError] = useState("");
  const bottomRef = useRef(null);

  // 1) who am I + who have I talked to (+ the person in the URL if it's a new chat)
  useEffect(() => {
    let ignore = false;
    (async () => {
      try {
        const [meRes, pRes] = await Promise.all([fetchMe(), fetchPartners()]);
        let list = pRes.data.data;
        if (activeId && !list.some((p) => p.id === activeId)) {
          const u = (await fetchUserById(activeId)).data.data;
          list = [{ id: u.id, name: u.name }, ...list];
        }
        if (!ignore) {
          setMe(meRes.data.data);
          setPartners(list);
        }
      } catch (e) {
        if (!ignore) setError(getErrorMessage(e));
      }
    })();
    return () => {
      ignore = true;
    };
  }, [activeId]);

  // 2) load the open conversation and poll every 3s
  useEffect(() => {
    if (!activeId) return;
    let ignore = false;
    const load = () =>
      fetchConversation(activeId)
        .then((res) => !ignore && setMessages(res.data.data))
        .catch((e) => !ignore && setError(getErrorMessage(e)));
    load();
    const timer = setInterval(load, 3000);
    return () => {
      ignore = true;
      clearInterval(timer);
    };
  }, [activeId]);

  // 3) keep the newest message in view
  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages]);

  const openChat = (id) => {
    setMessages([]); // don't flash the previous conversation
    navigate(`/chat/${id}`);
  };

  const handleSend = async (e) => {
    e.preventDefault();
    const content = text.trim();
    if (!content || !activeId) return;
    setText("");
    setError("");
    try {
      await sendMessage({ receiverId: activeId, content });
      const res = await fetchConversation(activeId);
      setMessages(res.data.data);
    } catch (err) {
      setError(getErrorMessage(err));
      setText(content); // give the text back
    }
  };

  const active = partners.find((p) => p.id === activeId);

  return (
    <main className="max-w-4xl mx-auto px-4 pt-6 flex gap-4 h-[calc(100vh-90px)]">
      <aside className="w-1/3 rounded-xl bg-slate-800 border border-slate-700 overflow-y-auto">
        <h2 className="p-3 font-semibold border-b border-slate-700">Chats</h2>
        {partners.length === 0 && (
          <p className="p-3 text-sm text-slate-400">
            No chats yet. Use the Chat button on a match in your dashboard.
          </p>
        )}
        {partners.map((p) => (
          <button
            key={p.id}
            onClick={() => openChat(p.id)}
            className={`block w-full text-left px-3 py-2 cursor-pointer hover:bg-slate-700 ${
              p.id === activeId ? "bg-slate-700" : ""
            }`}
          >
            {p.name}
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
            {error && (
              <p className="m-3 rounded-lg bg-red-900/50 border border-red-500 px-3 py-2 text-sm">
                {error}
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
