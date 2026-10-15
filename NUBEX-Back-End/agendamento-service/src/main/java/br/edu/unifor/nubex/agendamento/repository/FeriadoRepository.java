package br.edu.unifor.nubex.agendamento.repository;

import br.edu.unifor.nubex.agendamento.model.Feriado;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeriadoRepository extends JpaRepository<Feriado, Long> {
}
