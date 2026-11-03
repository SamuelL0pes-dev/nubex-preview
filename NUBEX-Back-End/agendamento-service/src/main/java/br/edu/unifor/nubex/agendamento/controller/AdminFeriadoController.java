package br.edu.unifor.nubex.agendamento.controller;

import br.edu.unifor.nubex.agendamento.dto.FeriadoRequest;
import br.edu.unifor.nubex.agendamento.dto.FeriadoResponse;
import br.edu.unifor.nubex.agendamento.service.FeriadoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/admin/feriados")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class AdminFeriadoController {
    private final FeriadoService service;

    public AdminFeriadoController(FeriadoService service){
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<FeriadoResponse> criar(@Valid @RequestBody FeriadoRequest request){
        FeriadoResponse criado = service.criar(request);
        URI local = ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/admin/feriados/{id}").buildAndExpand(criado.id()).toUri();

        return ResponseEntity.created(local).body(criado);
    }

    @PutMapping("/{id}")
    public FeriadoResponse atualizar(@PathVariable("id") Long id, @Valid @RequestBody FeriadoRequest request){
        return service.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable("id") Long id){
        service.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
