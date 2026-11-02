import { useCallback, useEffect, useState } from "react";
import { identificarTipoErro, type TipoErroFetch } from "./useFetch";
import { buscarEditalAberto, listarNoticias } from "../services/conteudo";
import type { CartaoDoMural } from "../types/conteudo";

export type EstadoDoMural = {
  cartoes: CartaoDoMural[];
  carregando: boolean;
  erro: Error | null;
  tipoErro: TipoErroFetch | null;
  recarregar: () => void;
};

export function useMural(): EstadoDoMural {
  const [estado, setEstado] = useState<{
    cartoes: CartaoDoMural[];
    carregando: boolean;
    erro: Error | null;
    tipoErro: TipoErroFetch | null;
  }>({
    cartoes: [],
    carregando: true,
    erro: null,
    tipoErro: null,
  });

  const [tentativa, setTentativa] = useState(0);

  const recarregar = useCallback(() => {
    setEstado((anterior) => ({
      ...anterior,
      carregando: true,
      erro: null,
      tipoErro: null,
    }));
    setTentativa((t) => t + 1);
  }, []);

  useEffect(() => {
    let cancelado = false;

    const carregarMural = async () => {
      if (typeof navigator !== "undefined" && !navigator.onLine) {
        if (!cancelado) {
          setEstado({
            cartoes: [],
            carregando: false,
            erro: new Error("Sem conexão com a internet."),
            tipoErro: "sem_internet",
          });
        }
        return;
      }

      try {
        const [noticias, edital] = await Promise.all([
          listarNoticias(3),
          buscarEditalAberto(),
        ]);
        if (cancelado) return;

        const deNoticia = noticias.map((noticia): CartaoDoMural => ({
          ...noticia,
          categoria: "noticia",
        }));

        const cartoes: CartaoDoMural[] = edital
          ? [...deNoticia.slice(0, 2), { ...edital, categoria: "edital" }]
          : deNoticia.slice(0, 3);

        setEstado({ cartoes, carregando: false, erro: null, tipoErro: null });
      } catch (erro) {
        if (!cancelado) {
          setEstado({
            cartoes: [],
            carregando: false,
            erro: erro as Error,
            tipoErro: identificarTipoErro(erro),
          });
        }
      }
    };

    void carregarMural();

    return () => {
      cancelado = true;
    };
  }, [tentativa]);

  return {
    ...estado,
    recarregar,
  };
}
