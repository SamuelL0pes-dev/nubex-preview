import { Link, useLocation } from "react-router-dom";
import styles from "./Breadcrumb.module.css";

const ROTAS_NOMES: Record<string, string> = {
  "quem-somos": "Quem Somos",
  "missao-visao-valores": "Missão, Visão e Valores",
  "unidades": "Unidades Estratégicas",
  "producao-animais": "Produção de Animais",
  "experimentacao": "Experimentação In Vivo e In Vitro",
  "habilidades-cirurgicas": "Habilidades Cirúrgicas",
  "biologia-estrutural": "Biologia Estrutural e Funcional",
  "biologia-molecular": "Biologia Molecular e Desenvolvimento",
  "bioagentes": "Bioagentes Patogênicos",
  "servicos": "Serviços e Portfólio",
  "diferenciais": "Diferenciais e Impacto",
  "agendamento": "Agendamento",
  "avisos": "Avisos e Status",
  "faq": "Perguntas Frequentes",
  "contato": "Contato",
  "parceiros": "Parceiros",
  "projetos": "Projetos de Pesquisa",
  "noticias": "Notícias e Editais",
  "login": "Acessar Portal",
  "solicitar-acesso": "Solicitar Acesso (Docente)",
  "pendente": "Pedido Pendente",
  "cadastro": "Cadastro",
  "solicitante": "Solicitante Interno"
};

export default function Breadcrumb() {
  const { pathname } = useLocation();

  // Não exibe breadcrumb na página inicial
  if (pathname === "/") {
    return null;
  }

  const partes = pathname.split("/").filter(Boolean);

  return (
    <nav aria-label="Trilha de navegação" className={styles.container}>
      <ol className={styles.lista}>
        <li className={styles.item}>
          <Link to="/" className={styles.link}>
            Início
          </Link>
        </li>

        {partes.map((parte, index) => {
          const ehUltimo = index === partes.length - 1;
          const caminho = `/${partes.slice(0, index + 1).join("/")}`;
          const rotulo = ROTAS_NOMES[parte] || decodeURIComponent(parte);

          return (
            <li key={caminho} className={styles.item}>
              <span className={styles.separador} aria-hidden="true">
                /
              </span>
              {ehUltimo ? (
                <span className={styles.ativo} aria-current="page">
                  {rotulo}
                </span>
              ) : (
                <Link to={caminho} className={styles.link}>
                  {rotulo}
                </Link>
              )}
            </li>
          );
        })}
      </ol>
    </nav>
  );
}
