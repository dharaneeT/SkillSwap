// components/roadmap/ProgressBar.jsx  (NEW FILE)
export default function ProgressBar({ value = 0, label = "Progress" }) {
  const v = Math.max(0, Math.min(100, Number(value) || 0));
  return (
    <div
      role="progressbar"
      aria-label={label}
      aria-valuenow={v}
      aria-valuemin={0}
      aria-valuemax={100}
      className="h-2.5 w-full overflow-hidden rounded-full bg-white"
    >
      <div
        className="h-full rounded-full bg-brand transition-all duration-300"
        style={{ width: `${v}%` }}
      />
    </div>
  );
}
