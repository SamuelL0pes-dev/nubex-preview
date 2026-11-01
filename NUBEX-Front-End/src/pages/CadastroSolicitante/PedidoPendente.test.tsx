import { render, screen, fireEvent, cleanup, waitFor } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import { describe, it, expect, afterEach } from "vitest";
import PedidoPendente from "./PedidoPendente";

afterEach(() => {
  cleanup();
  localStorage.clear();
  sessionStorage.clear();
});

function renderizar() {
  render(
    <MemoryRouter>
      <PedidoPendente />
    </MemoryRouter>,
  );
}

describe("PedidoPendente (RF052 / RF058)", () => {
  it("renderiza o estado inicial de Aguardando Aprovacao com protocolo e dados", () => {
    renderizar();

    expect(
      screen.getByRole("heading", {
        level: 1,
        name: /acompanhamento de solicitação de acesso/i,
      }),
    ).toBeInTheDocument();

    expect(screen.getByText(/status: aguardando aprovação/i)).toBeInTheDocument();
    expect(screen.getAllByText(/administrador do nubex/i).length).toBeGreaterThan(0);
  });

  it("exibe a explicacao institucional do fluxo bloqueante (RF054 / RF052)", () => {
    renderizar();

    expect(
      screen.getByText(/por que esta tela é a porta de entrada de toda a cadeia do nubex\?/i),
    ).toBeInTheDocument();
    expect(screen.getByText(/solicitante ativo/i)).toBeInTheDocument();
  });

  it("exibe as 4 etapas da linha do tempo do acesso", () => {
    renderizar();

    expect(screen.getByText(/1\. solicitação enviada/i)).toBeInTheDocument();
    expect(screen.getByText(/2\. deliberação administrativa \(rf052 \/ rf088\)/i)).toBeInTheDocument();
    expect(screen.getByText(/3\. notificação por e-mail/i)).toBeInTheDocument();
    expect(screen.getByText(/4\. login e desbloqueio da equipe/i)).toBeInTheDocument();
  });

  it("permite verificar o status na API com feedback", async () => {
    renderizar();

    const botaoVerificar = screen.getByRole("button", {
      name: /verificar status agora/i,
    });
    fireEvent.click(botaoVerificar);

    expect(screen.getByText(/verificando na api\.\.\./i)).toBeInTheDocument();

    await waitFor(
      () => {
        expect(
          screen.getByText(/status verificado: sua solicitação continua sob avaliação/i),
        ).toBeInTheDocument();
      },
      { timeout: 2000 },
    );
  });

  it("permite simular a aprovacao do administrador atraves do simulador da demo", () => {
    renderizar();

    const btnSimularAprovado = screen.getByRole("button", {
      name: /simular: administrador aprovou/i,
    });
    fireEvent.click(btnSimularAprovado);

    expect(screen.getByText(/status: aprovado \(ativo\)/i)).toBeInTheDocument();
    expect(screen.getByRole("link", { name: /acessar portal agora/i })).toBeInTheDocument();
  });

  it("permite simular a recusa do administrador atraves do simulador da demo", () => {
    renderizar();

    const btnSimularRecusado = screen.getByRole("button", {
      name: /simular: administrador recusou/i,
    });
    fireEvent.click(btnSimularRecusado);

    expect(screen.getByText(/status: recusado/i)).toBeInTheDocument();
    expect(
      screen.getByText(/solicitação de acesso não homologada/i),
    ).toBeInTheDocument();
  });
});
