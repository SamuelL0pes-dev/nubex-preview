package br.edu.unifor.nubex.catalogo.controller;

import br.edu.unifor.nubex.catalogo.dto.EquipamentoResponse;
import br.edu.unifor.nubex.catalogo.dto.EquipamentoStatusResponse;
import br.edu.unifor.nubex.catalogo.service.EquipamentoService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/equipamentos")
public class EquipamentoController {
    private final EquipamentoService service;

    public EquipamentoController(EquipamentoService service){
        this.service = service;
    }

    @GetMapping
    public List<EquipamentoResponse> listar(@RequestParam(required = false) Long laboratorioId, @RequestParam(required = false) Long unidadeId){
        return service.listar(laboratorioId, unidadeId);
    }

    @GetMapping("/{id}")
    public EquipamentoResponse buscar(@PathVariable Long id){
        return service.buscarPorId(id);
    }

    @GetMapping("/{id}/status")
    public EquipamentoStatusResponse buscarStatus(@PathVariable Long id){
        return service.buscarStatus(id);
    }
}
