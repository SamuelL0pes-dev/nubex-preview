package br.edu.unifor.nubex.catalogo.repository;

import br.edu.unifor.nubex.catalogo.model.Servico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ServicoRepository extends JpaRepository<Servico, Long> {

    @Query("""
            SELECT s FROM Servico s
            JOIN FETCH s.unidade u
            WHERE s.ativo = true
                AND u.ativo = true
                AND (:categoria IS NULL OR s.categoria = :categoria)
                AND (:unidadeId IS NULL OR u.id = :unidadeId)
            ORDER BY s.categoria, u.nome, u.id
            """)
    List<Servico> buscarAtivos(@Param("categoria") String categoria, @Param("unidadeId") Long unidadeId);
}
