package br.edu.unifor.nubex.usuario.model;

/**
 * Situação de um pedido de aprovação de cadastro (RF060, RF061).
 *
 * Decidido o pedido, ele não aceita nova deliberação: quem garante isso é o
 * service, ao checar que o status ainda é AGUARDANDO antes de gravar.
 */
public enum StatusPedido {

    /** Esperando a decisão de quem vai aprovar. */
    AGUARDANDO,

    /** Aprovado: a conta do usuário passa a ATIVO (RF061). */
    APROVADO,

    /** Recusado: a conta do usuário passa a RECUSADO (RF061). */
    RECUSADO
}
