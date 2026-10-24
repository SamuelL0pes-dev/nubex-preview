package br.edu.unifor.nubex.conteudo.service;

import br.edu.unifor.nubex.conteudo.dto.FaqCategoriaResponse;
import br.edu.unifor.nubex.conteudo.dto.FaqResponse;
import br.edu.unifor.nubex.conteudo.mapper.FaqMapper;
import br.edu.unifor.nubex.conteudo.model.Faq;
import br.edu.unifor.nubex.conteudo.repository.FaqRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FaqService {
    private final FaqRepository repository;
    private final FaqMapper mapper;

    public FaqService(FaqRepository repository, FaqMapper mapper){
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<FaqCategoriaResponse> listar(){
        Map<String, List<FaqResponse>> porCategoria = repository.findByAtivoTrueOrderByOrdemAscIdAsc().stream().collect(Collectors.groupingBy(
                Faq::getCategoria, LinkedHashMap::new, Collectors.mapping(mapper::toResponse, Collectors.toList())
        ));
        return porCategoria.entrySet().stream().map(grupo -> new FaqCategoriaResponse(grupo.getKey(), grupo.getValue())).toList();
    }
}
