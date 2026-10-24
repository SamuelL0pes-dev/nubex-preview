package br.edu.unifor.nubex.conteudo.controller;

import br.edu.unifor.nubex.conteudo.dto.PesquisadorSecaoResponse;
import br.edu.unifor.nubex.conteudo.service.PesquisadorService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/pesquisadores")
public class PesquisadorController {
    private final PesquisadorService service;

    public PesquisadorController(PesquisadorService service){
        this.service = service;
    }

    @GetMapping
    public List<PesquisadorSecaoResponse> listar(){
        return service.listar();
    }
}
