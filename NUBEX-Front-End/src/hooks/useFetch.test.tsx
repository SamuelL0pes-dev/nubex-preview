import { act, renderHook, waitFor } from "@testing-library/react";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { api, ErroDaApi } from "../services/api";
import { identificarTipoErro, useFetch } from "./useFetch";

describe("useFetch", () => {
  beforeEach(() => {
    vi.restoreAllMocks();
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it("identifica corretamente tipo de erro quando está offline", () => {
    vi.stubGlobal("navigator", { onLine: false });
    expect(identificarTipoErro(new Error("Qualquer erro"))).toBe(
      "sem_internet",
    );
  });

  it("identifica erro de serviço 500 ou 503 como servico_indisponivel", () => {
    vi.stubGlobal("navigator", { onLine: true });
    expect(
      identificarTipoErro(new ErroDaApi(500, "Internal Server Error")),
    ).toBe("servico_indisponivel");
    expect(identificarTipoErro(new ErroDaApi(503, "Service Unavailable"))).toBe(
      "servico_indisponivel",
    );
  });

  it("identifica erro 401 como nao_autenticado", () => {
    vi.stubGlobal("navigator", { onLine: true });
    const erro401 = new ErroDaApi(401, "Não autenticado");
    expect(identificarTipoErro(erro401)).toBe("nao_autenticado");
  });

  it("identifica erro 403 como sem_permissao", () => {
    vi.stubGlobal("navigator", { onLine: true });
    const erro403 = new ErroDaApi(
      403,
      "Você não tem permissão para acessar este recurso",
    );
    expect(identificarTipoErro(erro403)).toBe("sem_permissao");
  });

  it("identifica outros erros de cliente como erro_inesperado", () => {
    vi.stubGlobal("navigator", { onLine: true });
    const erro404 = new ErroDaApi(404, "Não encontrado");
    expect(identificarTipoErro(erro404)).toBe("erro_inesperado");
  });

  it("carrega dados com sucesso da api", async () => {
    vi.stubGlobal("navigator", { onLine: true });
    vi.spyOn(api, "get").mockResolvedValueOnce([{ id: 1, nome: "Item 1" }]);

    const { result } = renderHook(() =>
      useFetch<Array<{ id: number; nome: string }>>("/teste"),
    );

    expect(result.current.carregando).toBe(true);
    expect(result.current.dado).toBeNull();

    await waitFor(() => {
      expect(result.current.carregando).toBe(false);
    });

    expect(result.current.dado).toEqual([{ id: 1, nome: "Item 1" }]);
    expect(result.current.erro).toBeNull();
  });

  it("trata erro de serviço fora do ar e permite recarregar()", async () => {
    vi.stubGlobal("navigator", { onLine: true });
    vi.spyOn(api, "get")
      .mockRejectedValueOnce(new ErroDaApi(500, "Erro interno"))
      .mockResolvedValueOnce([{ id: 2, nome: "Item 2" }]);

    const { result } = renderHook(() =>
      useFetch<Array<{ id: number; nome: string }>>("/teste"),
    );

    await waitFor(() => {
      expect(result.current.carregando).toBe(false);
    });

    expect(result.current.erro).toBeTruthy();
    expect(result.current.tipoErro).toBe("servico_indisponivel");
    expect(result.current.dado).toBeNull();

    // Dispara recarregar
    act(() => {
      result.current.recarregar();
    });

    await waitFor(() => {
      expect(result.current.carregando).toBe(false);
      expect(result.current.dado).toEqual([{ id: 2, nome: "Item 2" }]);
    });

    expect(result.current.erro).toBeNull();
  });

  it("detecta imediatamente ausência de internet", async () => {
    vi.stubGlobal("navigator", { onLine: false });
    const getSpy = vi.spyOn(api, "get");

    const { result } = renderHook(() => useFetch("/teste"));

    await waitFor(() => {
      expect(result.current.carregando).toBe(false);
    });

    expect(result.current.tipoErro).toBe("sem_internet");
    expect(result.current.erro?.message).toContain("Sem conexão");
    expect(getSpy).not.toHaveBeenCalled();
  });
});
