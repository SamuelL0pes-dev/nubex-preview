package br.edu.unifor.nubex.catalogo.repository;

import br.edu.unifor.nubex.catalogo.model.UnidadeEstrategica;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UnidadeEstrategicaRepository extends JpaRepository<UnidadeEstrategica, Long> {

    List<UnidadeEstrategica> findByAtivoTrueOrderByIdAsc();
    Optional<UnidadeEstrategica> findBySlugAndAtivoTrue(String slug);
}
