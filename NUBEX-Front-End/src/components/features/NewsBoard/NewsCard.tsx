import { Link } from "react-router-dom";
import { dataCurta, diaEMes, diasAte } from "../../../utils/datas";
import type { CartaoDoMural } from "../../../types/conteudo";
import { IconeDeEdital, IconeDeNoticia, Seta } from "./NewsBoard.icones";
import estilos from "./NewsCard.module.css";

const NOME_DA_CATEGORIA = {
  noticia: "Notícia",
  edital: "Edital",
} as const;

/** A sete dias ou menos o selo passa a contar o tempo que resta. */
function textoDoPrazo(encerramento: string): string {
  const dias = diasAte(encerramento);

  if (dias <= 0) return "Último dia";
  if (dias <= 7) return `Encerra em ${dias} ${dias === 1 ? "dia" : "dias"}`;

  return `Aberto até ${diaEMes(encerramento)}`;
}

type Props = {
  item: CartaoDoMural;
  /** true quando a grade ja apareceu na tela */
  entrando?: boolean;
  /** atraso em milissegundos, para um cartao entrar depois do outro */
  atraso?: number;
};

export default function NewsCard({ item, entrando = true, atraso = 0 }: Props) {
  const destino =
    item.categoria === "edital"
      ? `/editais/${item.id}`
      : `/noticias/${item.id}`;

  return (
    <article
      className={`${estilos.cartao} ${entrando ? estilos.entrando : estilos.aguardando}`}
      data-categoria={item.categoria}
      style={{ animationDelay: `${atraso}ms` }}
    >
      <Link
        className={`${estilos.capa} ${item.imagemUrl ? "" : estilos.semFoto}`}
        to={destino}
        tabIndex={-1}
        aria-hidden="true"
      >
        {item.imagemUrl ? (
          <img src={item.imagemUrl} alt="" />
        ) : item.categoria === "edital" ? (
          <IconeDeEdital />
        ) : (
          <IconeDeNoticia />
        )}
        <span className={estilos.etiqueta}>
          {NOME_DA_CATEGORIA[item.categoria]}
        </span>
      </Link>

      <div className={estilos.corpo}>
        <div className={estilos.topo}>
          <span className={estilos.data}>{dataCurta(item.dataPublicacao)}</span>

          {item.categoria === "edital" && (
            <span
              className={estilos.prazo}
              data-urgente={diasAte(item.dataEncerramento) <= 7 ? "sim" : "nao"}
            >
              {textoDoPrazo(item.dataEncerramento)}
            </span>
          )}
        </div>

        {item.unidade && <p className={estilos.unidade}>{item.unidade}</p>}

        <h3 className={estilos.titulo}>
          <Link to={destino}>{item.titulo}</Link>
        </h3>

        {item.resumo && <p className={estilos.resumo}>{item.resumo}</p>}
      </div>

      <Link className={estilos.leia} to={destino}>
        Leia <Seta />
      </Link>
    </article>
  );
}
