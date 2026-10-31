package br.edu.unifor.nubex.conteudo.repository;

import br.edu.unifor.nubex.conteudo.model.Edital;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface EditalRepository extends JpaRepository<Edital, Long> {

    Optional<Edital> findFirstByAtivoTrueAndDataEncerramentoGreaterThanEqualOrderByDataPublicacaoDesc(
        LocalDate hoje);
}
