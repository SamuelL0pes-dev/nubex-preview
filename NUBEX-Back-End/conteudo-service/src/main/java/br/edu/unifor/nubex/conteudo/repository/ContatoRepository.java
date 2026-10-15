package br.edu.unifor.nubex.conteudo.repository;

import br.edu.unifor.nubex.conteudo.model.Contato;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContatoRepository extends JpaRepository<Contato, Long> {
}
