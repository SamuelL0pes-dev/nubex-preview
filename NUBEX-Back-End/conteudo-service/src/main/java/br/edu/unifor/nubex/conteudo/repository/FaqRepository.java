package br.edu.unifor.nubex.conteudo.repository;

import br.edu.unifor.nubex.conteudo.model.Faq;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FaqRepository extends JpaRepository<Faq, Long> {
}
