import { cleanup, fireEvent, render, screen } from "@testing-library/react";
import { afterEach, describe, expect, it, vi } from "vitest";
import { EstadoSecao } from "./EstadoSecao";

afterEach(() => {
  cleanup();
});

describe("EstadoSecao", () => {
  it("renderiza o estado carregando com skeleton e atributos de acessibilidade", () => {
    render(
      <EstadoSecao carregando={true} nomeSecao="Mural de notícias">
        <div>Conteúdo pronto</div>
      </EstadoSecao>,
    );

    expect(screen.queryByText("Conteúdo pronto")).not.toBeInTheDocument();
    const container = screen.getByText(
      /Carregando informações de Mural de notícias/i,
    );
    expect(container).toBeInTheDocument();
  });

  it("renderiza o estado pronto com os elementos filhos", () => {
    render(
      <EstadoSecao carregando={false} erro={null}>
        <div data-testid="conteudo-pronto">
          Conteúdo renderizado com sucesso
        </div>
      </EstadoSecao>,
    );

    expect(screen.getByTestId("conteudo-pronto")).toBeInTheDocument();
    expect(
      screen.getByText("Conteúdo renderizado com sucesso"),
    ).toBeInTheDocument();
    expect(screen.queryByRole("alert")).not.toBeInTheDocument();
  });

  it("renderiza o estado pronto usando função render-prop", () => {
    render(
      <EstadoSecao<{ titulo: string }>
        carregando={false}
        erro={null}
        dado={{ titulo: "Dado vindo da API" }}
      >
        {(dado) => <h1>{dado.titulo}</h1>}
      </EstadoSecao>,
    );

    expect(screen.getByText("Dado vindo da API")).toBeInTheDocument();
  });

  it("renderiza erro de serviço fora do ar com role='alert' e sem derrubar o restante", () => {
    render(
      <EstadoSecao
        carregando={false}
        erro={new Error("503 Service Unavailable")}
        tipoErro="servico_indisponivel"
        nomeSecao="o mural de avisos"
      >
        <div>Conteúdo</div>
      </EstadoSecao>,
    );

    const alerta = screen.getByRole("alert");
    expect(alerta).toBeInTheDocument();
    expect(
      screen.getByText("Serviço temporariamente indisponível"),
    ).toBeInTheDocument();
    expect(
      screen.getByText(/Não foi possível carregar o mural de avisos/i),
    ).toBeInTheDocument();
    expect(screen.queryByText("Conteúdo")).not.toBeInTheDocument();
  });

  it("renderiza erro específico quando o usuário está sem internet", () => {
    render(
      <EstadoSecao
        carregando={false}
        erro={new Error("Failed to fetch")}
        tipoErro="sem_internet"
        nomeSecao="parceiros"
      >
        <div>Conteúdo</div>
      </EstadoSecao>,
    );

    const alerta = screen.getByRole("alert");
    expect(alerta).toBeInTheDocument();
    expect(screen.getByText("Sem conexão com a internet")).toBeInTheDocument();
    expect(
      screen.getByText(
        /Você parece estar offline. Verifique sua conexão com a rede/i,
      ),
    ).toBeInTheDocument();
  });

  it("exibe o botão 'Tentar novamente' e dispara o callback quando clicado", () => {
    const tentarNovamenteMock = vi.fn();

    render(
      <EstadoSecao
        carregando={false}
        erro={new Error("Erro temporário")}
        onTentarNovamente={tentarNovamenteMock}
        nomeSecao="Mural"
      >
        <div>Conteúdo</div>
      </EstadoSecao>,
    );

    const botao = screen.getByRole("button", {
      name: /Tentar carregar Mural novamente/i,
    });
    expect(botao).toBeInTheDocument();

    fireEvent.click(botao);
    expect(tentarNovamenteMock).toHaveBeenCalledTimes(1);
  });

  it("NÃO exibe o botão 'Tentar novamente' quando a função não faz sentido/não foi fornecida", () => {
    render(
      <EstadoSecao carregando={false} erro={new Error("Erro estático")}>
        <div>Conteúdo</div>
      </EstadoSecao>,
    );

    expect(screen.queryByRole("button")).not.toBeInTheDocument();
  });
});
