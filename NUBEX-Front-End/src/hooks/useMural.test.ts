import { renderHook, waitFor } from "@testing-library/react";
import { describe, it, expect, vi, beforeEach } from "vitest";
import { useMural } from "./useMural";
import { buscarEditalAberto, listarNoticias } from "../services/conteudo";
import type { Edital, Noticia } from "../types/conteudo";

vi.mock("../services/conteudo");

const listar = vi.mocked(listarNoticias);
const buscarEdital = vi.mocked(buscarEditalAberto);

function noticia(id: number): Noticia {
  return {
    id,
    titulo: `Noticia ${id}`,
    resumo: null,
    unidade: null,
    imagemUrl: null,
    link: null,
    dataPublicacao: "2026-10-20",
    destaque: false,
  };
}

const EDITAL: Edital = {
  ...noticia(90),
  titulo: "Edital aberto",
  dataEncerramento: "2026-11-14",
};

beforeEach(() => {
  vi.clearAllMocks();
});

describe("useMural", () => {
  it("com edital aberto mostra duas noticias e o edital no fim", async () => {
    listar.mockResolvedValue([noticia(1), noticia(2), noticia(3)]);
    buscarEdital.mockResolvedValue(EDITAL);

    const { result } = renderHook(() => useMural());

    await waitFor(() => expect(result.current.carregando).toBe(false));

    expect(result.current.cartoes).toHaveLength(3);
    expect(result.current.cartoes.map((cartao) => cartao.categoria)).toEqual([
      "noticia",
      "noticia",
      "edital",
    ]);
    expect(result.current.cartoes[2].titulo).toBe("Edital aberto");
    expect(result.current.erro).toBeNull();
  });

  it("sem edital aberto mostra tres noticias", async () => {
    listar.mockResolvedValue([noticia(1), noticia(2), noticia(3)]);
    buscarEdital.mockResolvedValue(undefined);

    const { result } = renderHook(() => useMural());

    await waitFor(() => expect(result.current.carregando).toBe(false));

    expect(result.current.cartoes.map((cartao) => cartao.categoria)).toEqual([
      "noticia",
      "noticia",
      "noticia",
    ]);
  });

  it("quando o servico falha devolve o erro e nenhum cartao", async () => {
    listar.mockRejectedValue(new Error("rede fora"));
    buscarEdital.mockResolvedValue(undefined);

    const { result } = renderHook(() => useMural());

    await waitFor(() => expect(result.current.carregando).toBe(false));

    expect(result.current.cartoes).toEqual([]);
    expect(result.current.erro?.message).toBe("rede fora");
  });
});