package br.edu.unifor.nubex.catalogo.service;

import br.edu.unifor.nubex.catalogo.dto.EquipamentoResponse;
import br.edu.unifor.nubex.catalogo.dto.EquipamentoStatusResponse;
import br.edu.unifor.nubex.catalogo.mapper.EquipamentoMapper;
import br.edu.unifor.nubex.catalogo.model.Equipamento;
import br.edu.unifor.nubex.catalogo.repository.EquipamentoRepository;
import br.edu.unifor.nubex.common.exception.RecursoNaoEncontradoException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EquipamentoService {
    private final EquipamentoRepository repository;
    private final EquipamentoMapper mapper;

    public EquipamentoService(EquipamentoRepository repository, EquipamentoMapper mapper){
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<EquipamentoResponse> listar(Long laboratorioId, Long unidadeId){
        return repository.buscarAtivos(laboratorioId, unidadeId).stream().map(mapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    private Equipamento buscarAtivo(Long id){
        return repository.findByIdAndAtivoTrueAndLaboratorioAtivoTrueAndLaboratorioUnidadeAtivoTrue(id).orElseThrow(() -> new RecursoNaoEncontradoException("Equipamento não encontrado: " + id));
    }

    @Transactional(readOnly = true)
    public EquipamentoResponse buscarPorId(Long id){
        return mapper.toResponse(buscarAtivo(id));
    }

    @Transactional(readOnly = true)
    public EquipamentoStatusResponse buscarStatus(Long id){
        return mapper.toStatus(buscarAtivo(id));
    }
}
