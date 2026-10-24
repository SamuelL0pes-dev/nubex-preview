package br.edu.unifor.nubex.catalogo.service;

import br.edu.unifor.nubex.catalogo.dto.ServicoResponse;
import br.edu.unifor.nubex.catalogo.mapper.ServicoMapper;
import br.edu.unifor.nubex.catalogo.repository.ServicoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ServicoService {
    private final ServicoRepository repository;
    private final ServicoMapper mapper;

    public ServicoService(ServicoRepository repository, ServicoMapper mapper){
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<ServicoResponse> listar(String categoria, Long unidadeId) {
        String filtroCategoria = (categoria == null || categoria.isBlank()) ? null : categoria.trim();

        return repository.buscarAtivos(filtroCategoria, unidadeId).stream().map(mapper::toResponse).toList();
    }
}
