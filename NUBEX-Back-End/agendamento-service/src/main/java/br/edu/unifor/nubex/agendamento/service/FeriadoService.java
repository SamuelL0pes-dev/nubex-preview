package br.edu.unifor.nubex.agendamento.service;

import br.edu.unifor.nubex.agendamento.dto.FeriadoResponse;
import br.edu.unifor.nubex.agendamento.mapper.FeriadoMapper;
import br.edu.unifor.nubex.agendamento.repository.FeriadoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FeriadoService {
    private final FeriadoRepository repository;
    private final FeriadoMapper mapper;

    public FeriadoService(FeriadoRepository repository, FeriadoMapper mapper){
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<FeriadoResponse> listar(){
        return repository.findAllByOrderByDataAsc().stream().map(mapper::toResponse).toList();
    }

}
