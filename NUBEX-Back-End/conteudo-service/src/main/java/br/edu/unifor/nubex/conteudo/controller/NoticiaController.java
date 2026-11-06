package br.edu.unifor.nubex.conteudo.controller;

import br.edu.unifor.nubex.conteudo.dto.NoticiaResumo;
import br.edu.unifor.nubex.conteudo.service.NoticiaService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/noticias")
public class NoticiaController {
    private final NoticiaService service;

    public NoticiaController (NoticiaService service){
        this.service = service;
    }

    @GetMapping
    public List<NoticiaResumo> listar(@RequestParam(defaultValue = "3") int limite){
        return service.listar(limite);
    }
}
