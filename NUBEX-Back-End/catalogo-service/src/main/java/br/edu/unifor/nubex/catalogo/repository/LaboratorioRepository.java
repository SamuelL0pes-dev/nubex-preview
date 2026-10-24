package br.edu.unifor.nubex.catalogo.repository;

import br.edu.unifor.nubex.catalogo.model.Laboratorio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LaboratorioRepository extends JpaRepository<Laboratorio, Long> {
    List<Laboratorio> findByUnidadeIdAndAtivoTrueAndUnidadeAtivoTrueOrderByNomeAsc(Long unidadeId);

    List<Laboratorio> findByAtivoTrueAndUnidadeAtivoTrueOrderByNomeAsc();

    Optional<Laboratorio> findByIdAndAtivoTrueAndUnidadeAtivoTrue(Long id);
}
