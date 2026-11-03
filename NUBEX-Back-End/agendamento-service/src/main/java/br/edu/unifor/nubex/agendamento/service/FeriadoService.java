package br.edu.unifor.nubex.agendamento.service;

import br.edu.unifor.nubex.agendamento.dto.FeriadoRequest;
import br.edu.unifor.nubex.agendamento.dto.FeriadoResponse;
import br.edu.unifor.nubex.agendamento.mapper.FeriadoMapper;
import br.edu.unifor.nubex.agendamento.model.Feriado;
import br.edu.unifor.nubex.agendamento.repository.FeriadoRepository;
import br.edu.unifor.nubex.common.exception.ConflitoException;
import br.edu.unifor.nubex.common.exception.RecursoNaoEncontradoException;
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

    @Transactional
    public FeriadoResponse criar(FeriadoRequest request){
        if (repository.existsByData(request.data())){
            throw new ConflitoException("Já existe um feriado em " + request.data());
        }
        Feriado feriado = new Feriado();
        feriado.setData(request.data());
        feriado.setDescricao(request.descricao());

        return mapper.toResponse(repository.save(feriado));
    }

    @Transactional
    public FeriadoResponse atualizar(Long id, FeriadoRequest request){
        Feriado feriado = repository.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Feriado não encontrado: " + id));

        if (repository.existsByDataAndIdNot(request.data(), id)){
            throw new ConflitoException("Já existe um feriado em " + request.data());
        }

        feriado.setData(request.data());
        feriado.setDescricao(request.descricao());
        return mapper.toResponse(feriado);
    }

    @Transactional
    public void excluir(Long id){
        if (!repository.existsById(id)){
            throw new RecursoNaoEncontradoException("Feriado não encontrado: " + id);
        }
        repository.deleteById(id);
    }

}
