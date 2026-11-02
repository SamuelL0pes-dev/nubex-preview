import { describe, it, expect, beforeEach, afterEach, vi } from "vitest";
import { api, enderecoDe } from "./api";

describe("api service", () => {
  beforeEach(() => {
    localStorage.clear();
    sessionStorage.clear();
    vi.restoreAllMocks();
  });

  afterEach(() => {
    localStorage.clear();
    sessionStorage.clear();
  });

  describe("enderecoDe", () => {
    it("resolve rotas de auth para o usuario-service", () => {
      const url = enderecoDe("/auth/login");
      expect(url).toContain("/api/auth/login");
    });

    it("resolve rotas de cadastro para o usuario-service", () => {
      const url = enderecoDe("/cadastro/solicitante");
      expect(url).toContain("/api/cadastro/solicitante");
    });

    it("resolve rotas de catalogo, conteudo, etc.", () => {
      expect(enderecoDe("/noticias")).toContain("/api/noticias");
      expect(enderecoDe("/unidades")).toContain("/api/unidades");
      expect(enderecoDe("/agendamentos")).toContain("/api/agendamentos");
      expect(enderecoDe("/projetos")).toContain("/api/projetos");
    });

    it("lanca erro quando o recurso nao esta mapeado em ONDE_MORA", () => {
      expect(() => enderecoDe("/recurso-desconhecido")).toThrow(
        'Recurso "recurso-desconhecido" nao esta mapeado em ONDE_MORA.',
      );
    });
  });

  describe("pedir / interceptador de autorizacao", () => {
    it("anexa o cabecalho Authorization: Bearer <token> quando ha token no storage", async () => {
      localStorage.setItem("nubex_token", "jwt-token-de-teste");

      const mockFetch = vi.fn().mockResolvedValue({
        ok: true,
        status: 200,
        json: async () => ({ sucesso: true }),
      });
      globalThis.fetch = mockFetch;

      await api.get("/auth/teste");

      expect(mockFetch).toHaveBeenCalled();
      const chamadas = mockFetch.mock.calls[0];
      const opcoes = chamadas[1];
      const headers = opcoes.headers;
      expect(headers.get("Authorization")).toBe("Bearer jwt-token-de-teste");
    });

    it("lanca ErroDaApi com mensagem, status e campos no erro", async () => {
      const mockFetch = vi.fn().mockResolvedValue({
        ok: false,
        status: 409,
        json: async () => ({
          mensagem: "Já existe uma conta com este e-mail",
        }),
      });
      globalThis.fetch = mockFetch;

      await expect(api.post("/cadastro/solicitante", {})).rejects.toThrow(
        "Já existe uma conta com este e-mail",
      );
    });
  });
});
