package br.edu.unifor.nubex.projeto.repository;

import br.edu.unifor.nubex.projeto.model.Projeto;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Consultas dos projetos de pesquisa. Cada método existe por causa de um
 * requisito, apontado no comentário.
 */
public interface ProjetoRepository extends JpaRepository<Projeto, Long> {

    /** "Meus projetos" do solicitante (RF068, /api/portal/projetos). */
    List<Projeto> findBySolicitanteIdAndAtivo(Long solicitanteId, boolean ativo);

    /**
     * Listagem pública (RF019, GET /api/projetos). Paginada porque é página
     * aberta e a lista cresce sem limite.
     */
    Page<Projeto> findByAtivo(boolean ativo, Pageable pagina);

    /**
     * Valida as duas condições que o RF069 exige da compra de uma só vez: o
     * projeto está ativo E pertence a quem está pedindo. O service chama isto
     * antes de gravar a solicitação; se der falso, lança NegocioException.
     */
    boolean existsByIdAndSolicitanteIdAndAtivo(Long id, Long solicitanteId, boolean ativo);
}
