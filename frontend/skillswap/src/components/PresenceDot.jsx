// components/PresenceDot.jsx  (NEW FILE)
// Put inside a `relative` wrapper around an avatar: it sits on the bottom-right corner.
export default function PresenceDot({ online }) {
  return (
    <span
      role="img"
      aria-label={online ? "Online" : "Offline"}
      className={`absolute bottom-0 right-0 h-2.5 w-2.5 rounded-full ring-2 ring-white ${
        online ? "bg-[#2fa66a]" : "bg-[#b9b7b0]"
      }`}
    />
  );
}
