package br.edu.unifor.nubex.catalogo.service;


import br.edu.unifor.nubex.catalogo.dto.UnidadeDetalheResponse;
import br.edu.unifor.nubex.catalogo.dto.UnidadeResumoResponse;
import br.edu.unifor.nubex.catalogo.mapper.UnidadeMapper;
import br.edu.unifor.nubex.catalogo.model.Equipamento;
import br.edu.unifor.nubex.catalogo.model.Laboratorio;
import br.edu.unifor.nubex.catalogo.model.UnidadeEstrategica;
import br.edu.unifor.nubex.catalogo.repository.EquipamentoRepository;
import br.edu.unifor.nubex.catalogo.repository.LaboratorioRepository;
import br.edu.unifor.nubex.catalogo.repository.UnidadeEstrategicaRepository;
import br.edu.unifor.nubex.common.exception.RecursoNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UnidadeService {
    private final UnidadeEstrategicaRepository unidadeRepository;
    private final LaboratorioRepository laboratorioRepository;
    private final EquipamentoRepository equipamentoRepository;
    private final UnidadeMapper mapper;

    public UnidadeService(UnidadeEstrategicaRepository unidadeRepository, LaboratorioRepository laboratorioRepository, EquipamentoRepository equipamentoRepository, UnidadeMapper mapper){
        this.unidadeRepository = unidadeRepository;
        this.laboratorioRepository =  laboratorioRepository;
        this.equipamentoRepository = equipamentoRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<UnidadeResumoResponse> listarAtivas(){
        return unidadeRepository.findByAtivoTrueOrderByIdAsc().stream().map(mapper::toResumo).toList();
    }

    @Transactional(readOnly = true)
    public UnidadeDetalheResponse buscarPorSlug(String slug){
        UnidadeEstrategica unidade = unidadeRepository.findBySlugAndAtivoTrue(slug).orElseThrow(() -> new RecursoNaoEncontradoException("Unidade não encontrada: " + slug));

        List<Laboratorio> laboratorios = laboratorioRepository.findByUnidadeIdAndAtivoTrueAndUnidadeAtivoTrueOrderByNomeAsc(unidade.getId());

        List<Long> idsLaboratorios = laboratorios.stream().map(Laboratorio::getId).toList();

        Map<Long, List<Equipamento>> equipamentosPorLaboratorio = idsLaboratorios.isEmpty() ? Map.of()
                : equipamentoRepository.findByLaboratorioIdInAndAtivoTrueOrderByNomeAsc(idsLaboratorios).stream().collect(Collectors.groupingBy(e -> e.getLaboratorio().getId()));

        return mapper.toDetalhe(unidade, laboratorios, equipamentosPorLaboratorio);
    }

    @Transactional(readOnly = true)
    public UnidadeResumoResponse buscarPorId(Long id){
        return unidadeRepository.findByIdAndAtivoTrue(id).map(mapper::toResumo).orElseThrow(() -> new RecursoNaoEncontradoException("Unidade não encontrada: " + id));
    }
}
