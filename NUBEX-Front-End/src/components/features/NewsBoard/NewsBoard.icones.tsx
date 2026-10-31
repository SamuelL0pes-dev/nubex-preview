const tracos = {
  fill: "none",
  stroke: "currentColor",
  strokeWidth: 2,
  strokeLinecap: "round",
  strokeLinejoin: "round",
} as const;

export function IconeDeNoticia() {
  return (
    <svg viewBox="0 0 48 48" aria-hidden="true" {...tracos}>
      <rect x="5" y="10" width="38" height="28" rx="3" />
      <path d="M12 18 h12 M12 25 h12 M12 31 h8 M30 18 h7 M30 25 h7" />
    </svg>
  );
}

export function IconeDeEdital() {
  return (
    <svg viewBox="0 0 48 48" aria-hidden="true" {...tracos}>
      <path d="M12 5 h18 l8 8 v30 H12 Z" />
      <path d="M30 5 v8 h8" />
      <path d="M18 24 h14 M18 31 h14 M18 17 h7" />
    </svg>
  );
}

export function Seta() {
  return (
    <svg viewBox="0 0 24 24" aria-hidden="true" {...tracos} strokeWidth={2.4}>
      <path d="M5 12h13" />
      <path d="m13 6 6 6-6 6" />
    </svg>
  );
}

export function Helice() {
  const fitaDeBaixo =
    "M0,42 C100,42 100,88 200,88 S300,42 400,42 S500,88 600,88 " +
    "S700,42 800,42 S900,88 1000,88 S1100,42 1200,42";

  const fitaDeCima =
    "M0,88 C100,88 100,42 200,42 S300,88 400,88 S500,42 600,42 " +
    "S700,88 800,88 S900,42 1000,42 S1100,88 1200,88";

  return (
    <svg
      viewBox="0 0 1200 118"
      preserveAspectRatio="none"
      aria-hidden="true"
      focusable="false"
    >
      <defs>
        <linearGradient id="nubex-fio" x1="0" y1="0" x2="1" y2="0">
          <stop offset="0" stopColor="#004af7" />
          <stop offset="1" stopColor="#00b37a" />
        </linearGradient>
      </defs>

      <path
        d={`${fitaDeBaixo} L1200,118 L0,118 Z`}
        style={{ fill: "var(--faixa-corte, #f7f9fe)" }}
      />
      <path
        d={fitaDeBaixo}
        style={{ fill: "none", stroke: "url(#nubex-fio)", strokeWidth: 2.5 }}
        vectorEffect="non-scaling-stroke"
        strokeLinecap="round"
      />
      <path
        d={fitaDeCima}
        style={{
          fill: "none",
          stroke: "var(--cor-verde, #00b37a)",
          strokeWidth: 2.5,
          opacity: 0.75,
        }}
        vectorEffect="non-scaling-stroke"
        strokeLinecap="round"
      />
    </svg>
  );
}
