package br.edu.unifor.nubex.projeto.repository;

import br.edu.unifor.nubex.projeto.model.SolicitacaoCompra;
import br.edu.unifor.nubex.projeto.model.StatusCompra;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Consultas das solicitações de compra (RF069, RF070, RF075).
 */
public interface SolicitacaoCompraRepository extends JpaRepository<SolicitacaoCompra, Long> {

    /** "Minhas solicitações" do solicitante (/api/portal/compras). */
    List<SolicitacaoCompra> findBySolicitanteId(Long solicitanteId);

    /**
     * A fila de quem avalia e de quem despacha (/api/admin/compras).
     * A diretoria filtra por AGUARDANDO (RF070); a supervisão administrativa,
     * por APROVADA (RF075). Paginada porque a lista só cresce.
     */
    Page<SolicitacaoCompra> findByStatus(StatusCompra status, Pageable pagina);

    /**
     * As compras de um projeto. Serve ao RF070, que manda avaliar "a coerência
     * entre os insumos solicitados e as metas, equipe e produtos científicos
     * declarados no projeto vinculado" -- quem avalia precisa ver o conjunto.
     */
    List<SolicitacaoCompra> findByProjetoId(Long projetoId);
}
