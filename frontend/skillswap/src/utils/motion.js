// utils/motion.js  (NEW FILE)
// true when the user asked their OS for less animation: every animation below checks this
export const prefersReducedMotion = () =>
  typeof window !== "undefined" &&
  window.matchMedia?.("(prefers-reduced-motion: reduce)").matches === true;
