package br.edu.unifor.nubex.usuario.model;

/**
 * Em que ponto do fluxo de cadastro a conta está (RF058 a RF061, RF074).
 *
 * Só ATIVO entra no portal: o RF067 manda bloquear o acesso de toda conta
 * que não esteja ativa, conferindo perfil e status na API a cada requisição.
 *
 * O caminho normal é PENDENTE_EMAIL -> AGUARDANDO_APROVACAO -> ATIVO.
 */
public enum StatusUsuario {

    /** Cadastro feito, e-mail ainda não confirmado pelo código (RF057). */
    PENDENTE_EMAIL,

    /** E-mail confirmado, esperando a decisão do solicitante interno ou do administrador (RF058). */
    AGUARDANDO_APROVACAO,

    /** Aprovada e dentro do prazo de acesso. É o único status que entra no portal (RF067). */
    ATIVO,

    /** Pedido recusado por quem deveria aprovar (RF061). */
    RECUSADO,

    /** Prazo de acesso vencido (RF063) ou conta desligada (RF066). */
    INATIVO
}
