import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import styles from "./FAQ.module.css";
import { CATEGORIAS_FAQ } from "./FAQ.data";

const PALAVRAS_IGNORADAS = new Set([
  "como",
  "para",
  "que",
  "com",
  "uma",
  "umas",
  "uns",
  "dos",
  "das",
  "nos",
  "nas",
  "por",
  "meu",
  "minha",
  "qual",
  "quais",
  "sao",
  "ser",
  "tem",
]);

function normalizar(texto: string) {
  return texto
    .normalize("NFD")
    .replace(/\p{Diacritic}/gu, "")
    .toLowerCase();
}

function raizesDaBusca(busca: string) {
  const termo = normalizar(busca.trim());
  if (!termo) return [];

  const palavras = termo
    .split(/\s+/)
    .filter((p) => p.length >= 3 && !PALAVRAS_IGNORADAS.has(p));

  if (palavras.length === 0) return [termo];
  return palavras.map((p) => (p.length > 5 ? p.slice(0, -2) : p));
}

function doisDigitos(numero: number) {
  return String(numero).padStart(2, "0");
}

export default function FAQ() {
  const [busca, setBusca] = useState("");
  const [abertaId, setAbertaId] = useState<string | null>(null);
  const [temaAtual, setTemaAtual] = useState(CATEGORIAS_FAQ[0].id);

  const temBusca = busca.trim() !== "";

  const categoriasComBusca = useMemo(() => {
    const raizes = raizesDaBusca(busca);

    return CATEGORIAS_FAQ.map((categoria) => ({
      ...categoria,
      perguntas: categoria.perguntas
        .map((p) => {
          const texto = normalizar(`${p.pergunta} ${p.resposta}`);
          const pontos = raizes.filter((r) => texto.includes(r)).length;
          return { ...p, pontos };
        })
        .filter((p) => raizes.length === 0 || p.pontos > 0)
        .sort((a, b) => b.pontos - a.pontos),
    }));
  }, [busca]);

  const secoesVisiveis = categoriasComBusca.filter(
    (categoria) => categoria.perguntas.length > 0,
  );

  const totalEncontrado = secoesVisiveis.reduce(
    (soma, categoria) => soma + categoria.perguntas.length,
    0,
  );

  const idsVisiveis = secoesVisiveis.map((categoria) => categoria.id).join(",");

  useEffect(() => {
    if (typeof IntersectionObserver === "undefined" || !idsVisiveis) return;

    const observer = new IntersectionObserver(
      (entries) => {
        const visivel = entries.find((entry) => entry.isIntersecting);
        if (visivel) setTemaAtual(visivel.target.id.replace("tema-", ""));
      },
      { rootMargin: "-120px 0px -60% 0px" },
    );

    idsVisiveis.split(",").forEach((id) => {
      const secao = document.getElementById(`tema-${id}`);
      if (secao) observer.observe(secao);
    });

    return () => observer.disconnect();
  }, [idsVisiveis]);

  function alternar(id: string) {
    setAbertaId((atual) => (atual === id ? null : id));
  }

  function irParaTema(id: string) {
    setTemaAtual(id);
    const reduzirMovimento = window.matchMedia(
      "(prefers-reduced-motion: reduce)",
    ).matches;
    document.getElementById(`tema-${id}`)?.scrollIntoView({
      behavior: reduzirMovimento ? "auto" : "smooth",
      block: "start",
    });
  }

  return (
    <div className={styles.pagina}>
      <section className={styles.hero} aria-labelledby="faq-titulo">
        <div className={styles.heroConteudo}>
          <span className={styles.eyebrow}>Perguntas frequentes</span>
          <h1 id="faq-titulo" className={styles.titulo}>
            Tudo o que você precisa saber para{" "}
            <span className={styles.tituloDestaque}>pesquisar no NUBEX</span>
          </h1>
          <p className={styles.descricao}>
            Orientações sobre acesso, agendamento de serviços, biossegurança e
            parcerias. Cada laboratório pode ter regras específicas.
          </p>

          <form
            role="search"
            className={styles.busca}
            onSubmit={(e) => e.preventDefault()}
          >
            <svg
              className={styles.buscaIcone}
              viewBox="0 0 24 24"
              aria-hidden="true"
            >
              <circle cx="11" cy="11" r="7" />
              <path d="m20 20-3.5-3.5" />
            </svg>
            <label htmlFor="faq-busca" className={styles.rotuloOculto}>
              Buscar dúvida
            </label>
            <input
              id="faq-busca"
              type="search"
              className={styles.buscaCampo}
              placeholder="Digite sua dúvida, ex.: como agendar um equipamento"
              value={busca}
              onChange={(e) => setBusca(e.target.value)}
              autoComplete="off"
            />
            {busca && (
              <button
                type="button"
                className={styles.buscaLimpar}
                aria-label="Limpar busca"
                onClick={() => setBusca("")}
              >
                ×
              </button>
            )}
          </form>
        </div>
      </section>

      <div className={styles.corpo}>
        <aside className={styles.lateral}>
          <span className={styles.eyebrow}>Central de ajuda</span>
          <h2 className={styles.lateralTitulo}>Ficou com alguma dúvida?</h2>
          <p className={styles.lateralTexto}>
            Escolha um tema para ir direto a ele ou pesquise acima. Se não
            encontrar o que procura, nossa equipe técnica responde.
          </p>

          <nav className={styles.temas} aria-label="Temas das perguntas">
            {categoriasComBusca.map((categoria) => {
              const ativo = temaAtual === categoria.id;

              return (
                <button
                  key={categoria.id}
                  type="button"
                  className={`${styles.tema} ${ativo ? styles.temaAtivo : ""}`}
                  aria-current={ativo ? "true" : undefined}
                  disabled={categoria.perguntas.length === 0}
                  onClick={() => irParaTema(categoria.id)}
                >
                  <span>{categoria.titulo}</span>
                  <span className={styles.temaTotal}>
                    {doisDigitos(categoria.perguntas.length)}
                  </span>
                </button>
              );
            })}
          </nav>

          <Link to="/contato" className={styles.botao}>
            Fale com a equipe →
          </Link>
        </aside>

        <div className={styles.conteudo}>
          {temBusca && (
            <p className={styles.resultado} role="status">
              {totalEncontrado === 1
                ? "1 pergunta encontrada"
                : `${totalEncontrado} perguntas encontradas`}{" "}
              para "{busca.trim()}"
            </p>
          )}

          {secoesVisiveis.length === 0 ? (
            <div className={styles.vazio}>
              <p>Nenhuma pergunta encontrada.</p>
              <button
                type="button"
                className={styles.vazioBotao}
                onClick={() => setBusca("")}
              >
                Ver todas as perguntas
              </button>
            </div>
          ) : (
            secoesVisiveis.map((categoria, indiceSecao) => {
              const inicio =
                secoesVisiveis
                  .slice(0, indiceSecao)
                  .reduce(
                    (soma, anterior) => soma + anterior.perguntas.length,
                    0,
                  ) + 1;

              return (
                <section
                  key={categoria.id}
                  id={`tema-${categoria.id}`}
                  className={styles.secao}
                  aria-labelledby={`tema-${categoria.id}-titulo`}
                >
                  <h2
                    id={`tema-${categoria.id}-titulo`}
                    className={styles.secaoTitulo}
                  >
                    {categoria.titulo}
                  </h2>

                  <ol className={styles.lista} start={inicio}>
                    {categoria.perguntas.map((p, index) => {
                      const aberta = abertaId === p.id;

                      return (
                        <li
                          key={p.id}
                          className={`${styles.item} ${aberta ? styles.itemAberto : ""}`}
                        >
                          <h3 className={styles.perguntaTitulo}>
                            <button
                              type="button"
                              id={`pergunta-${p.id}`}
                              className={styles.pergunta}
                              aria-expanded={aberta}
                              aria-controls={`resposta-${p.id}`}
                              onClick={() => alternar(p.id)}
                            >
                              <span className={styles.numero}>
                                {doisDigitos(inicio + index)}
                              </span>
                              <span>{p.pergunta}</span>
                              <span
                                className={styles.icone}
                                aria-hidden="true"
                              />
                            </button>
                          </h3>

                          <div
                            id={`resposta-${p.id}`}
                            role="region"
                            aria-labelledby={`pergunta-${p.id}`}
                            className={styles.resposta}
                            inert={!aberta}
                          >
                            <div className={styles.respostaConteudo}>
                              <p>{p.resposta}</p>
                            </div>
                          </div>
                        </li>
                      );
                    })}
                  </ol>
                </section>
              );
            })
          )}
        </div>
      </div>
    </div>
  );
}