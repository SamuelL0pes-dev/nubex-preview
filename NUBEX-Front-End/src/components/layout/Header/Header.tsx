import styles from "./Header.module.css";
import { useState, useEffect, useRef } from "react";
import { Link, NavLink, useLocation } from "react-router-dom";
import { USUARIO_ATUAL } from "../../../mocks/usuario";
import { URL_PORTAL } from "../../../constants/portal";

export default function Header() {
  const { pathname } = useLocation();
  return <HeaderContent key={pathname} pathname={pathname} />;
}

function HeaderContent({ pathname }: { pathname: string }) {
  
  const [menuAberto, setMenuAberto] = useState<"sobre" | "unidades" | null>(
    null,
  );
  const [rolou, setRolou] = useState(false);
  const [contaAberta, setContaAberta] = useState(false);
  const contaRef = useRef<HTMLDivElement>(null);

  const temHero = pathname === "/";
  const solido = rolou || !temHero;

  useEffect(() => {
    function aoRolar() {
      setRolou(window.scrollY > 50);
    }
    aoRolar();
    window.addEventListener("scroll", aoRolar, { passive: true });
    return () => window.removeEventListener("scroll", aoRolar);
  }, []);

  useEffect(() => {
    if (!contaAberta) return;

    function aoClicarFora(evento: MouseEvent) {
      if (
        contaRef.current &&
        !contaRef.current.contains(evento.target as Node)
      ) {
        setContaAberta(false);
      }
    }

    function aoTeclar(evento: KeyboardEvent) {
      if (evento.key === "Escape") setContaAberta(false);
    }

    document.addEventListener("mousedown", aoClicarFora);
    document.addEventListener("keydown", aoTeclar);

    return () => {
      document.removeEventListener("mousedown", aoClicarFora);
      document.removeEventListener("keydown", aoTeclar);
    };
  }, [contaAberta]);

  const classeItem = ({ isActive }: { isActive: boolean }) =>
    `${styles.itemMenu} ${isActive ? styles.itemAtivo : ""}`;

  return (
    <header className={`${styles.header} ${solido ? styles.headerSolido : ""}`}>
      {}
      <div className={styles.barraPrincipal}>
        {}
        <div className={styles.container}>
          {}
          <Link to="/" className={styles.logo}>
            <img
              src="/LogoNubexBranca.png"
              alt="Logo Nubex"
              className={styles.logoBranca}
            />
            <img
              src="/LogoNubexHorizontalUnifor.png"
              alt=""
              className={styles.logoColorida}
            />
          </Link>

          {}
          <nav aria-label="Navegação Principal" className={styles.nav}>
            <ul className={styles.menu}>
              {}
              <li
                onMouseLeave={() => setMenuAberto(null)}
                onMouseEnter={() => setMenuAberto("sobre")}
                className={styles.itemDropdown}
              >
                {}
                <button
                  className={styles.itemMenu}
                  type="button"
                  aria-expanded={menuAberto === "sobre"}
                  onClick={() =>
                    setMenuAberto(menuAberto === "sobre" ? null : "sobre")
                  }
                >
                  Sobre
                  <span className={styles.seta} aria-hidden="true"></span>
                </button>
                {}
                <ul
                  className={`${styles.submenu} ${menuAberto === "sobre" ? styles.submenuAberto : ""}`}
                >
                  <li>
                    <Link to="/quem-somos">Quem Somos</Link>
                  </li>
                  <li>
                    <Link to="/missao-visao-valores">
                      Missão, Visão e Valores
                    </Link>
                  </li>
                  <li>
                    <Link to="/quem-somos#corpo-cientifico">
                      Corpo Científico
                    </Link>
                  </li>
                  <li>
                    <Link to="/diferenciais">Diferenciais e Impacto</Link>
                  </li>
                </ul>
              </li>
              {}
              <li
                onMouseLeave={() => setMenuAberto(null)}
                onMouseEnter={() => setMenuAberto("unidades")}
                className={styles.itemDropdown}
              >
                {}
                <button
                  className={styles.itemMenu}
                  type="button"
                  aria-expanded={menuAberto === "unidades"}
                  onClick={() =>
                    setMenuAberto(menuAberto === "unidades" ? null : "unidades")
                  }
                >
                  Unidades
                  <span className={styles.seta} aria-hidden="true"></span>
                </button>
                {}
                <ul
                  className={`${styles.submenu} ${menuAberto === "unidades" ? styles.submenuAberto : ""}`}
                >
                  <li>
                    <Link to="/unidades/producao-animais">
                      Produção de Animais
                    </Link>
                  </li>
                  <li>
                    <Link to="/unidades/experimentacao">
                      Experimentação In Vivo / In Vitro
                    </Link>
                  </li>
                  <li>
                    <Link to="/unidades/habilidades-cirurgicas">
                      Habilidades Cirúrgicas
                    </Link>
                  </li>
                  <li>
                    <Link to="/unidades/biologia-estrutural">
                      Biologia Estrutural e Funcional
                    </Link>
                  </li>
                  <li>
                    <Link to="/unidades/biologia-molecular">
                      Biologia Molecular e Desenvolvimento
                    </Link>
                  </li>
                  <li>
                    <Link to="/unidades/bioagentes">Bioagentes Patogênicos</Link>
                  </li>
                </ul>
              </li>
              <li>
                {}
                <NavLink to="/servicos" className={classeItem}>
                  Serviços
                </NavLink>
              </li>
              <li>
                <NavLink to="/avisos" className={classeItem}>
                  Avisos
                </NavLink>
              </li>
            </ul>
          </nav>

          {}
          <Link to="/agendamento" className={styles.cta}>
            Agendamentos
          </Link>

          {USUARIO_ATUAL ? (

            <div className={styles.conta} ref={contaRef}>
              <button
                type="button"
                className={styles.contaBotao}
                aria-expanded={contaAberta}
                onClick={() => setContaAberta(!contaAberta)}
              >
                <span className={styles.contaNome}>{USUARIO_ATUAL.nome}</span>
                <span className={styles.contaSeta} aria-hidden="true">
                  ▾
                </span>
              </button>

              <div
                className={`${styles.contaMenu} ${contaAberta ? styles.contaAberta : ""}`}
              >
                <div className={styles.contaRotulo}>Minha conta</div>
                {}
                <a href="/admin">Painel Administrativo</a>
                <a className={styles.contaSair} href="/sair">
                  Sair do Sistema
                </a>
              </div>
            </div>
          ) : (
            <a href={URL_PORTAL} className={styles.ctaSec}>
              <svg
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="2"
                strokeLinecap="round"
                strokeLinejoin="round"
                aria-hidden="true"
              >
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
                <circle cx="12" cy="7" r="4" />
              </svg>
              Acessar Portal
            </a>
          )}
        </div>
      </div>
    </header>
  );
}