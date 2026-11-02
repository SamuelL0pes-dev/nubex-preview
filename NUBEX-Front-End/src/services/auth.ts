import { api } from "./api";
import type { LoginRequisicao, LoginResposta } from "../types/usuario";

export const CHAVE_TOKEN = "nubex_token";
export const CHAVE_USUARIO = "nubex_usuario";

/**
 * Obtém o token JWT atualmente armazenado na sessão ou armazenamento local.
 */
export function obterToken(): string | null {
  try {
    return sessionStorage.getItem(CHAVE_TOKEN) ?? localStorage.getItem(CHAVE_TOKEN);
  } catch {
    return null;
  }
}

/**
 * Persiste os dados de autenticação após o login bem-sucedido.
 */
export function salvarSessao(resposta: LoginResposta, persistir = true): void {
  try {
    const storage = persistir ? localStorage : sessionStorage;
    storage.setItem(CHAVE_TOKEN, resposta.token);
    storage.setItem(CHAVE_USUARIO, JSON.stringify(resposta.usuario));
  } catch (erro) {
    console.warn("Não foi possível persistir a sessão de usuário:", erro);
  }
}

/**
 * Remove os dados de autenticação para efetuar logout.
 */
export function limparSessao(): void {
  try {
    sessionStorage.removeItem(CHAVE_TOKEN);
    sessionStorage.removeItem(CHAVE_USUARIO);
    localStorage.removeItem(CHAVE_TOKEN);
    localStorage.removeItem(CHAVE_USUARIO);
  } catch (erro) {
    console.warn("Não foi possível limpar a sessão:", erro);
  }
}

/**
 * Efetua login no usuario-service (POST /api/auth/login).
 * Aceita e-mail institucional ou matrícula no campo identificador.
 */
export async function realizarLogin(dados: LoginRequisicao): Promise<LoginResposta> {
  const resposta = await api.post<LoginResposta>("/auth/login", dados);
  salvarSessao(resposta);
  return resposta;
}
