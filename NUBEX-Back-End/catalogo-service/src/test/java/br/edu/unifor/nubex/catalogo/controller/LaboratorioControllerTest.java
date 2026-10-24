package br.edu.unifor.nubex.catalogo.controller;

import br.edu.unifor.nubex.catalogo.dto.LaboratorioResponse;
import br.edu.unifor.nubex.catalogo.service.LaboratorioService;
import br.edu.unifor.nubex.common.exception.RecursoNaoEncontradoException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LaboratorioController.class)
class LaboratorioControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private LaboratorioService service;

    @Test
    void listarSemFiltroPassaNullParaOService() throws Exception{
        when(service.listar(null)).thenReturn(List.of(new LaboratorioResponse(1L, 1L, "Laboratório Teste", null)));
        mockMvc.perform(get("/api/laboratorios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Laboratório Teste"))
                .andExpect(jsonPath("$[0].unidadeId").value(1));

        verify(service).listar(null);
    }
    @Test
    void listarComFiltroRepassaAUnidade() throws Exception {
        when(service.listar(2L)).thenReturn(List.of());
        mockMvc.perform(get("/api/laboratorios").param("unidadeId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(service).listar(2L);
    }
    @Test
    void buscarIdInexistenteRetorna404() throws Exception {
        when(service.buscarPorId(999L)).thenThrow(new RecursoNaoEncontradoException("Laboratório não encontrado: 999"));
        mockMvc.perform(get("/api/laboratorios/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Laboratório não encontrado: 999"));
    }
}
