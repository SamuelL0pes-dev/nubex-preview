package br.edu.unifor.nubex.agendamento.controller;

import br.edu.unifor.nubex.agendamento.dto.FeriadoResponse;
import br.edu.unifor.nubex.agendamento.service.FeriadoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/feriados")
public class FeriadoController {
    private final FeriadoService service;

    public FeriadoController(FeriadoService service){
        this.service = service;
    }

    @GetMapping
    public List<FeriadoResponse> listar(){
        return service.listar();
    }
}
