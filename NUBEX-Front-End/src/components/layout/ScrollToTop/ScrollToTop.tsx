import { useState, useEffect } from "react";
import { useLocation } from "react-router-dom";
import styles from "./ScrollToTop.module.css";

export default function ScrollToTop() {
  const [visivel, setVisivel] = useState(false);
  const { pathname } = useLocation();

  // Reseta a rolagem para o topo sempre que mudar de rota
  useEffect(() => {
    window.scrollTo({ top: 0, behavior: "instant" });
  }, [pathname]);

  // Monitora a rolagem para mostrar/ocultar o botão
  useEffect(() => {
    function aoRolar() {
      setVisivel(window.scrollY > 300);
    }

    window.addEventListener("scroll", aoRolar, { passive: true });
    return () => window.removeEventListener("scroll", aoRolar);
  }, []);

  function rolarAoTopo() {
    window.scrollTo({ top: 0, behavior: "smooth" });
  }

  return (
    <button
      type="button"
      className={`${styles.botao} ${visivel ? styles.visivel : ""}`}
      onClick={rolarAoTopo}
      aria-label="Voltar ao topo da página"
      title="Voltar ao topo"
    >
      <svg
        className={styles.icone}
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        strokeWidth="2.5"
        strokeLinecap="round"
        strokeLinejoin="round"
        aria-hidden="true"
      >
        <polyline points="18 15 12 9 6 15" />
      </svg>
    </button>
  );
}
