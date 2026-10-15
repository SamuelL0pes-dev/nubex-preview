package br.edu.unifor.nubex.catalogo.repository;

import br.edu.unifor.nubex.catalogo.model.Servico;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServicoRepository extends JpaRepository<Servico, Long> {
}
