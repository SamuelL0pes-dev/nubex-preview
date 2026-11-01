/**
 * Tipos e modelos para o domínio de Usuários e Pedidos de Acesso (RF051, RF052, RF054).
 */

export type PerfilUsuario =
  | "PARTICIPANTE"
  | "SOLICITANTE"
  | "ADMINISTRADOR"
  | "SUPERVISAO_ADMINISTRATIVA"
  | "ADMINISTRADOR_CONSULTA";

export type TipoUsuario = "INTERNO" | "EXTERNO";

export type VinculoUsuario =
  | "DOCENTE"
  | "PESQUISADOR"
  | "MESTRANDO"
  | "DOUTORANDO"
  | "GRADUANDO"
  | "TECNICO";

export type StatusUsuario =
  | "PENDENTE_EMAIL"
  | "AGUARDANDO_APROVACAO"
  | "ATIVO"
  | "RECUSADO"
  | "INATIVO";

export type StatusPedido = "AGUARDANDO" | "APROVADO" | "RECUSADO";

/**
 * Dados enviados no formulário de solicitação de acesso para Solicitante Interno (RF052).
 */
export interface SolicitacaoSolicitanteInput {
  nome: string;
  email: string;
  senha: string;
  confirmacaoSenha: string;
  telefone: string;
  vinculo: "DOCENTE" | "PESQUISADOR";
  centro: string;
  lattesUrl: string;
  orcidUrl?: string;
  termosAceitos: boolean;
}

/**
 * Resumo do pedido de aprovação para exibição na tela de Pedido Pendente (RF052, RF058, RF088).
 */
export interface PedidoPendenteInfo {
  protocolo: string;
  nome: string;
  email: string;
  telefone: string;
  vinculo: "DOCENTE" | "PESQUISADOR";
  centro: string;
  lattesUrl: string;
  orcidUrl?: string;
  criadoEm: string;
  status: StatusUsuario;
  motivoRecusa?: string;
}
