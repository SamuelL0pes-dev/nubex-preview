import styles from "./HeroBanner.module.css";
import videoHero from "../../../assets/videos/hero.webm";
import TextoDigitado from "../../ui/TextoDigitado/TextoDigitado";

export default function HeroBanner() {
  return (
    <section className={styles.hero} aria-labelledby="hero-titulo">
      <video className={styles.video} src={videoHero} autoPlay muted loop playsInline aria-hidden="true" />

        <div className={styles.camada} />

      <div className={styles.conteudo} >
        <h1 id="hero-titulo"><TextoDigitado texto="Núcleo de Biologia Experimental" cursorFinal={false} /></h1>
        <p><TextoDigitado texto="Excelência em Pesquisa, Desenvolvimento e Inovação em Saúde." atraso={1400} /></p>
      </div>

    </section>
  );
}