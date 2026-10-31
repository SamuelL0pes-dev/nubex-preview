import { useCallback, useEffect, useRef, useState } from "react";

export function useAoEntrarNaTela<T extends HTMLElement>(fracaoVisivel = 0.12) {
  const [visivel, setVisivel] = useState(false);
  const observador = useRef<IntersectionObserver | null>(null);

  const alvo = useCallback(
    (elemento: T | null) => {
      observador.current?.disconnect();

      if (!elemento || visivel) return;

      if (typeof IntersectionObserver === "undefined") {
        setVisivel(true);
        return;
      }

      observador.current = new IntersectionObserver(
        ([entrada]) => {
          if (!entrada.isIntersecting) return;
          setVisivel(true);
          observador.current?.disconnect();
        },
        { threshold: fracaoVisivel },
      );

      observador.current.observe(elemento);
    },
    [fracaoVisivel, visivel],
  );

  useEffect(() => () => observador.current?.disconnect(), []);

  return { alvo, visivel };
}
