// components/dashboard/Ring.jsx  (NEW FILE)  concentric progress rings that animate in
import { useEffect, useState } from "react";
import { prefersReducedMotion } from "../../utils/motion";

const SIZE = 220;
const C = SIZE / 2;
const STROKE = 15;
const GAP = 7;

// rings = [{ key, label, value (0-100), color, track }], outermost first
// active = key of the ring to highlight (the rest fade)
export default function Ring({ rings, active, children }) {
  const [ready, setReady] = useState(prefersReducedMotion());
  useEffect(() => {
    const id = requestAnimationFrame(() => setReady(true));
    return () => cancelAnimationFrame(id);
  }, []);
  const still = prefersReducedMotion();

  return (
    <div className="relative mx-auto aspect-square w-full max-w-[16rem]">
      <svg
        viewBox={`0 0 ${SIZE} ${SIZE}`}
        role="img"
        aria-label={rings.map((r) => `${r.label} ${r.value}%`).join(", ")}
        className="h-full w-full"
      >
        {rings.map((r, i) => {
          const radius = C - STROKE / 2 - 2 - i * (STROKE + GAP);
          const len = 2 * Math.PI * radius;
          const dim = active && active !== r.key;
          return (
            <g
              key={r.key}
              transform={`rotate(-90 ${C} ${C})`}
              style={{ opacity: dim ? 0.25 : 1, transition: "opacity 0.2s" }}
            >
              <circle
                cx={C}
                cy={C}
                r={radius}
                fill="none"
                stroke={r.track}
                strokeWidth={STROKE}
              />
              <circle
                cx={C}
                cy={C}
                r={radius}
                fill="none"
                stroke={r.color}
                strokeWidth={STROKE}
                strokeLinecap="round"
                strokeOpacity={r.value > 0 ? 1 : 0}
                strokeDasharray={len}
                strokeDashoffset={len * (1 - (ready ? r.value : 0) / 100)}
                style={{
                  transition: still
                    ? "none"
                    : `stroke-dashoffset 900ms cubic-bezier(0.22, 1, 0.36, 1) ${i * 120}ms`,
                }}
              />
            </g>
          );
        })}
      </svg>
      <div className="pointer-events-none absolute inset-0 flex flex-col items-center justify-center text-center">
        {children}
      </div>
    </div>
  );
}
