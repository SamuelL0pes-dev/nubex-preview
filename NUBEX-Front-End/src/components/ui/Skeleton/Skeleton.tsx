import type { CSSProperties, HTMLAttributes } from "react";
import styles from "./Skeleton.module.css";

export interface SkeletonProps extends HTMLAttributes<HTMLDivElement> {
  variante?: "linha" | "linhaCurta" | "bloco" | "circulo";
  largura?: string | number;
  altura?: string | number;
}

export function Skeleton({
  variante = "linha",
  largura,
  altura,
  className = "",
  style,
  ...resto
}: SkeletonProps) {
  const estiloCustomizado: CSSProperties = {
    ...style,
    ...(largura
      ? { width: typeof largura === "number" ? `${largura}px` : largura }
      : {}),
    ...(altura
      ? { height: typeof altura === "number" ? `${altura}px` : altura }
      : {}),
  };

  const classeVariante = styles[variante] || styles.linha;

  return (
    <div
      className={`${classeVariante} ${className}`.trim()}
      style={estiloCustomizado}
      aria-hidden="true"
      {...resto}
    />
  );
}

export function SkeletonCartao({ className = "" }: { className?: string }) {
  return (
    <div className={`${styles.cartao} ${className}`.trim()} aria-hidden="true">
      <Skeleton variante="bloco" altura={160} />
      <Skeleton variante="linha" altura={20} />
      <Skeleton variante="linhaCurta" altura={14} />
    </div>
  );
}

export function SkeletonGrade({
  quantidade = 3,
  className = "",
}: {
  quantidade?: number;
  className?: string;
}) {
  return (
    <div
      className={`${styles.gradePadrao} ${className}`.trim()}
      aria-hidden="true"
    >
      {Array.from({ length: quantidade }).map((_, index) => (
        <SkeletonCartao key={index} />
      ))}
    </div>
  );
}
