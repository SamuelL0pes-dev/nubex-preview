import { useState, useEffect, useRef, useSyncExternalStore } from "react";
import { COUNTERS_DATA, type CounterItem } from "./AnimatedCounters.data";
import styles from "./AnimatedCounters.module.css";

const REDUCED_MOTION_QUERY = "(prefers-reduced-motion: reduce)";

function subscribeReducedMotion(onChange: () => void) {
  const media = window.matchMedia(REDUCED_MOTION_QUERY);
  media.addEventListener("change", onChange);
  return () => media.removeEventListener("change", onChange);
}

function getReducedMotion() {
  return window.matchMedia(REDUCED_MOTION_QUERY).matches;
}

function getServerReducedMotion() {
  return false;
}

function CounterItemView({
  item,
  isVisible,
}: {
  item: CounterItem;
  isVisible: boolean;
}) {
  const [count, setCount] = useState(0);
  const prefersReducedMotion = useSyncExternalStore(
    subscribeReducedMotion,
    getReducedMotion,
    getServerReducedMotion,
  );

  useEffect(() => {
    if (!isVisible) return;

    if (prefersReducedMotion) return;

    let startTime: number | null = null;
    let frameId: number;
    const duracaoMs = 2000;

    const animate = (currentTime: number) => {
      if (startTime === null) startTime = currentTime;
      const progress = Math.min((currentTime - startTime) / duracaoMs, 1);
      const easeOut = 1 - Math.pow(1 - progress, 3);

      setCount(Math.floor(easeOut * item.valorFinal));

      if (progress < 1) {
        frameId = requestAnimationFrame(animate);
      } else {
        setCount(item.valorFinal);
      }
    };

    frameId = requestAnimationFrame(animate);
    return () => cancelAnimationFrame(frameId);
  }, [isVisible, item.valorFinal, prefersReducedMotion]);

  return (
    <div className={styles.item}>
      <span className={styles.numero}>
        {item.prefixo || ""}
        {(isVisible && prefersReducedMotion ? item.valorFinal : count).toLocaleString("pt-BR")}
        {item.sufixo || ""}
      </span>
      <p className={styles.frase}>{item.frase}</p>
    </div>
  );
}

export default function AnimatedCounters() {
  const [hasAnimated, setHasAnimated] = useState(false);
  const sectionRef = useRef<HTMLElement>(null);

  useEffect(() => {
    const observer = new IntersectionObserver(
      ([entry]) => {
        if (entry.isIntersecting) {
          setHasAnimated(true);
          observer.disconnect();
        }
      },
      { threshold: 0.3 },
    );

    if (sectionRef.current) {
      observer.observe(sectionRef.current);
    }

    return () => observer.disconnect();
  }, []);

  return (
    <section
      ref={sectionRef}
      className={styles.secao}
      aria-label="Estatísticas do NUBEX"
    >
      <div className={styles.interno}>
        <div className={styles.cabecalho}>
          <span className={styles.selo}>O NUBEX em números</span>
          <h2 className={styles.tituloSecao}>Escala que sustenta a pesquisa</h2>
          <div className={styles.traco} />
        </div>

        <div className={styles.container}>
          {COUNTERS_DATA.map((item) => (
            <CounterItemView
              key={item.id}
              item={item}
              isVisible={hasAnimated}
            />
          ))}
        </div>
      </div>
    </section>
  );
}
