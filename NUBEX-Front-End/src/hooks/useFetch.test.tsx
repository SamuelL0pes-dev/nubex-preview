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

  it("identifica erro de serviço 503 como servico_indisponivel", () => {
    vi.stubGlobal("navigator", { onLine: true });
    const erro503 = new ErroDaApi(503, "Service Unavailable");
    expect(identificarTipoErro(erro503)).toBe("servico_indisponivel");
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
