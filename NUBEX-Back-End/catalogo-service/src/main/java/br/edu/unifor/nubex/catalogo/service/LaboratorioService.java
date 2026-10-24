package br.edu.unifor.nubex.catalogo.service;

import br.edu.unifor.nubex.catalogo.dto.LaboratorioDetalheResponse;
import br.edu.unifor.nubex.catalogo.dto.LaboratorioResponse;
import br.edu.unifor.nubex.catalogo.mapper.LaboratorioMapper;
import br.edu.unifor.nubex.catalogo.model.Equipamento;
import br.edu.unifor.nubex.catalogo.model.Laboratorio;
import br.edu.unifor.nubex.catalogo.repository.EquipamentoRepository;
import br.edu.unifor.nubex.catalogo.repository.LaboratorioRepository;
import br.edu.unifor.nubex.common.exception.RecursoNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LaboratorioService {
    private final LaboratorioRepository laboratorioRepository;
    private final EquipamentoRepository equipamentoRepository;
    private final LaboratorioMapper mapper;

    public LaboratorioService(LaboratorioRepository laboratorioRepository, EquipamentoRepository equipamentoRepository, LaboratorioMapper mapper){
        this.laboratorioRepository = laboratorioRepository;
        this.equipamentoRepository = equipamentoRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<LaboratorioResponse> listar(Long unidadeId){
        List<Laboratorio> laboratorios = (unidadeId == null)? laboratorioRepository.findByAtivoTrueAndUnidadeAtivoTrueOrderByNomeAsc() : laboratorioRepository.findByUnidadeIdAndAtivoTrueAndUnidadeAtivoTrueOrderByNomeAsc(unidadeId);
        return laboratorios.stream().map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public LaboratorioDetalheResponse buscarPorId(Long id){
        Laboratorio laboratorio = laboratorioRepository.findByIdAndAtivoTrueAndUnidadeAtivoTrue(id).orElseThrow(() -> new RecursoNaoEncontradoException("Laboratório não encontrado: " + id));
        List<Equipamento> equipamentos = equipamentoRepository.findByLaboratorioIdAndAtivoTrueOrderByNomeAsc(id);
        return mapper.toDetalhe(laboratorio, equipamentos);
    }
}
