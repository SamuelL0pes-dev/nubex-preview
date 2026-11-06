package br.edu.unifor.nubex.conteudo.controller;

import br.edu.unifor.nubex.conteudo.dto.EditalResumo;
import br.edu.unifor.nubex.conteudo.service.EditalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/editais")
public class EditalController {
    private final EditalService service;

    public EditalController (EditalService service){
        this.service = service;
    }

    @GetMapping("/aberto")
    public ResponseEntity<EditalResumo> buscarAberto(){
        return service.buscarAberto()
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }
}
