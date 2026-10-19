import { useEffect, useState } from "react";
import { api } from "../services/api";

export function useFetch<T>(caminho: string) {
  const [estado, setEstado] = useState<{
    dado: T | null;
    carregando: boolean;
    erro: Error | null;
  }>({ dado: null, carregando: true, erro: null });

  useEffect(() => {
    // vira true quando a tela sai do ar antes da resposta chegar
    let cancelado = false;

    api
      .get<T>(caminho)
      .then((dado) => {
        if (!cancelado) setEstado({ dado, carregando: false, erro: null });
      })
      .catch((erro: Error) => {
        if (!cancelado) setEstado({ dado: null, carregando: false, erro });
      });

    return () => {
      cancelado = true;
    };
  }, [caminho]);

  return estado;
}
