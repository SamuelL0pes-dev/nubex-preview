import {useEffect, useState} from "react";
import styles from "./TextoDigitado.module.css";
const VELOCIDADE_MS = 40; 

type TextoDigitadoProps = {
  texto: string;
  atraso?: number;
  cursorFinal?: boolean;
};

export default function TextoDigitado({ texto, atraso = 0, cursorFinal = true }: TextoDigitadoProps) {
    const reduzirMovimento = window.matchMedia("(prefers-reduced-motion: reduce)").matches;
    const [letras, setLetras] = useState(reduzirMovimento ? texto.length: 0); // Se o usuário preferir reduzir movimento, mostra o texto completo de uma vez
    const terminou = letras >= texto.length;
    useEffect(() => {
        if (terminou) return;
        const tempo = letras === 0 ? atraso : VELOCIDADE_MS;
        const id = setTimeout(() => setLetras(letras + 1), tempo);
        return () => clearTimeout(id);
    }, [letras, texto.length, atraso]);
    return (
  <>
    <span className={styles.acessivel}>{texto}</span>
    <span aria-hidden="true">
      {texto.slice(0, letras)}
      {(!terminou || cursorFinal) && <span className={styles.cursor} />}
      <span className={styles.restante}>{texto.slice(letras)}</span>
    </span>
  </>
);
}
