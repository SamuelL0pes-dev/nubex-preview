package br.edu.unifor.nubex.agendamento.controller;

import br.edu.unifor.nubex.agendamento.dto.FeriadoResponse;
import br.edu.unifor.nubex.agendamento.service.FeriadoService;
import br.edu.unifor.nubex.common.config.NubexCommonAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FeriadoController.class)
@ImportAutoConfiguration(NubexCommonAutoConfiguration.class)
public class FeriadoControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private FeriadoService service;

    @Test
    void listarRetornaAsDatasNoFormatoAnoMesDia() throws Exception {
        when(service.listar()).thenReturn(List.of(
                new FeriadoResponse(1L, LocalDate.of(2026, 11, 2), "Finados"),
                new FeriadoResponse(2L, LocalDate.of(2026, 12, 25), "Natal")));
        mockMvc.perform(get("/api/feriados"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].data").value("2026-11-02"))
                .andExpect(jsonPath("$[0].descricao").value("Finados"))
                .andExpect(jsonPath("$[1].data").value("2026-12-25"))
                .andExpect(jsonPath("$[1].descricao").value("Natal"));
    }

    @Test
    void listarSemFeriadosRetornaListaVazia() throws Exception{
        when(service.listar()).thenReturn(List.of());
        mockMvc.perform(get("/api/feriados"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
