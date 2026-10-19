import type { CSSProperties } from "react";
import styles from "./Partners.module.css";
import { PARCEIROS, type Parceiro } from "./Partners.data";

interface PartnersProps {
  onSelecionar?: (id: string) => void;
}

function Anel({
  lista,
  classe,
  onSelecionar,
}: {
  lista: Parceiro[];
  classe: string;
  onSelecionar?: (id: string) => void;
}) {
  return (
    <div className={`${styles.anel} ${classe}`}>
      {lista.map((p, indice) => (
        <div
          key={p.id}
          className={styles.orbe}
          style={{ "--a": `${(360 / lista.length) * indice}deg` } as CSSProperties}
        >
          <div className={styles.braco}>
            <div className={styles.contra}>
              <button
                type="button"
                className={styles.parceiro}
                aria-label={`Ver a parceria com ${p.nome}`}
                onClick={() => onSelecionar?.(p.id)}
              >
                <img src={p.logo} alt="" />
              </button>
              <span className={styles.nomeParceiro}>{p.sigla}</span>
            </div>
          </div>
        </div>
      ))}
    </div>
  );
}

export default function Partners({ onSelecionar }: PartnersProps) {
  const externos = PARCEIROS.filter((p) => p.anel === "externo");
  const internos = PARCEIROS.filter((p) => p.anel === "interno");

  return (
    <section className={styles.secao} aria-labelledby="titulo-parceiros">
      <div className={styles.faixa}>
        <div className={styles.interno}>
          <div className={styles.cabecalho}>
            <span className={styles.selo}>Nossos parceiros</span>
            <h2 id="titulo-parceiros" className={styles.tituloSecao}>
              Quem constrói <em>ciência</em> junto com o NUBEX
            </h2>
            <p className={styles.subtitulo}>
              Em volta do nosso símbolo estão as <b>instituições parceiras</b>: no anel de fora as{" "}
              <b>brasileiras</b>, no de dentro as <b>internacionais</b>. <b>Passe o mouse</b> para
              ver o nome de cada uma e <b>clique</b> para conhecer a parceria.
            </p>
            <div className={styles.traco}></div>
          </div>
        </div>
      </div>

      <div className={styles.corpo}>
        <div className={styles.palco}>
          <span className={styles.campo}></span>
          <span className={`${styles.aro} ${styles.aro1}`}></span>
          <span className={`${styles.aro} ${styles.aro2}`}></span>
          <span className={`${styles.aro} ${styles.aro3}`}></span>
          <span className={styles.nucleo}></span>

          <div className={`${styles.trilha} ${styles.trilhaExterna}`}></div>
          <div className={`${styles.trilha} ${styles.trilhaInterna}`}></div>

          <Anel lista={externos} classe={styles.anelExterno} onSelecionar={onSelecionar} />
          <Anel lista={internos} classe={styles.anelInterno} onSelecionar={onSelecionar} />

          <span className={styles.pulso}></span>
          <span className={`${styles.pulso} ${styles.pulsoB}`}></span>

          <button
            type="button"
            className={styles.centro}
            aria-label="Ver todos os parceiros do NUBEX"
            onClick={() => onSelecionar?.("todos")}
          >
            <img src="/icone-nubex-512.png" alt="" />
          </button>
        </div>

        <p className={styles.rodape}>
          A rotação pausa quando o mouse entra ·{" "}
          <button type="button" onClick={() => onSelecionar?.("todos")}>
            Ver a lista completa de parceiros
          </button>
        </p>
      </div>
    </section>
  );
}