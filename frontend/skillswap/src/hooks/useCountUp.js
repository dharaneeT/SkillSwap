// hooks/useCountUp.js  (NEW FILE)  number that counts up to `target` (and between changes)
import { useEffect, useRef, useState } from "react";
import { prefersReducedMotion } from "../utils/motion";

export function useCountUp(target, { duration = 900, decimals = 0 } = {}) {
  const goal = Number(target) || 0;
  const reduced = prefersReducedMotion();
  const [value, setValue] = useState(0);
  const fromRef = useRef(0);

  useEffect(() => {
    if (reduced) return;
    const from = fromRef.current;
    const start = performance.now();
    let raf;
    const tick = (t) => {
      const p = Math.min(1, (t - start) / duration);
      const v = from + (goal - from) * (1 - Math.pow(1 - p, 3)); // ease-out
      fromRef.current = v;
      setValue(v);
      if (p < 1) raf = requestAnimationFrame(tick);
    };
    raf = requestAnimationFrame(tick);
    return () => cancelAnimationFrame(raf);
  }, [goal, duration, reduced]);

  const shown = reduced ? goal : value;
  return decimals ? shown.toFixed(decimals) : Math.round(shown);
}
