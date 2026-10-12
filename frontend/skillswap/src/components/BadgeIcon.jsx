import Icon from "./Icon";

const SIZES = {
  sm: { cls: "medal-sm", icon: 15 },
  md: { cls: "", icon: 21 },
  lg: { cls: "medal-lg", icon: 30 },
};

export default function BadgeIcon({ badge, earned = true, size = "md" }) {
  const { cls, icon } = SIZES[size] ?? SIZES.md;
  const text = `${badge.label}${earned ? "" : " (locked)"}`;

  return (
    <span
      role="img"
      aria-label={text}
      title={`${text} - ${badge.description}`}
      className={`medal ${cls} medal-${badge.tone} ${earned ? "" : "medal-locked"}`}
    >
      <Icon
        name={badge.icon}
        size={icon}
        filled={Boolean(badge.filled)}
        strokeWidth={badge.filled ? 1.5 : 2}
      />
    </span>
  );
}
