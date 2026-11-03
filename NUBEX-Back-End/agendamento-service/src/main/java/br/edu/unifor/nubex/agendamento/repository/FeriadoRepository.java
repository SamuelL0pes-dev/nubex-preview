package br.edu.unifor.nubex.agendamento.repository;

import br.edu.unifor.nubex.agendamento.model.Feriado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface FeriadoRepository extends JpaRepository<Feriado, Long> {
    List<Feriado> findAllByOrderByDataAsc();
    boolean existsByData(LocalDate data);
    boolean existsByDataAndIdNot(LocalDate data, Long id);
}
