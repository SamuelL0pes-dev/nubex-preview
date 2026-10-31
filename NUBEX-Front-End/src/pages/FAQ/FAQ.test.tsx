import { render, screen, fireEvent, cleanup } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import { describe, it, expect, afterEach, vi } from "vitest";
import FAQ from "./FAQ";

afterEach(() => {
  cleanup();
  vi.unstubAllGlobals();
});

function renderizar() {
  render(
    <MemoryRouter>
      <FAQ />
    </MemoryRouter>,
  );
}

function buscar(texto: string) {
  fireEvent.change(screen.getByRole("searchbox", { name: /buscar dúvida/i }), {
    target: { value: texto },
  });
}

function simularMovimentoReduzido(ativo: boolean) {
  vi.stubGlobal(
    "matchMedia",
    vi.fn(() => ({ matches: ativo })),
  );
}

function clicarNoTema(nome: RegExp) {
  const secao = screen.getByRole("region", { name: nome });
  const rolar = vi.fn();
  secao.scrollIntoView = rolar;

  fireEvent.click(screen.getByRole("button", { name: nome }));

  return rolar;
}

describe("FAQ", () => {
  it("mostra o titulo da pagina", () => {
    renderizar();

    expect(
      screen.getByRole("heading", {
        level: 1,
        name: /tudo o que você precisa saber/i,
      }),
    ).toBeInTheDocument();
  });

  it("abre e fecha uma pergunta ao clicar", () => {
    renderizar();
    const pergunta = screen.getByRole("button", { name: /o que é o nubex/i });

    expect(pergunta).toHaveAttribute("aria-expanded", "false");
    fireEvent.click(pergunta);
    expect(pergunta).toHaveAttribute("aria-expanded", "true");
    fireEvent.click(pergunta);
    expect(pergunta).toHaveAttribute("aria-expanded", "false");
  });

  it("filtra as perguntas pela busca, ignorando acentos", () => {
    renderizar();
    buscar("comissao de etica");

    expect(
      screen.getByRole("button", { name: /aprovação ética/i }),
    ).toBeInTheDocument();
    expect(
      screen.queryByRole("button", { name: /o que é o nubex/i }),
    ).not.toBeInTheDocument();
    expect(screen.getByRole("status")).toHaveTextContent(
      /1 pergunta encontrada/i,
    );
  });

  it("acha a pergunta certa digitando uma frase com palavras soltas", () => {
    renderizar();
    buscar("como agendar um equipamento");

    const perguntas = screen.getAllByRole("button", { expanded: false });
    expect(perguntas[0]).toHaveAccessibleName(/uso de um equipamento/i);
  });

  it("rola ate a secao do tema clicado sem esconder as outras perguntas", () => {
    simularMovimentoReduzido(false);
    renderizar();

    const rolar = clicarNoTema(/parcerias e projetos/i);

    expect(rolar).toHaveBeenCalledWith({ behavior: "smooth", block: "start" });
    expect(
      screen.getByRole("button", { name: /firmar uma parceria/i }),
    ).toBeInTheDocument();
    expect(
      screen.getByRole("button", { name: /o que é o nubex/i }),
    ).toBeInTheDocument();
  });

  it("rola sem animacao quando o movimento reduzido esta ativo", () => {
    simularMovimentoReduzido(true);
    renderizar();

    const rolar = clicarNoTema(/parcerias e projetos/i);

    expect(rolar).toHaveBeenCalledWith({ behavior: "auto", block: "start" });
  });

  it("mostra aviso quando nada e encontrado e permite ver todas de novo", () => {
    renderizar();
    buscar("xyzabc");

    expect(
      screen.getByText(/nenhuma pergunta encontrada/i),
    ).toBeInTheDocument();

    fireEvent.click(
      screen.getByRole("button", { name: /ver todas as perguntas/i }),
    );

    expect(
      screen.getByRole("button", { name: /o que é o nubex/i }),
    ).toBeInTheDocument();
  });
});