package br.edu.unifor.nubex.conteudo.service;

import br.edu.unifor.nubex.common.util.FusoNubex;
import br.edu.unifor.nubex.conteudo.dto.NoticiaResumo;
import br.edu.unifor.nubex.conteudo.mapper.NoticiaMapper;
import br.edu.unifor.nubex.conteudo.repository.NoticiaRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NoticiaService {

    private static final int LIMITE_MAXIMO = 20;

    private final NoticiaRepository repository;
    private final NoticiaMapper mapper;

    public NoticiaService(NoticiaRepository repository, NoticiaMapper mapper){
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<NoticiaResumo> listar(int limite){
        int quantidade = Math.min(Math.max(limite, 1), LIMITE_MAXIMO);

        return repository
                .findByAtivoTrueAndDataPublicacaoLessThanEqualOrderByDataPublicacaoDesc(
                    FusoNubex.hoje(), PageRequest.of(0, quantidade))
                .stream()
                .map(mapper::toResponse)
                .toList();
    }
}
