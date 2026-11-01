import type {
  PedidoPendenteInfo,
  SolicitacaoSolicitanteInput,
  StatusUsuario,
} from "../types/usuario";
import { api, ErroDaApi } from "./api";

const CHAVE_STORAGE = "nubex_pedido_solicitante";

/**
 * Salva os dados do pedido de solicitação no armazenamento local.
 */
export function salvarPedidoPendente(pedido: PedidoPendenteInfo): void {
  try {
    sessionStorage.setItem(CHAVE_STORAGE, JSON.stringify(pedido));
    localStorage.setItem(CHAVE_STORAGE, JSON.stringify(pedido));
  } catch (erro) {
    console.warn("Não foi possível persistir o pedido em storage:", erro);
  }
}

/**
 * Recupera os dados do último pedido de solicitação registrado.
 */
export function obterPedidoPendente(): PedidoPendenteInfo | null {
  try {
    const dadosSessao = sessionStorage.getItem(CHAVE_STORAGE);
    if (dadosSessao) return JSON.parse(dadosSessao) as PedidoPendenteInfo;

    const dadosLocal = localStorage.getItem(CHAVE_STORAGE);
    if (dadosLocal) return JSON.parse(dadosLocal) as PedidoPendenteInfo;
  } catch (erro) {
    console.warn("Erro ao ler pedido de solicitação:", erro);
  }
  return null;
}

/**
 * Cria um protocolo único institucional para a solicitação.
 */
function gerarProtocolo(): string {
  const data = new Date();
  const ano = data.getFullYear();
  const mes = String(data.getMonth() + 1).padStart(2, "0");
  const dia = String(data.getDate()).padStart(2, "0");
  const aleatorio = Math.floor(1000 + Math.random() * 9000);
  return `SOL-${ano}${mes}${dia}-${aleatorio}`;
}

/**
 * Formata a data atual para exibição em português.
 */
export function formatarDataHoraAtual(): string {
  const agora = new Date();
  return agora.toLocaleDateString("pt-BR", {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });
}

/**
 * Envia a solicitação de acesso para a API do backend (usuario-service).
 * Se a API estiver offline ou em ambiente demo, produz e persiste o pedido localmente com resiliência.
 */
export async function enviarSolicitacaoAcesso(
  dados: SolicitacaoSolicitanteInput,
): Promise<PedidoPendenteInfo> {
  const telefoneLimpo = dados.telefone.replace(/\D/g, "");
  const protocolo = gerarProtocolo();
  const criadoEm = formatarDataHoraAtual();

  const pedidoGerado: PedidoPendenteInfo = {
    protocolo,
    nome: dados.nome.trim(),
    email: dados.email.trim().toLowerCase(),
    telefone: dados.telefone,
    vinculo: dados.vinculo,
    centro: dados.centro,
    lattesUrl: dados.lattesUrl.trim(),
    orcidUrl: dados.orcidUrl?.trim() || undefined,
    criadoEm,
    status: "AGUARDANDO_APROVACAO",
  };

  const payloadBack = {
    nome: dados.nome.trim(),
    email: dados.email.trim().toLowerCase(),
    senha: dados.senha,
    telefone: telefoneLimpo,
    perfil: "SOLICITANTE",
    tipo: "INTERNO",
    vinculo: dados.vinculo,
    lattesUrl: dados.lattesUrl.trim(),
    orcidUrl: dados.orcidUrl?.trim() || null,
  };

  try {
    // Tenta registrar na API real do backend
    const resposta = await api.post<Partial<PedidoPendenteInfo>>(
      "/usuarios/solicitacoes",
      payloadBack,
    );

    const pedidoFinal: PedidoPendenteInfo = {
      ...pedidoGerado,
      ...(resposta || {}),
    };

    salvarPedidoPendente(pedidoFinal);
    return pedidoFinal;
  } catch (erro) {
    // Se a API responder erro de validação de negócio (ex: e-mail já existe), repassa
    if (erro instanceof ErroDaApi && erro.status < 500 && erro.status !== 404) {
      throw erro;
    }

    // Se estiver em ambiente demo ou serviço offline, registra o pedido simulado
    console.info(
      "Serviço de usuários offline ou em modo demonstração. Gravando pedido no ambiente local:",
      pedidoGerado,
    );
    salvarPedidoPendente(pedidoGerado);
    return pedidoGerado;
  }
}

/**
 * Atualiza o status do pedido (utilizado na demonstração interativa).
 */
export function atualizarStatusDemonstracao(
  novoStatus: StatusUsuario,
  base?: PedidoPendenteInfo,
  motivoRecusa?: string,
): PedidoPendenteInfo {
  const atual = base || obterPedidoPendente() || {
    protocolo: "SOL-20261101-7824",
    nome: "Prof. Dr. Samuel Lopes",
    email: "samuel.lopes@unifor.br",
    telefone: "(85) 99876-5432",
    vinculo: "DOCENTE" as const,
    centro: "Centro de Ciências Tecnológicas (CCT)",
    lattesUrl: "http://lattes.cnpq.br/4928172910492817",
    criadoEm: formatarDataHoraAtual(),
    status: "AGUARDANDO_APROVACAO" as const,
  };

  const atualizado: PedidoPendenteInfo = {
    ...atual,
    status: novoStatus,
    motivoRecusa:
      novoStatus === "RECUSADO"
        ? (motivoRecusa ||
          "Vínculo acadêmico institucional não validado junto à Pró-Reitoria de Pesquisa.")
        : undefined,
  };

  salvarPedidoPendente(atualizado);
  return atualizado;
}
