package br.edu.unifor.nubex.projeto.repository;

import br.edu.unifor.nubex.projeto.model.ProjetoIntegrante;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Consultas da equipe dos projetos (RF019, RF068).
 */
public interface ProjetoIntegranteRepository extends JpaRepository<ProjetoIntegrante, Long> {

    /**
     * A equipe de um projeto, para a página pública (RF019). Usa o índice
     * único uq_projeto_integrante, que começa por projeto_id.
     */
    List<ProjetoIntegrante> findByProjetoId(Long projetoId);

    /**
     * Impede incluir a mesma pessoa duas vezes na equipe. O índice único do
     * banco é a garantia final, em caso de duas requisições simultâneas; este
     * método existe para o service lançar ConflitoException (409) com
     * mensagem amigável antes disso.
     */
    boolean existsByProjetoIdAndUsuarioId(Long projetoId, Long usuarioId);
}
