// components/dashboard/Reveal.jsx  (NEW FILE)  fade + slide-up on mount, with a stagger delay
import { useEffect, useState } from "react";
import { prefersReducedMotion } from "../../utils/motion";

export default function Reveal({ delay = 0, className = "", children }) {
  const [shown, setShown] = useState(prefersReducedMotion());
  useEffect(() => {
    const id = requestAnimationFrame(() => setShown(true));
    return () => cancelAnimationFrame(id);
  }, []);
  return (
    <div
      className={`transition duration-500 ease-out ${
        shown ? "translate-y-0 opacity-100" : "translate-y-3 opacity-0"
      } ${className}`}
      style={{ transitionDelay: `${delay}ms` }}
    >
      {children}
    </div>
  );
}
