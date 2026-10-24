package br.edu.unifor.nubex.catalogo.controller;

import br.edu.unifor.nubex.catalogo.dto.LaboratorioDetalheResponse;
import br.edu.unifor.nubex.catalogo.dto.LaboratorioResponse;
import br.edu.unifor.nubex.catalogo.service.LaboratorioService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/laboratorios")
public class LaboratorioController {
    private final LaboratorioService service;

    public LaboratorioController(LaboratorioService service){
        this.service = service;
    }

    @GetMapping
    public List<LaboratorioResponse> listar(@RequestParam(required = false) Long unidadeId){
        return service.listar(unidadeId);
    }

    @GetMapping("/{id}")
    public LaboratorioDetalheResponse buscar(@PathVariable Long id){
        return service.buscarPorId(id);
    }
}
