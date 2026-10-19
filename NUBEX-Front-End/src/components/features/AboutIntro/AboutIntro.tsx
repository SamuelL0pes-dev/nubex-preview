import { useState, useEffect, useRef } from "react";
import styles from "./AboutIntro.module.css";
import { FOTOS_ESTRUTURA, type FotosEstrutura } from "./AboutIntro.data";

export default function AboutIntro() {
  const [foto, setFoto] = useState<FotosEstrutura | null>(null);
  const [isVisible, setIsVisible] = useState(false);
  const sectionRef = useRef<HTMLElement>(null);

  useEffect(() => {
    const observer = new IntersectionObserver(
      ([entry]) => {
        if (entry.isIntersecting) {
          setIsVisible(true);
          observer.disconnect(); 
        }
      },
      {
        rootMargin: "0px 0px -250px 0px",
        threshold: 0.25,
      },
    );

    if (sectionRef.current) {
      observer.observe(sectionRef.current);
    }

    return () => observer.disconnect();
  }, []);

  return (
    <section
      ref={sectionRef}
      className={`${styles.secao} ${isVisible ? styles.visivel : ""}`}
      id="sobre"
      aria-label="Sobre o NUBEX"
    >
      <div className={styles.container}>
        <div className={styles.logoCol}>
          <div
            className={`${styles.logoMoldura} ${foto ? styles.comFoto : ""}`}
          >
            <img
              src="/LogoNubexHorizontalUnifor.png"
              alt="Logo oficial do NUBEX e Universidade de Fortaleza (Unifor)"
              className={styles.logoImg}
            />
            {foto && (
              <img
                key={foto.id}
                src={foto.src}
                alt=""
                className={styles.fotoCamada}
              />
            )}

            <div className={styles.legenda}>{foto?.titulo}</div>
          </div>
          <div className={styles.miniaturas}>
            {FOTOS_ESTRUTURA.map((f) => (
              <button
                key={f.id}
                type="button"
                className={styles.mini}
                aria-label={`Ver ${f.titulo}`}
                onMouseEnter={() => setFoto(f)}
                onFocus={() => setFoto(f)}
                onMouseLeave={() => setFoto(null)}
                onBlur={() => setFoto(null)}
              >
                <img src={f.src} alt="" />
              </button>
            ))}
          </div>
        </div>

        <div className={styles.textoCol}>
          <span className={styles.badge}>Sobre o NUBEX</span>
          <h2 className={styles.titulo}>
            Polo de Excelência em Pesquisa, Desenvolvimento e Inovação em Saúde
          </h2>
          <div className={styles.linha} />

          <p className={styles.paragrafo}>
            O <strong>Núcleo de Biologia Experimental (NUBEX)</strong> da
            Universidade de Fortaleza (Unifor) é uma plataforma científica
            multiusuária de alta complexidade tecnológica, estruturada para
            integrar saúde humana, pesquisa de ponta e desenvolvimento de
            biofármacos e tecnologias diagnósticas.
          </p>

          <p className={styles.paragrafo}>
            Com infraestrutura comparável aos grandes centros mundiais —
            incluindo biotério SPF, laboratórios de biossegurança NB3 e
            plataformas analíticas de espectrometria e genômica —, o NUBEX atua
            como polo catalisador entre a academia, programas de pós-graduação e
            o setor produtivo.
          </p>

          <div className={styles.acoes}>
            <a href="/quem-somos" className={styles.linkDestaque}>
              Conheça nossa trajetória completa →
            </a>
          </div>
        </div>
      </div>
    </section>
  );
}
