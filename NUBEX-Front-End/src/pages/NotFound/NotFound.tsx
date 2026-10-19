import { Link } from "react-router-dom";
import styles from "./NotFound.module.css";

export default function NotFound() {
  return (
    <div className={styles.container}>
      <span className={styles.codigo}>404</span>
      <h1 className={styles.titulo}>Página Não Encontrada</h1>
      <p className={styles.descricao}>
        O endereço que você tentou acessar não existe ou foi removido do portal.
      </p>
      <Link to="/" className={styles.botao}>
        Voltar para a Página Inicial
      </Link>
    </div>
  );
}
