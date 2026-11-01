import { render, screen, fireEvent, cleanup, waitFor } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import { describe, it, expect, afterEach, vi } from "vitest";
import CadastroSolicitante from "./CadastroSolicitante";

const mockNavigate = vi.fn();
vi.mock("react-router-dom", async () => {
  const atual = await vi.importActual("react-router-dom");
  return {
    ...atual,
    useNavigate: () => mockNavigate,
  };
});

afterEach(() => {
  cleanup();
  vi.clearAllMocks();
});

function renderizar() {
  render(
    <MemoryRouter>
      <CadastroSolicitante />
    </MemoryRouter>,
  );
}

describe("CadastroSolicitante (RF052)", () => {
  it("renderiza o titulo da pagina e o aviso do fluxo bloqueante (RF052 / RF054)", () => {
    renderizar();

    expect(
      screen.getByRole("heading", {
        level: 1,
        name: /solicitar acesso como solicitante/i,
      }),
    ).toBeInTheDocument();

    // Contexto arquitetural explicado com clareza
    expect(screen.getByText(/por que esta solicitação requer aprovação prévia/i)).toBeInTheDocument();
    expect(screen.getByText(/solicitante ativo/i)).toBeInTheDocument();
  });

  it("exibe erros de validacao acessiveis quando o formulario e submetido vazio", async () => {
    renderizar();

    const botaoEnviar = screen.getByRole("button", {
      name: /submeter solicitação de acesso/i,
    });
    fireEvent.click(botaoEnviar);

    await waitFor(() => {
      expect(screen.getByText(/informe seu nome completo/i)).toBeInTheDocument();
      expect(screen.getByText(/informe seu e-mail institucional unifor/i)).toBeInTheDocument();
      expect(screen.getByText(/informe um telefone válido/i)).toBeInTheDocument();
      expect(screen.getByText(/o link do currículo lattes é obrigatório/i)).toBeInTheDocument();
      expect(screen.getByText(/crie uma senha de acesso/i)).toBeInTheDocument();
      expect(screen.getByText(/você deve concordar com os termos/i)).toBeInTheDocument();
    });

    // Garante que não navegou
    expect(mockNavigate).not.toHaveBeenCalled();
  });

  it("exige e-mail institucional @unifor.br ou @edu.unifor.br", async () => {
    renderizar();

    const campoEmail = screen.getByLabelText(/e-mail institucional unifor/i);
    fireEvent.change(campoEmail, { target: { value: "professor@gmail.com" } });

    const botaoEnviar = screen.getByRole("button", {
      name: /submeter solicitação de acesso/i,
    });
    fireEvent.click(botaoEnviar);

    await waitFor(() => {
      expect(
        screen.getByText(/exige e-mail institucional unifor \(@unifor\.br ou @edu\.unifor\.br\)/i),
      ).toBeInTheDocument();
    });
  });

  it("aplica mascara no campo de telefone", () => {
    renderizar();

    const campoTelefone = screen.getByLabelText(/telefone \/ celular/i);
    fireEvent.change(campoTelefone, { target: { value: "85999998888" } });

    expect(campoTelefone).toHaveValue("(85) 99999-8888");
  });

  it("valida que a confirmacao de senha confere com a senha digitada", async () => {
    renderizar();

    const campoSenha = screen.getByLabelText(/^senha de acesso/i);
    const campoConfirmacao = screen.getByLabelText(/confirmar senha/i);

    fireEvent.change(campoSenha, { target: { value: "SenhaForte@2026" } });
    fireEvent.change(campoConfirmacao, { target: { value: "OutraSenhaDiferente" } });

    const botaoEnviar = screen.getByRole("button", {
      name: /submeter solicitação de acesso/i,
    });
    fireEvent.click(botaoEnviar);

    await waitFor(() => {
      expect(screen.getByText(/as senhas digitadas não coincidem/i)).toBeInTheDocument();
    });
  });

  it("permite selecionar entre vinculo Docente e Pesquisador", () => {
    renderizar();

    const opcaoPesquisador = screen.getByRole("radio", {
      name: /pesquisador\(a\) institucional/i,
    });
    expect(opcaoPesquisador).not.toBeChecked();

    fireEvent.click(opcaoPesquisador);
    expect(opcaoPesquisador).toBeChecked();
  });

  it("submete com sucesso e redireciona para a tela de pedido pendente", async () => {
    renderizar();

    fireEvent.change(screen.getByLabelText(/nome completo/i), {
      target: { value: "Prof. Dr. Samuel Lopes" },
    });
    fireEvent.change(screen.getByLabelText(/e-mail institucional unifor/i), {
      target: { value: "samuel.lopes@unifor.br" },
    });
    fireEvent.change(screen.getByLabelText(/telefone \/ celular/i), {
      target: { value: "85999998888" },
    });
    fireEvent.change(screen.getByLabelText(/link do currículo lattes/i), {
      target: { value: "http://lattes.cnpq.br/1234567890123456" },
    });
    fireEvent.change(screen.getByLabelText(/^senha de acesso/i), {
      target: { value: "SenhaForte@123" },
    });
    fireEvent.change(screen.getByLabelText(/confirmar senha/i), {
      target: { value: "SenhaForte@123" },
    });

    const checkboxTermos = screen.getByRole("checkbox");
    fireEvent.click(checkboxTermos);

    const botaoEnviar = screen.getByRole("button", {
      name: /submeter solicitação de acesso/i,
    });
    fireEvent.click(botaoEnviar);

    await waitFor(() => {
      expect(mockNavigate).toHaveBeenCalledWith("/solicitar-acesso/pendente");
    });
  });
});
