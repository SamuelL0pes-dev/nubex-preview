import { Link } from "react-router-dom";
import styles from "./Footer.module.css";

export default function Footer() {
  return (
    <footer className={styles.footer}>
      <div className={styles.container}>
        <div className={styles.grid}>
          {/* Coluna 1: Apresentação */}
          <div>
            <img
              src="/LogoNubexBranca.png"
              alt="NUBEX - Núcleo de Biologia Experimental"
              className={styles.logo}
            />
            <p className={styles.descricao}>
              Plataforma multiusuária de pesquisa e inovação em saúde da Universidade
              de Fortaleza (Unifor), promovendo excelência científica e apoio ao
              desenvolvimento tecnológico.
            </p>
            <div className={styles.vlibrasBadge}>
              <span aria-hidden="true">🤟</span>
              <span>Conteúdo acessível em Libras (VLibras)</span>
            </div>
          </div>

          {/* Coluna 2: Links Rápidos */}
          <div>
            <h2 className={styles.colunaTitulo}>Links Rápidos</h2>
            <ul className={styles.lista}>
              <li>
                <Link to="/quem-somos" className={styles.link}>
                  Quem Somos
                </Link>
              </li>
              <li>
                <Link to="/unidades" className={styles.link}>
                  Unidades Estratégicas
                </Link>
              </li>
              <li>
                <Link to="/servicos" className={styles.link}>
                  Serviços e Portfólio
                </Link>
              </li>
              <li>
                <Link to="/agendamento" className={styles.link}>
                  Agendamentos
                </Link>
              </li>
              <li>
                <Link to="/avisos" className={styles.link}>
                  Avisos e Status
                </Link>
              </li>
              <li>
                <Link to="/parceiros" className={styles.link}>
                  Parceiros
                </Link>
              </li>
              <li>
                <Link to="/faq" className={styles.link}>
                  Perguntas Frequentes
                </Link>
              </li>
            </ul>
          </div>

          {/* Coluna 3: Contato e Horários */}
          <div>
            <h2 className={styles.colunaTitulo}>Atendimento</h2>
            <div className={styles.infoItem}>
              <svg
                className={styles.infoIcone}
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="2"
                strokeLinecap="round"
                strokeLinejoin="round"
                aria-hidden="true"
              >
                <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z" />
                <circle cx="12" cy="10" r="3" />
              </svg>
              <span>
                Av. Washington Soares, 1321 - Edson Queiroz, Fortaleza - CE
              </span>
            </div>

            <div className={styles.infoItem}>
              <svg
                className={styles.infoIcone}
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="2"
                strokeLinecap="round"
                strokeLinejoin="round"
                aria-hidden="true"
              >
                <path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z" />
              </svg>
              <span>(85) 3477-3000</span>
            </div>

            <div className={styles.infoItem}>
              <svg
                className={styles.infoIcone}
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="2"
                strokeLinecap="round"
                strokeLinejoin="round"
                aria-hidden="true"
              >
                <circle cx="12" cy="12" r="10" />
                <polyline points="12 6 12 12 16 14" />
              </svg>
              <span>Segunda a sexta-feira, das 8h às 18h</span>
            </div>

            <div style={{ marginTop: "16px" }}>
              <Link to="/contato" className={styles.link} style={{ color: "var(--cor-acao)", fontWeight: 600 }}>
                Formulário de Contato →
              </Link>
            </div>
          </div>

          {/* Coluna 4: Institucional */}
          <div>
            <h2 className={styles.colunaTitulo}>Institucional</h2>
            <ul className={styles.lista}>
              <li>
                <a
                  href="https://www.unifor.br"
                  target="_blank"
                  rel="noopener noreferrer"
                  className={styles.link}
                >
                  Portal Unifor
                </a>
              </li>
              <li>
                <Link to="/missao-visao-valores" className={styles.link}>
                  Missão, Visão e Valores
                </Link>
              </li>
              <li>
                <Link to="/diferenciais" className={styles.link}>
                  Diferenciais e Impacto
                </Link>
              </li>
              <li>
                <Link to="/login" className={styles.link}>
                  Acessar Portal
                </Link>
              </li>
            </ul>
          </div>
        </div>

        {/* Rodapé Inferior */}
        <div className={styles.rodapeInferior}>
          <p className={styles.copyright}>
            © {new Date().getFullYear()} NUBEX - Núcleo de Biologia Experimental. Universidade de Fortaleza. Todos os direitos reservados.
          </p>
          <p className={styles.copyright}>
            Desenvolvido em conformidade com WCAG 2.1 AA e LGPD.
          </p>
        </div>
      </div>
    </footer>
  );
}
