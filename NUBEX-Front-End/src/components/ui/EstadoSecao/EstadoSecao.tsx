import { type ReactNode } from "react";
import { identificarTipoErro } from "../../../hooks/useFetch";
import { SkeletonGrade } from "../Skeleton";
import styles from "./EstadoSecao.module.css";
import type { EstadoSecaoProps } from "./EstadoSecao.types";

function IconeOffline() {
  return (
    <svg
      className={styles.icone}
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="2"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
    >
      <line x1="1" y1="1" x2="23" y2="23" />
      <path d="M16.72 11.06A10.94 10.94 0 0 1 19 12.55" />
      <path d="M5 12.55a10.94 10.94 0 0 1 5.17-2.39" />
      <path d="M10.71 5.05A16 16 0 0 1 22.58 9" />
      <path d="M1.42 9a15.91 15.91 0 0 1 4.7-2.88" />
      <path d="M8.53 16.11a6 6 0 0 1 6.95 0" />
      <line x1="12" y1="20" x2="12.01" y2="20" />
    </svg>
  );
}

function IconeServico() {
  return (
    <svg
      className={styles.icone}
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="2"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
    >
      <rect x="2" y="2" width="20" height="8" rx="2" ry="2" />
      <rect x="2" y="14" width="20" height="8" rx="2" ry="2" />
      <line x1="6" y1="6" x2="6.01" y2="6" />
      <line x1="6" y1="18" x2="6.01" y2="18" />
      <path d="M18 6v.01" />
      <path d="M18 18v.01" />
    </svg>
  );
}

function IconeCadeado() {
  return (
    <svg
      className={styles.icone}
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="2"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
    >
      <rect x="3" y="11" width="18" height="11" rx="2" ry="2" />
      <path d="M7 11V7a5 5 0 0 1 10 0v4" />
    </svg>
  );
}

function IconeRecarregar() {
  return (
    <svg
      className={styles.iconeBotao}
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="2"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
    >
      <path d="M21.5 2v6h-6" />
      <path d="M21.34 15.57a10 10 0 1 1-.57-8.38l5.67-5.19" />
    </svg>
  );
}

export function EstadoSecao<T = unknown>({
  carregando,
  erro,
  tipoErro,
  dado,
  nomeSecao,
  onTentarNovamente,
  esqueleto,
  mensagemErro,
  mensagemOffline,
  compacto = false,
  className = "",
  children,
}: EstadoSecaoProps<T>) {
  // Estado 1: Carregando
  if (carregando) {
    return (
      <div
        className={`${styles.containerSecao} ${className}`.trim()}
        aria-busy="true"
        aria-live="polite"
      >
        <span className={styles.srOnly}>
          Carregando informações {nomeSecao ? `de ${nomeSecao}` : "da página"}
          ...
        </span>
        {esqueleto ?? <SkeletonGrade />}
      </div>
    );
  }

  // Estado 2: Erro (Falha parcial isolada)
  if (erro) {
    const tipo = tipoErro ?? identificarTipoErro(erro);
    const estaOffline = tipo === "sem_internet";
    const ehSeguranca = tipo === "nao_autenticado" || tipo === "sem_permissao";

    let titulo = "Serviço temporariamente indisponível";
    let descricaoPadrao = nomeSecao
      ? `Não foi possível carregar ${nomeSecao}. O restante do portal continua funcionando normalmente.`
      : "Não foi possível carregar estas informações no momento. O restante da página continua ativo.";

    if (estaOffline) {
      titulo = "Sem conexão com a internet";
      descricaoPadrao =
        mensagemOffline ??
        "Você parece estar offline. Verifique sua conexão com a rede e tente novamente.";
    } else if (tipo === "nao_autenticado") {
      titulo = "Acesso não autenticado";
      descricaoPadrao =
        erro instanceof Error &&
        erro.message &&
        !erro.message.startsWith("O servidor respondeu")
          ? erro.message
          : "Faça login para ter acesso a este conteúdo.";
    } else if (tipo === "sem_permissao") {
      titulo = "Sem permissão de acesso";
      descricaoPadrao =
        erro instanceof Error &&
        erro.message &&
        !erro.message.startsWith("O servidor respondeu")
          ? erro.message
          : "Você não tem permissão para acessar este recurso.";
    }

    const descricao =
      tipo === "sem_internet"
        ? (mensagemOffline ?? descricaoPadrao)
        : (mensagemErro ?? descricaoPadrao);

    return (
      <div
        className={`${compacto ? styles.painelErroCompacto : styles.painelErro} ${className}`.trim()}
        role="alert"
        aria-live="polite"
      >
        <div
          className={
            estaOffline ? styles.iconeWrapperOffline : styles.iconeWrapper
          }
        >
          {estaOffline ? (
            <IconeOffline />
          ) : ehSeguranca ? (
            <IconeCadeado />
          ) : (
            <IconeServico />
          )}
        </div>

        <div className={styles.conteudoErro}>
          <h3 className={styles.tituloErro}>{titulo}</h3>
          <p className={styles.descricaoErro}>{descricao}</p>
        </div>

        {onTentarNovamente && (
          <button
            type="button"
            className={styles.botaoTentarNovamente}
            onClick={onTentarNovamente}
            aria-label={
              nomeSecao
                ? `Tentar carregar ${nomeSecao} novamente`
                : "Tentar carregar este conteúdo novamente"
            }
          >
            <IconeRecarregar />
            <span>Tentar novamente</span>
          </button>
        )}
      </div>
    );
  }

  // Estado 3: Pronto
  const conteudoRenderizado: ReactNode =
    typeof children === "function"
      ? (children as (dado: T) => ReactNode)(dado as T)
      : children;

  return (
    <div className={`${styles.containerSecao} ${className}`.trim()}>
      {conteudoRenderizado}
    </div>
  );
}
