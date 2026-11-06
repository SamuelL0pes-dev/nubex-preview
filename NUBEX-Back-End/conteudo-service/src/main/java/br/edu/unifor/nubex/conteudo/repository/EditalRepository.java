package br.edu.unifor.nubex.conteudo.repository;

import br.edu.unifor.nubex.conteudo.model.Edital;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface EditalRepository extends JpaRepository<Edital, Long> {

    @Query("""
            select e from Edital e
             where e.ativo = true
               and e.dataPublicacao <= :hoje
               and e.dataEncerramento >= :hoje
             order by e.dataPublicacao desc
            """)
    List<Edital> buscarAbertos(@Param("hoje") LocalDate hoje, Pageable paginacao);
}
