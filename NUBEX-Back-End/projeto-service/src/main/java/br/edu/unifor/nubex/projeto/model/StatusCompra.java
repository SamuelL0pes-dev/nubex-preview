package br.edu.unifor.nubex.projeto.model;

/**
 * Situação de uma solicitação de compra (RF069, RF070, RF075).
 *
 * O caminho é AGUARDANDO -> APROVADA -> ENCAMINHADA, com RECUSADA como
 * desfecho alternativo.
 */
public enum StatusCompra {

    /** Registrada pelo solicitante, esperando a avaliação da diretoria (RF069). */
    AGUARDANDO,

    /** Aprovada pela diretoria, com justificativa registrada (RF070). */
    APROVADA,

    /** Recusada pela diretoria, com justificativa registrada (RF070). */
    RECUSADA,

    /**
     * A supervisão administrativa registrou o encaminhamento ao sistema de
     * compras da Unifor (RF070, RF075).
     *
     * É o estado final: acompanhar a compra depois disso está no escopo
     * negativo do documento, que exclui "lançamento das solicitações de
     * compra no sistema de compras da Unifor e acompanhamento da compra".
     */
    ENCAMINHADA
}
