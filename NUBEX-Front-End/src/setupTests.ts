// ensina ao expect do Vitest as frases do jest-dom (toBeInTheDocument e companhia)
import "@testing-library/jest-dom/vitest";

// Endereços padrão dos microsserviços para execução de testes unitários
if (!import.meta.env.VITE_API_USUARIOS) {
  import.meta.env.VITE_API_USUARIOS = "http://localhost:8081";
  import.meta.env.VITE_API_AGENDAMENTOS = "http://localhost:8082";
  import.meta.env.VITE_API_CATALOGO = "http://localhost:8083";
  import.meta.env.VITE_API_PROJETOS = "http://localhost:8084";
  import.meta.env.VITE_API_CONTEUDO = "http://localhost:8085";
}
