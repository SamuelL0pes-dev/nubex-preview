package br.edu.unifor.nubex.catalogo.controller;

import br.edu.unifor.nubex.catalogo.dto.UnidadeDetalheResponse;
import br.edu.unifor.nubex.catalogo.dto.UnidadeResumoResponse;
import br.edu.unifor.nubex.catalogo.service.UnidadeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/unidades")
public class UnidadeController {
    private final UnidadeService service;

    public UnidadeController(UnidadeService service){
        this.service = service;
    }

    @GetMapping
    public List<UnidadeResumoResponse> listar(){
        return service.listarAtivas();
    }

    @GetMapping("/{slug}")
    public UnidadeDetalheResponse buscar(@PathVariable String slug){
        return service.buscarPorSlug(slug);
    }
}
