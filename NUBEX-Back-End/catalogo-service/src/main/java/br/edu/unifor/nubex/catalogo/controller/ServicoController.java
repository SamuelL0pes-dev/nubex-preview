package br.edu.unifor.nubex.catalogo.controller;

import br.edu.unifor.nubex.catalogo.dto.ServicoResponse;
import br.edu.unifor.nubex.catalogo.service.ServicoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/servicos")
public class ServicoController {
    private final ServicoService service;

    public ServicoController(ServicoService service){
        this.service = service;
    }

    @GetMapping
    public List<ServicoResponse> listar(@RequestParam(required = false) String categoria, @RequestParam(required = false) Long unidadeId) {
        return service.listar(categoria, unidadeId);
    }
}
