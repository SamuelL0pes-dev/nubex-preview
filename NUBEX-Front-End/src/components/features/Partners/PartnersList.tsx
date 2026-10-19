import { useState } from "react";
import styles from "./PartnersList.module.css";
import { PARCEIROS } from "./Partners.data";

const FILTROS = ["Todos", "Nacionais", "Internacionais"] as const;
type Filtro = (typeof FILTROS)[number];

interface PartnersListProps {
  destacado?: string;
}

export default function PartnersList({ destacado }: PartnersListProps) {
  const [filtro, setFiltro] = useState<Filtro>("Todos");

  const visiveis =
    filtro === "Todos"
      ? PARCEIROS
      : PARCEIROS.filter((p) =>
          filtro === "Nacionais" ? p.escopo === "Nacional" : p.escopo === "Internacional",
        );

  return (
    <section className={styles.secao} aria-labelledby="titulo-lista-parceiros">
      <div className={styles.interno}>
        <div className={styles.cabecalho}>
          <span className={styles.selo}>Rede de parceiros</span>
          <h1 id="titulo-lista-parceiros" className={styles.titulo}>
            As instituições que constroem <em>ciência</em> com o NUBEX
          </h1>
          <p className={styles.subtitulo}>
            {PARCEIROS.length} instituições, entre universidades brasileiras, centros de pesquisa
            e parceiros internacionais.
          </p>
        </div>

        <div className={styles.filtros}>
          {FILTROS.map((f) => (
            <button
              key={f}
              type="button"
              className={`${styles.filtro} ${f === filtro ? styles.filtroAtivo : ""}`}
              aria-pressed={f === filtro}
              onClick={() => setFiltro(f)}
            >
              {f}
            </button>
          ))}
        </div>

        <div className={styles.grade}>
          {visiveis.map((p) => (
            <article
              key={p.id}
              id={`parceiro-${p.id}`}
              className={`${styles.item} ${
                destacado === `parceiro-${p.id}` ? styles.itemDestacado : ""
              }`}
            >
              <img src={p.logo} alt="" />
              <div>
                <span className={styles.escopo}>{p.escopo}</span>
                <p className={styles.nome}>{p.nome}</p>
                <p className={styles.descricao}>{p.descricao}</p>
              </div>
            </article>
          ))}
        </div>

        {visiveis.length === 0 && <p className={styles.vazio}>Nenhum parceiro neste filtro.</p>}
      </div>
    </section>
  );
}