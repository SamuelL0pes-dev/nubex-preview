import { useState } from "react";
import type { QuickAccessCardItem } from "./QuickAccessCards.data";
import styles from "./QuickAccessCards.module.css";

interface QuickAccessCardProps {
  card: QuickAccessCardItem;
}

export default function QuickAccessCard({ card }: QuickAccessCardProps) {
  const [isFlipped, setIsFlipped] = useState(false);

  const toggleFlip = () => {
    setIsFlipped((prev) => !prev);
  };

  return (
    <div
      className={styles.card}
      onClick={toggleFlip}
      role="button"
      tabIndex={0}
      aria-label={`Card: ${card.tituloFrente}. Clique para ver mais detalhes.`}
      onKeyDown={(e) => {
        if (e.key === "Enter" || e.key === " ") {
          e.preventDefault();
          toggleFlip();
        }
      }}
    >
      <div className={`${styles.cardInner} ${isFlipped ? styles.flipped : ""}`}>
        <div className={styles.cardFront}>
          <span className={styles.icone} aria-hidden="true">
            {card.icone}
          </span>
          <h3 className={styles.titulo}>{card.tituloFrente}</h3>
          <p className={styles.resumo}>{card.resumoFrente}</p>
        </div>

        <div className={styles.cardBack}>
          <h3 className={styles.titulo}>{card.tituloVerso}</h3>
          <p className={styles.textoCompleto}>{card.textoVerso}</p>

          <a
            href={card.linkUrl}
            className={styles.botao}
            onClick={(e) => {
              e.stopPropagation();
            }}
          >
            {card.linkTexto} →
          </a>
        </div>
      </div>
    </div>
  );
}
