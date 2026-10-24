package br.edu.unifor.nubex.conteudo.controller;

import br.edu.unifor.nubex.conteudo.dto.PesquisadorResponse;
import br.edu.unifor.nubex.conteudo.dto.PesquisadorSecaoResponse;
import br.edu.unifor.nubex.conteudo.model.SecaoPesquisador;
import br.edu.unifor.nubex.conteudo.service.PesquisadorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PesquisadorController.class)
public class PesquisadorControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private PesquisadorService service;

    @Test
    void listarRetornaOsPesquisadoresAgrupadosPorSecao() throws Exception{
        when(service.listar()).thenReturn(List.of(
                new PesquisadorSecaoResponse(SecaoPesquisador.COORDENACAO, List.of(
                        new PesquisadorResponse(1L, "Ana Coordenadora", "Coordenadora", "Biologia Molecular", null, null, "http://lattes.cnpq.br/0000000000000000", null, null))),
                new PesquisadorSecaoResponse(SecaoPesquisador.INICIACAO_CIENTIFICA, List.of(
                        new PesquisadorResponse(2L, "Zeca Aluno", "Bolsista IC", "Genômica", null, null, null, null, null)))));
        mockMvc.perform(get("/api/pesquisadores"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].secao").value("COORDENACAO"))
                .andExpect(jsonPath("$[0].pesquisadores[0].nome").value("Ana Coordenadora"))
                .andExpect(jsonPath("$[0].pesquisadores[0].cargo").value("Coordenadora"))
                .andExpect(jsonPath("$[0].pesquisadores[0].linhaPesquisa").value("Biologia Molecular"))
                .andExpect(jsonPath("$[0].pesquisadores[0].lattesUrl").value("http://lattes.cnpq.br/0000000000000000"))
                .andExpect(jsonPath("$[1].secao").value("INICIACAO_CIENTIFICA"))
                .andExpect(jsonPath("$[1].pesquisadores[0].nome").value("Zeca Aluno"))
                .andExpect(jsonPath("$[1].pesquisadores[0].cargo").value("Bolsista IC"))
                .andExpect(jsonPath("$[1].pesquisadores[0].linhaPesquisa").value("Genômica"));
    }

    @Test
    void listarSemPesquisadoresRetornaListaVazia() throws Exception{
        when(service.listar()).thenReturn(List.of());
        mockMvc.perform(get("/api/pesquisadores"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
