import { useEffect, useState } from "react";
import { buscarEditalAberto, listarNoticias } from "../services/conteudo";
import type { CartaoDoMural } from "../types/conteudo";

type EstadoDoMural = {
  cartoes: CartaoDoMural[];
  carregando: boolean;
  erro: Error | null;
};

export function useMural(): EstadoDoMural {
  const [estado, setEstado] = useState<EstadoDoMural>({
    cartoes: [],
    carregando: true,
    erro: null,
  });

  useEffect(() => {
    let cancelado = false;

    Promise.all([listarNoticias(3), buscarEditalAberto()])
      .then(([noticias, edital]) => {
        if (cancelado) return;

        const deNoticia = noticias.map(
          (noticia): CartaoDoMural => ({ ...noticia, categoria: "noticia" }),
        );

        const cartoes: CartaoDoMural[] = edital
          ? [...deNoticia.slice(0, 2), { ...edital, categoria: "edital" }]
          : deNoticia.slice(0, 3);

        setEstado({ cartoes, carregando: false, erro: null });
      })
      .catch((erro: Error) => {
        if (!cancelado) setEstado({ cartoes: [], carregando: false, erro });
      });

    return () => {
      cancelado = true;
    };
  }, []);

  return estado;
}