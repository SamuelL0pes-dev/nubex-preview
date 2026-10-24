package br.edu.unifor.nubex.conteudo.controller;

import br.edu.unifor.nubex.conteudo.dto.FaqCategoriaResponse;
import br.edu.unifor.nubex.conteudo.service.FaqService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/faq")
public class FaqController {
    private final FaqService service;

    public FaqController (FaqService service){
        this.service = service;
    }

    @GetMapping
    public List<FaqCategoriaResponse> listar(){
        return service.listar();
    }
}
