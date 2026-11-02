package br.edu.unifor.nubex.usuario.dto;

import br.edu.unifor.nubex.usuario.model.StatusUsuario;
import java.time.Year;

/**
 * Resposta do pedido de acesso: o protocolo e o estado em que a conta nasceu.
 *
 * <p>O protocolo é derivado do id do pedido, e não sorteado. A tela do portal
 * gerava um número aleatório no navegador, que servia para mostrar e não para
 * nada mais: ninguém conseguia achar o pedido por ele. Este existe no banco.
 */
public record SolicitacaoAcessoResposta(String protocolo, StatusUsuario status) {

    /**
     * Monta o protocolo no formato SOL-2026-000007.
     *
     * <p>O ano é informativo, para a pessoa saber de quando é o pedido sem
     * consultar nada. Quem identifica de verdade é o id, que é único para sempre
     * -- então dois pedidos nunca colidem, mesmo virando o ano.
     */
    public static SolicitacaoAcessoResposta de(Long pedidoId, StatusUsuario status) {
        return new SolicitacaoAcessoResposta(
                "SOL-" + Year.now() + "-" + String.format("%06d", pedidoId), status);
    }
}
