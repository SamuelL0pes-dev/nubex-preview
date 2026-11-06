package br.edu.unifor.nubex.conteudo.service;

import br.edu.unifor.nubex.common.util.FusoNubex;
import br.edu.unifor.nubex.conteudo.dto.EditalResumo;
import br.edu.unifor.nubex.conteudo.mapper.EditalMapper;
import br.edu.unifor.nubex.conteudo.repository.EditalRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class EditalService {

    private final EditalRepository repository;
    private final EditalMapper mapper;

    public EditalService(EditalRepository repository, EditalMapper mapper){
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public Optional<EditalResumo> buscarAberto(){
        return repository
                .buscarAbertos(FusoNubex.hoje(), PageRequest.of(0, 1))
                .stream()
                .findFirst()
                .map(mapper::toResponse);
    }
}
