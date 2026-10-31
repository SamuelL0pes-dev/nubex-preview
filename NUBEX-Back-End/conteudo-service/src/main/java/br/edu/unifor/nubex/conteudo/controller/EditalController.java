package br.edu.unifor.nubex.conteudo.controller;

import br.edu.unifor.nubex.conteudo.dto.EditalResumo;
import br.edu.unifor.nubex.conteudo.repository.EditalRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/editais")
public class EditalController {

    private final EditalRepository editalRepository;

    public EditalController(EditalRepository editalRepository) {
        this.editalRepository = editalRepository;
    }

    @GetMapping("/aberto")
    public ResponseEntity<EditalResumo> buscarAberto() {
        return editalRepository
                .findFirstByAtivoTrueAndDataEncerramentoGreaterThanEqualOrderByDataPublicacaoDesc(
                        LocalDate.now())
                .map(EditalResumo::de)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }
}
