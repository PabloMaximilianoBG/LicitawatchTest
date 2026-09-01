const colorFor = (score) => (score >= 75 ? "#1fae5a" : score >= 50 ? "#e0a020" : "#94a3b8");

export default function ScoreRing({ score = 0, size = 52 }) {
  const r = size / 2 - 4;
  const c = 2 * Math.PI * r;
  const off = c * (1 - score / 100);
  const color = colorFor(score);

  return (
    <div className="relative flex-shrink-0" style={{ width: size, height: size }}>
      <svg width={size} height={size} className="-rotate-90">
        <circle cx={size / 2} cy={size / 2} r={r} fill="none" stroke="#eef2f8" strokeWidth={4} />
        <circle
          cx={size / 2}
          cy={size / 2}
          r={r}
          fill="none"
          stroke={color}
          strokeWidth={4}
          strokeLinecap="round"
          strokeDasharray={c}
          strokeDashoffset={off}
        />
      </svg>
      <div
        className="absolute inset-0 grid place-items-center font-display font-semibold"
        style={{ color, fontSize: size * 0.29 }}
      >
        {score}
      </div>
    </div>
  );
}
