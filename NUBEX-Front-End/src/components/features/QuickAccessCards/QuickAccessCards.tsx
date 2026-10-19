import styles from "./QuickAccessCards.module.css";
import { QUICK_ACCESS_CARDS } from "./QuickAccessCards.data";
import QuickAccessCard from "./QuickAccessCard";

export default function QuickAccessCards() {
  return (
    <section className={styles.secao} aria-label="Destaques e Áreas de Atuação">
      <div className={styles.interno}>
        <div className={styles.cabecalho}>
          <span className={styles.selo}>Áreas de atuação</span>
          <h2 className={styles.titulo}>
            Uma estrutura científica <em>aberta</em> à comunidade
          </h2>
          <p className={styles.subtitulo}>
            Seis unidades estratégicas, laboratórios multiusuários e serviços
            técnicos num só lugar.
          </p>
          <div className={styles.traco} />
        </div>

        <div className={styles.grid}>
          {QUICK_ACCESS_CARDS.map((card) => (
            <QuickAccessCard key={card.id} card={card} />
          ))}
        </div>
      </div>
    </section>
  );
}
