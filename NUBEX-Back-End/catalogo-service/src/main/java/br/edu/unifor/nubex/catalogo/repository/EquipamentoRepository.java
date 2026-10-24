package br.edu.unifor.nubex.catalogo.repository;

import br.edu.unifor.nubex.catalogo.model.Equipamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface EquipamentoRepository extends JpaRepository<Equipamento, Long> {
    List<Equipamento> findByLaboratorioIdInAndAtivoTrueOrderByNomeAsc(Collection<Long> laboratorioIds);

    List<Equipamento> findByLaboratorioIdAndAtivoTrueOrderByNomeAsc(Long laboratorioId);

    @Query("""
            SELECT e FROM Equipamento e 
            WHERE e.ativo = true
                AND e.laboratorio.ativo = true
                AND e.laboratorio.unidade.ativo = true
                AND (:laboratorioId IS NULL OR e.laboratorio.id = :laboratorioId)
                AND (:unidadeId IS NULL OR e.laboratorio.unidade.id = :unidadeId)    
            ORDER BY e.nome
            """)
    List<Equipamento> buscarAtivos(@Param("laboratorioId") Long laboratorioId, @Param("unidadeId") Long unidadeId);

    Optional<Equipamento> findByIdAndAtivoTrueAndLaboratorioAtivoTrueAndLaboratorioUnidadeAtivoTrue(Long id);
}
