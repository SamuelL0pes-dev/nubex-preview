package br.edu.unifor.nubex.catalogo.controller;

import br.edu.unifor.nubex.catalogo.dto.ServicoResponse;
import br.edu.unifor.nubex.catalogo.service.ServicoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ServicoController.class)
class ServicoControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private ServicoService service;

    @Test
    void listarSemFiltroRetornaServicosComAUnidade() throws Exception{
        when(service.listar(null, null)).thenReturn(List.of(new ServicoResponse(1L, 1L, "Produção de Animais", "producao-animais","Diagnóstico Molecular", "RT-PCR, qPCR", "Detecção de patógenos")));
        mockMvc.perform(get("/api/servicos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].categoria").value("Diagnóstico Molecular"))
                .andExpect(jsonPath("$[0].unidadeId").value(1))
                .andExpect(jsonPath("$[0].unidadeNome").value("Produção de Animais"))
                .andExpect(jsonPath("$[0].unidadeSlug").value("producao-animais"));

        verify(service).listar(null, null);
    }

    @Test
    void listarPorCategoriaComAcentoEEspacoRepassaOTextoCerto() throws Exception {
        when(service.listar("Diagnóstico Molecular", null)).thenReturn(List.of());
        mockMvc.perform(get("/api/servicos").param("categoria", "Diagnóstico Molecular"))
                .andExpect(status().isOk());

        verify(service).listar("Diagnóstico Molecular", null);
    }

    @Test
    void listarComOsDoisFiltrosRepassaOsDois() throws Exception {
        when(service.listar("Proteômica e Espectrometria", 2L)).thenReturn(List.of());
        mockMvc.perform(get("/api/servicos")
                        .param("categoria", "Proteômica e Espectrometria")
                        .param("unidadeId", "2"))
                .andExpect(status().isOk());

        verify(service).listar("Proteômica e Espectrometria", 2L);
    }
}
