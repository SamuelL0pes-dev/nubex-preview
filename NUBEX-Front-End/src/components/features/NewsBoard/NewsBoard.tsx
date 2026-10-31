import { Link } from "react-router-dom";
import { useAoEntrarNaTela } from "../../../hooks/useAoEntrarNaTela";
import { useMural } from "../../../hooks/useMural";
import { Helice, Seta } from "./NewsBoard.icones";
import NewsCard from "./NewsCard";
import estilos from "./NewsBoard.module.css";

const ATRASO_ENTRE_CARTOES = 210;

function Esqueleto() {
  return (
    <div className={estilos.esqueleto}>
      <div className={estilos.esqueletoCapa} />
      <div className={estilos.esqueletoLinha} />
      <div className={estilos.esqueletoLinhaCurta} />
    </div>
  );
}

export default function NewsBoard() {
  const { cartoes, carregando, erro } = useMural();
  const { alvo, visivel } = useAoEntrarNaTela<HTMLDivElement>();

  function corpo() {
    if (erro) {
      return (
        <p className={estilos.aviso} role="status">
          Não foi possível carregar as notícias agora. Tente novamente em instantes.
        </p>
      );
    }

    if (carregando) {
      return (
        <div className={estilos.grade} aria-hidden="true">
          <Esqueleto />
          <Esqueleto />
          <Esqueleto />
        </div>
      );
    }

    if (cartoes.length === 0) {
      return <p className={estilos.aviso}>Nenhuma notícia publicada ainda.</p>;
    }

    return (
      <div className={estilos.grade} ref={alvo}>
        {cartoes.map((item, indice) => (
          <NewsCard
            key={`${item.categoria}-${item.id}`}
            item={item}
            entrando={visivel}
            atraso={indice * ATRASO_ENTRE_CARTOES}
          />
        ))}
      </div>
    );
  }

  return (
    <section className={estilos.secao} aria-labelledby="mural-titulo">
      <div className={estilos.transicao} aria-hidden="true">
        <Helice />
      </div>

      <div className={estilos.corpoDaSecao}>
        <div className={estilos.interno}>
          <div className={estilos.topo}>
            <div className={estilos.cabeca}>
              <p className={estilos.selo}>Acontece no Núcleo</p>
              <h2 id="mural-titulo" className={estilos.titulo}>
                Notícias e editais
              </h2>
            </div>

            <p className={estilos.subtitulo}>
              O que acontece no Núcleo e as oportunidades abertas para pesquisadores
              e estudantes.
            </p>

            <Link className={estilos.verTodos} to="/noticias">
              Mais notícias <Seta />
            </Link>
          </div>

          {corpo()}
        </div>
      </div>
    </section>
  );
}