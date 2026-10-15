package br.edu.unifor.nubex.conteudo.repository;

import br.edu.unifor.nubex.conteudo.model.Pesquisador;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PesquisadorRepository extends JpaRepository<Pesquisador, Long> {
}
