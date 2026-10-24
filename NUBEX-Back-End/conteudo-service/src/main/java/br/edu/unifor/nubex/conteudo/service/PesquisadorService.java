package br.edu.unifor.nubex.conteudo.service;

import br.edu.unifor.nubex.conteudo.dto.PesquisadorResponse;
import br.edu.unifor.nubex.conteudo.dto.PesquisadorSecaoResponse;
import br.edu.unifor.nubex.conteudo.mapper.PesquisadorMapper;
import br.edu.unifor.nubex.conteudo.model.Pesquisador;
import br.edu.unifor.nubex.conteudo.model.SecaoPesquisador;
import br.edu.unifor.nubex.conteudo.repository.PesquisadorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PesquisadorService {
    private final PesquisadorRepository repository;
    private final PesquisadorMapper mapper;

    public PesquisadorService(PesquisadorRepository repository, PesquisadorMapper mapper){
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<PesquisadorSecaoResponse> listar(){
        Map<SecaoPesquisador, List<PesquisadorResponse>> porSecao = repository.findByAtivoTrueOrderByNomeAsc().stream().collect(Collectors.groupingBy(Pesquisador::getSecao,() -> new EnumMap<>(SecaoPesquisador.class), Collectors.mapping(mapper::toResponse, Collectors.toList())));
        return porSecao.entrySet().stream().map(grupo -> new PesquisadorSecaoResponse(grupo.getKey(), grupo.getValue())).toList();
    }
}
