// utils/presence.js  (NEW FILE)
const startOfDay = (d) =>
  new Date(d.getFullYear(), d.getMonth(), d.getDate()).getTime();

export function formatLastSeen(iso, now = Date.now()) {
  const d = new Date(iso);
  const mins = Math.floor((now - d.getTime()) / 60000);
  if (mins < 1) return "Last seen just now";
  if (mins < 60) return `Last seen ${mins} min ago`;

  const time = d.toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" });
  const days = Math.round(
    (startOfDay(new Date(now)) - startOfDay(d)) / 86400000,
  );
  if (days <= 0) return `Last seen today at ${time}`;
  if (days === 1) return `Last seen yesterday at ${time}`;
  if (days < 7) {
    return `Last seen ${d.toLocaleDateString([], { weekday: "long" })} at ${time}`;
  }
  return `Last seen ${d.toLocaleDateString([], { day: "numeric", month: "short" })}`;
}

// p = { online, lastSeenAt } from /presence (or undefined while loading)
export function presenceText(p, now = Date.now()) {
  if (!p) return "";
  if (p.online) return "Online";
  if (!p.lastSeenAt) return "Offline"; // never recorded yet
  return formatLastSeen(p.lastSeenAt, now);
}
