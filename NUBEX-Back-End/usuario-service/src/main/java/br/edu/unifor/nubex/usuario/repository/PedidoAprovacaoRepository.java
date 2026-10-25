package br.edu.unifor.nubex.usuario.repository;

import br.edu.unifor.nubex.usuario.model.PedidoAprovacao;
import br.edu.unifor.nubex.usuario.model.StatusPedido;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Consultas dos pedidos de aprovação de cadastro (RF059 a RF061, RF088).
 */
public interface PedidoAprovacaoRepository extends JpaRepository<PedidoAprovacao, Long> {

    /**
     * "Pedidos aguardando a minha decisão", na aba de avisos do solicitante
     * interno (RF060).
     */
    List<PedidoAprovacao> findBySolicitanteIdAndStatus(Long solicitanteId, StatusPedido status);

    /**
     * Fila dos administradores: os pedidos de acesso como solicitante interno
     * não têm aprovador indicado, então qualquer ADMINISTRADOR decide
     * (RF052, RF088).
     */
    List<PedidoAprovacao> findBySolicitanteIsNullAndStatus(StatusPedido status);

    /**
     * O pedido em aberto de um usuário. O service confere que ainda está
     * AGUARDANDO antes de gravar a decisão: decidido, não aceita nova
     * deliberação (RF060).
     */
    Optional<PedidoAprovacao> findByUsuarioIdAndStatus(Long usuarioId, StatusPedido status);

    /**
     * Evita abrir um segundo pedido para quem já tem um em aberto. O índice
     * único parcial da migração é a garantia final.
     */
    boolean existsByUsuarioIdAndStatus(Long usuarioId, StatusPedido status);
}
