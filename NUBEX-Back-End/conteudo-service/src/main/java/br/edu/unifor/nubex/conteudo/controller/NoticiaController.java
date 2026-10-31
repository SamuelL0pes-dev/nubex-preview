package br.edu.unifor.nubex.conteudo.controller;

import br.edu.unifor.nubex.conteudo.dto.NoticiaResumo;
import br.edu.unifor.nubex.conteudo.repository.NoticiaRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/noticias")
public class NoticiaController {

    private static final int LIMITE_MAXIMO = 20;

    private final NoticiaRepository noticiaRepository;

    public NoticiaController(NoticiaRepository noticiaRepository) {
        this.noticiaRepository = noticiaRepository;
    }

    @GetMapping
    public ResponseEntity<List<NoticiaResumo>> listar(
            @RequestParam(defaultValue = "3") int limite) {

        int quantidade = Math.min(Math.max(limite, 1), LIMITE_MAXIMO);

        List<NoticiaResumo> noticias = noticiaRepository
                .findByAtivoTrueOrderByDataPublicacaoDesc(PageRequest.of(0, quantidade))
                .stream()
                .map(NoticiaResumo::de)
                .toList();

        return ResponseEntity.ok(noticias);
    }
}
