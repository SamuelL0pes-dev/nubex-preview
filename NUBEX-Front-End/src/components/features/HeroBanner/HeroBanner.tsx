import { useRef, useState } from "react";
import styles from "./HeroBanner.module.css";
import videoHero from "../../../assets/videos/hero.webm";
import TextoDigitado from "../../ui/TextoDigitado/TextoDigitado";

const PREFERE_MENOS_MOVIMENTO = window.matchMedia(
  "(prefers-reduced-motion: reduce)",
).matches;

export default function HeroBanner() {
  const videoRef = useRef<HTMLVideoElement>(null);
  const [pausado, setPausado] = useState(PREFERE_MENOS_MOVIMENTO);

  function alternarVideo() {
    const video = videoRef.current;
    if (!video) return;

    if (video.paused) {
      video.play().catch(() => setPausado(true));
      setPausado(false);
    } else {
      video.pause();
      setPausado(true);
    }
  }
  return (
    <section className={styles.hero} aria-labelledby="hero-titulo">
      <video
        ref={videoRef}
        className={styles.video}
        src={videoHero}
        autoPlay={!PREFERE_MENOS_MOVIMENTO}
        muted
        loop
        playsInline
        aria-hidden="true"
      />

      <div className={styles.camada} />

      <div className={styles.conteudo}>
        <h1 id="hero-titulo">
          <TextoDigitado
            texto="Núcleo de Biologia Experimental"
            cursorFinal={false}
          />
        </h1>
        <p>
          <TextoDigitado
            texto="Excelência em Pesquisa, Desenvolvimento e Inovação em Saúde."
            atraso={1400}
          />
        </p>
      </div>

      <button
        type="button"
        className={styles.controleVideo}
        onClick={alternarVideo}
        aria-label={
          pausado ? "Retomar o vídeo de fundo" : "Pausar o vídeo de fundo"
        }
      >
        {pausado ? (
          <svg viewBox="0 0 24 24" aria-hidden="true">
            <path d="M8 5v14l11-7z" />
          </svg>
        ) : (
          <svg viewBox="0 0 24 24" aria-hidden="true">
            <path d="M7 5h3v14H7zM14 5h3v14h-3z" />
          </svg>
        )}
      </button>
    </section>
  );
}
