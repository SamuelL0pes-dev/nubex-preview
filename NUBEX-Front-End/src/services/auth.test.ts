import { describe, it, expect, beforeEach, afterEach, vi } from "vitest";
import {
  realizarLogin,
  salvarSessao,
  obterToken,
  limparSessao,
  CHAVE_TOKEN,
  CHAVE_USUARIO,
} from "./auth";
import { api } from "./api";
import type { LoginResposta } from "../types/usuario";

const respostaLoginMock: LoginResposta = {
  token: "jwt-token-12345",
  tipoDeToken: "Bearer",
  expiraEmSegundos: 7200,
  usuario: {
    id: 1,
    nome: "Prof. Dr. Samuel Lopes",
    perfil: "SOLICITANTE",
    tipo: "INTERNO",
  },
};

describe("auth service", () => {
  beforeEach(() => {
    localStorage.clear();
    sessionStorage.clear();
    vi.restoreAllMocks();
  });

  afterEach(() => {
    localStorage.clear();
    sessionStorage.clear();
  });

  it("salva e recupera a sessao no storage", () => {
    salvarSessao(respostaLoginMock, true);
    expect(localStorage.getItem(CHAVE_TOKEN)).toBe("jwt-token-12345");
    expect(obterToken()).toBe("jwt-token-12345");

    const usuarioSalvo = JSON.parse(localStorage.getItem(CHAVE_USUARIO) || "{}");
    expect(usuarioSalvo.nome).toBe("Prof. Dr. Samuel Lopes");

    limparSessao();
    expect(obterToken()).toBeNull();
    expect(localStorage.getItem(CHAVE_TOKEN)).toBeNull();
  });

  it("chama POST /auth/login e salva a sessao", async () => {
    const postSpy = vi.spyOn(api, "post").mockResolvedValueOnce(respostaLoginMock);

    const resultado = await realizarLogin({
      identificador: "samuel.lopes@unifor.br",
      senha: "SenhaValida123",
    });

    expect(postSpy).toHaveBeenCalledWith("/auth/login", {
      identificador: "samuel.lopes@unifor.br",
      senha: "SenhaValida123",
    });

    expect(resultado.token).toBe("jwt-token-12345");
    expect(obterToken()).toBe("jwt-token-12345");
  });
});
