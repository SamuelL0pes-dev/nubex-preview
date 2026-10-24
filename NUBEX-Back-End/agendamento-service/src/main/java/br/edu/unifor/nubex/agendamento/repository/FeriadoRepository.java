package br.edu.unifor.nubex.agendamento.repository;

import br.edu.unifor.nubex.agendamento.model.Feriado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeriadoRepository extends JpaRepository<Feriado, Long> {
    List<Feriado> findAllByOrderByDataAsc();
}
