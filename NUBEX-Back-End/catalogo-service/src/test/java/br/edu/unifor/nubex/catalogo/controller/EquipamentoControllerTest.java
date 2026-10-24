package br.edu.unifor.nubex.catalogo.controller;

import br.edu.unifor.nubex.catalogo.dto.EquipamentoResponse;
import br.edu.unifor.nubex.catalogo.dto.EquipamentoStatusResponse;
import br.edu.unifor.nubex.catalogo.model.StatusEquipamento;
import br.edu.unifor.nubex.catalogo.service.EquipamentoService;
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

@WebMvcTest(EquipamentoController.class)
public class EquipamentoControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private EquipamentoService service;

    @Test
    void listarSemFiltroPassaNullNosDoisFiltros() throws Exception {
        when(service.listar(null, null)).thenReturn(List.of(new EquipamentoResponse(1L, 1L, "Microscópio Teste", null, StatusEquipamento.DISPONIVEL)));
        mockMvc.perform(get("/api/equipamentos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nome").value("Microscópio Teste"))
                .andExpect(jsonPath("$.[0].laboratorioId").value(1))
                .andExpect(jsonPath("$[0].status").value("DISPONIVEL"));

        verify(service).listar(null, null);
    }

    @Test
    void listarComOsDoisFiltrosRepassaNaOrdemCerta() throws Exception {
        when(service.listar(2L, 1L)).thenReturn(List.of());
        mockMvc.perform(get("/api/equipamentos")
                        .param("laboratorioId", "2")
                        .param("unidadeId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(service).listar(2L, 1L);
    }

    @Test
    void buscarRetornaOEquipamento() throws Exception {
        when(service.buscarPorId(2L)).thenReturn(new EquipamentoResponse(2L, 2L, "Centrífuga Teste", null, StatusEquipamento.MANUTENCAO));
        mockMvc.perform(get("/api/equipamentos/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.nome").value("Centrífuga Teste"));
    }

    @Test
    void buscarStatusRetornaSoOStatus() throws Exception {
        when(service.buscarStatus(2L)).thenReturn(new EquipamentoStatusResponse(2L, StatusEquipamento.MANUTENCAO));
        mockMvc.perform(get("/api/equipamentos/2/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.status").value("MANUTENCAO"));
    }

    @Test
    void buscarIdInexistenteRetorna404() throws Exception {
        when(service.buscarPorId(999L)).thenThrow(new RecursoNaoEncontradoException("Equipamento não encontrado: 999"));
        mockMvc.perform(get("/api/equipamentos/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Equipamento não encontrado: 999"));
    }

    @Test
    void buscarStatusDeIdInexistenteRetorna404() throws Exception {
        when(service.buscarStatus(999L)).thenThrow(new RecursoNaoEncontradoException("Equipamento não encontrado: 999"));
        mockMvc.perform(get("/api/equipamentos/999/status"))
                .andExpect(status().isNotFound());
    }
}
