import { useCallback, useEffect, useState } from "react";
import { api, ErroDaApi } from "../services/api";

export type TipoErroFetch =
  | "sem_internet"
  | "servico_indisponivel"
  | "nao_autenticado"
  | "sem_permissao"
  | "erro_inesperado";

export interface EstadoFetch<T> {
  dado: T | null;
  carregando: boolean;
  erro: Error | null;
  tipoErro: TipoErroFetch | null;
  recarregar: () => void;
}

export function identificarTipoErro(erro: unknown): TipoErroFetch {
  if (typeof navigator !== "undefined" && !navigator.onLine) {
    return "sem_internet";
  }

  if (erro instanceof ErroDaApi) {
    if (erro.status === 401) {
      return "nao_autenticado";
    }
    if (erro.status === 403) {
      return "sem_permissao";
    }
    if (erro.status >= 500) {
      return "servico_indisponivel";
    }
    return "erro_inesperado";
  }

  // Falha de rede no browser (serviço offline / porta fechada / timeout)
  if (erro instanceof TypeError) {
    if (typeof navigator !== "undefined" && !navigator.onLine) {
      return "sem_internet";
    }
    return "servico_indisponivel";
  }

  return "servico_indisponivel";
}

export function useFetch<T>(caminho: string): EstadoFetch<T> {
  const [estado, setEstado] = useState<{
    dado: T | null;
    carregando: boolean;
    erro: Error | null;
    tipoErro: TipoErroFetch | null;
  }>({ dado: null, carregando: true, erro: null, tipoErro: null });

  const [tentativa, setTentativa] = useState(0);

  const recarregar = useCallback(() => {
    setEstado((anterior) => ({
      ...anterior,
      carregando: true,
      erro: null,
      tipoErro: null,
    }));
    setTentativa((atual) => atual + 1);
  }, []);

  useEffect(() => {
    // vira true quando a tela sai do ar antes da resposta chegar
    let cancelado = false;

    const executarRequisicao = async () => {
      if (typeof navigator !== "undefined" && !navigator.onLine) {
        if (!cancelado) {
          setEstado({
            dado: null,
            carregando: false,
            erro: new Error("Sem conexão com a internet."),
            tipoErro: "sem_internet",
          });
        }
        return;
      }

      try {
        const dado = await api.get<T>(caminho);
        if (!cancelado) {
          setEstado({ dado, carregando: false, erro: null, tipoErro: null });
        }
      } catch (erro) {
        if (!cancelado) {
          setEstado({
            dado: null,
            carregando: false,
            erro: erro as Error,
            tipoErro: identificarTipoErro(erro),
          });
        }
      }
    };

    void executarRequisicao();

    return () => {
      cancelado = true;
    };
  }, [caminho, tentativa]);

  return {
    ...estado,
    recarregar,
  };
}
