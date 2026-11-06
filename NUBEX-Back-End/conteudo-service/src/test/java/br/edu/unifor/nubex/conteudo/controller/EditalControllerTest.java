package br.edu.unifor.nubex.conteudo.controller;

import br.edu.unifor.nubex.conteudo.dto.EditalResumo;
import br.edu.unifor.nubex.conteudo.service.EditalService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EditalController.class)
public class EditalControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private EditalService service;

    @Test
    void comEditalAbertoDevolve200ComOPrazo() throws Exception {
        when(service.buscarAberto()).thenReturn(Optional.of(new EditalResumo(
                7L, "Edital de bolsas", "Resumo do edital.", "Unidade de Bioensaios",
                "/imagens/edital-7.png", "/editais/7",
                LocalDate.of(2026, 10, 10), LocalDate.of(2026, 11, 14), true)));

        mockMvc.perform(get("/api/editais/aberto"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.titulo").value("Edital de bolsas"))
                .andExpect(jsonPath("$.dataEncerramento").value("2026-11-14"))
                .andExpect(jsonPath("$.destaque").value(true));
    }

    @Test
    void semEditalAbertoDevolve204SemCorpo() throws Exception {
        when(service.buscarAberto()).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/editais/aberto"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
    }
}
