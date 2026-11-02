package br.edu.unifor.nubex.catalogo.controller;

import br.edu.unifor.nubex.catalogo.dto.UnidadeDetalheResponse;
import br.edu.unifor.nubex.catalogo.dto.UnidadeResumoResponse;
import br.edu.unifor.nubex.catalogo.service.UnidadeService;
import br.edu.unifor.nubex.common.exception.RecursoNaoEncontradoException;
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

@WebMvcTest(UnidadeController.class)
class UnidadeControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private UnidadeService service;

    @Test
    void listarRetornaAsUnidades() throws Exception{
        when(service.listarAtivas()).thenReturn(List.of(new UnidadeResumoResponse(1L, "Produção de Animais", "producao-animais",null, null)));
        mockMvc.perform(get("/api/unidades"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nome").value("Produção de Animais"))
                .andExpect(jsonPath("$[0].slug").value("producao-animais"));
    }

    @Test
    void buscarSlugInexistenteRetorna404() throws Exception{
        when(service.buscarPorSlug("nao-existe")).thenThrow(new RecursoNaoEncontradoException("Unidade não encontrada: nao-existe"));
        mockMvc.perform(get("/api/unidades/nao-existe"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.mensagem").value("Unidade não encontrada: nao-existe"));
    }

    @Test
    void buscarPorIdRetornaAUnidade() throws Exception {
        when(service.buscarPorId(1L)).thenReturn(new UnidadeResumoResponse(1L, "Produção de Animais", "producao-animais", null, null));
        mockMvc.perform(get("/api/unidades/1/resumo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Produção de Animais"))
                .andExpect(jsonPath("$.slug").value("producao-animais"));
    }

    @Test
    void buscarPorIdInexistenteRetorna404() throws Exception {
        when(service.buscarPorId(999L)).thenThrow(new RecursoNaoEncontradoException("Unidade não encontrada: 999"));
        mockMvc.perform(get("/api/unidades/999/resumo"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.mensagem").value("Unidade não encontrada: 999"));
    }

    @Test
    void buscarPorSlugContinuaIndoParaODetalhe() throws Exception {
        when(service.buscarPorSlug("producao-animais")).thenReturn(new UnidadeDetalheResponse(1L, "Produção de Animais", "producao-animais", null, null, List.of()));
        mockMvc.perform(get("/api/unidades/producao-animais"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.laboratorios").isArray());
    }

    @Test
    void slugComecandoPorNumeroVaiParaODetalhe() throws Exception {
        when(service.buscarPorSlug("3d-printing")).thenReturn(new UnidadeDetalheResponse(7L, "3D Printing", "3d-printing", null, null, List.of()));
        mockMvc.perform(get("/api/unidades/3d-printing"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(7))
            .andExpect(jsonPath("$.nome").value("3D Printing"))
            .andExpect(jsonPath("$.slug").value("3d-printing"));
        verify(service).buscarPorSlug("3d-printing");
    }
}
