import { describe, it, expect, beforeEach, afterEach, vi } from "vitest";
import { enviarSolicitacaoAcesso, obterPedidoPendente } from "./solicitante";
import { api, ErroDaApi } from "./api";
import type { SolicitacaoSolicitanteInput } from "../types/usuario";

const inputValido: SolicitacaoSolicitanteInput = {
  nome: "Prof. Dr. Samuel Lopes",
  email: "samuel.lopes@unifor.br",
  senha: "SenhaValida123",
  confirmacaoSenha: "SenhaValida123",
  telefone: "(85) 99999-8888",
  vinculo: "DOCENTE",
  centro: "Centro de Ciências Tecnológicas (CCT)",
  lattesUrl: "http://lattes.cnpq.br/1234567890123456",
  termosAceitos: true,
};

describe("solicitante service", () => {
  beforeEach(() => {
    localStorage.clear();
    sessionStorage.clear();
    vi.restoreAllMocks();
  });

  afterEach(() => {
    localStorage.clear();
    sessionStorage.clear();
  });

  it("chama /cadastro/solicitante e usa o protocolo retornado pela API real", async () => {
    const postSpy = vi.spyOn(api, "post").mockResolvedValueOnce({
      protocolo: "SOL-2026-000042",
      status: "AGUARDANDO_APROVACAO",
    });

    const resultado = await enviarSolicitacaoAcesso(inputValido);

    expect(postSpy).toHaveBeenCalledWith(
      "/cadastro/solicitante",
      expect.objectContaining({
        nome: "Prof. Dr. Samuel Lopes",
        email: "samuel.lopes@unifor.br",
        telefone: "85999998888",
        vinculo: "DOCENTE",
        lattesUrl: "http://lattes.cnpq.br/1234567890123456",
      }),
    );

    expect(resultado.protocolo).toBe("SOL-2026-000042");
    expect(resultado.status).toBe("AGUARDANDO_APROVACAO");

    const salvo = obterPedidoPendente();
    expect(salvo?.protocolo).toBe("SOL-2026-000042");
  });

  it("repassa o erro 409 quando o e-mail ja esta em uso no backend", async () => {
    vi.spyOn(api, "post").mockRejectedValueOnce(
      new ErroDaApi(409, "Já existe uma conta com este e-mail"),
    );

    await expect(enviarSolicitacaoAcesso(inputValido)).rejects.toThrow(
      "Já existe uma conta com este e-mail",
    );
  });

  it("repassa o erro 400 de validacao da API", async () => {
    vi.spyOn(api, "post").mockRejectedValueOnce(
      new ErroDaApi(400, "Dados inválidos"),
    );

    await expect(enviarSolicitacaoAcesso(inputValido)).rejects.toThrow(
      "Dados inválidos",
    );
  });

  it("gera pedido em modo demonstracao/offline caso a API nao responda (network error)", async () => {
    vi.spyOn(api, "post").mockRejectedValueOnce(new TypeError("Failed to fetch"));

    const resultado = await enviarSolicitacaoAcesso(inputValido);

    expect(resultado.protocolo).toMatch(/^SOL-\d{8}-\d{4}$/);
    expect(resultado.nome).toBe("Prof. Dr. Samuel Lopes");
    expect(resultado.status).toBe("AGUARDANDO_APROVACAO");
  });
});
