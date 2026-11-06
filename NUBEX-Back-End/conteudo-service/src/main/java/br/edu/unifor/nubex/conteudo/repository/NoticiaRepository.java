package br.edu.unifor.nubex.conteudo.repository;

import br.edu.unifor.nubex.conteudo.model.Noticia;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface NoticiaRepository extends JpaRepository<Noticia, Long> {

    List<Noticia> findByAtivoTrueAndDataPublicacaoLessThanEqualOrderByDataPublicacaoDesc(
        LocalDate hoje, Pageable paginacao);
}
