// components/dashboard/Sparkline.jsx  (NEW FILE)  smooth line chart that draws itself; hover/touch for values
import { useEffect, useState } from "react";
import { prefersReducedMotion } from "../../utils/motion";

const W = 400;
const H = 120;
const PAD = 14;

// data = [{ label, value }]
export default function Sparkline({
  data,
  color = "#101010",
  unit = "sessions",
}) {
  const [hover, setHover] = useState(null);
  const [ready, setReady] = useState(prefersReducedMotion());
  useEffect(() => {
    const id = requestAnimationFrame(() => setReady(true));
    return () => cancelAnimationFrame(id);
  }, []);

  const max = Math.max(1, ...data.map((d) => d.value));
  const step = (W - 2 * PAD) / Math.max(1, data.length - 1);
  const pts = data.map((d, i) => ({
    x: PAD + i * step,
    y: H - PAD - (d.value / max) * (H - 2 * PAD),
  }));
  // smooth S-curves between points (horizontal tangents: never overshoots the data)
  const line = pts
    .map((p, i) => {
      if (i === 0) return `M${p.x},${p.y}`;
      const mx = (pts[i - 1].x + p.x) / 2;
      return `C${mx},${pts[i - 1].y} ${mx},${p.y} ${p.x},${p.y}`;
    })
    .join(" ");
  const area = `${line} L${pts[pts.length - 1].x},${H} L${pts[0].x},${H} Z`;

  const onMove = (e) => {
    const r = e.currentTarget.getBoundingClientRect();
    const x = ((e.clientX - r.left) / r.width) * W;
    let best = 0;
    pts.forEach((p, i) => {
      if (Math.abs(p.x - x) < Math.abs(pts[best].x - x)) best = i;
    });
    setHover(best);
  };

  const h = hover === null ? null : { ...data[hover], ...pts[hover] };

  return (
    <div className="relative">
      <svg
        viewBox={`0 0 ${W} ${H}`}
        aria-hidden="true"
        className="block h-auto w-full touch-pan-y"
        onPointerMove={onMove}
        onPointerDown={onMove}
        onPointerLeave={() => setHover(null)}
      >
        <line
          x1={PAD}
          x2={W - PAD}
          y1={H - PAD}
          y2={H - PAD}
          stroke={color}
          strokeOpacity="0.15"
        />
        <path
          d={area}
          fill={color}
          fillOpacity="0.1"
          style={{ opacity: ready ? 1 : 0, transition: "opacity 1s 0.4s" }}
        />
        <path
          d={line}
          pathLength="1"
          fill="none"
          stroke={color}
          strokeWidth="3"
          strokeLinecap="round"
          strokeLinejoin="round"
          strokeDasharray="1"
          strokeDashoffset={ready ? 0 : 1}
          style={{
            transition: prefersReducedMotion()
              ? "none"
              : "stroke-dashoffset 1.1s ease-out",
          }}
        />
        {pts.map((p, i) => (
          <circle
            key={i}
            cx={p.x}
            cy={p.y}
            r={hover === i ? 6 : 3.5}
            fill={color}
            style={{ transition: "r 0.15s" }}
          />
        ))}
        {h && (
          <line
            x1={h.x}
            x2={h.x}
            y1={PAD}
            y2={H - PAD}
            stroke={color}
            strokeOpacity="0.3"
            strokeDasharray="3 4"
          />
        )}
      </svg>

      {h && (
        <div
          className="pointer-events-none absolute top-0 -translate-x-1/2 rounded-xl bg-ink px-2.5 py-1 text-xs font-semibold text-white shadow-lg"
          style={{ left: `${Math.min(88, Math.max(12, (h.x / W) * 100))}%` }}
        >
          {h.value} {unit}
          <span className="block text-[10px] font-normal opacity-70">
            week of {h.label}
          </span>
        </div>
      )}

      <div className="mt-1 flex justify-between text-[11px] opacity-60">
        <span>{data[0]?.label}</span>
        <span>This week</span>
      </div>

      {/* screen readers get the numbers instead of the picture */}
      <ul className="sr-only">
        {data.map((d) => (
          <li key={d.start ?? d.label}>
            Week of {d.label}: {d.value} {unit}
          </li>
        ))}
      </ul>
    </div>
  );
}
