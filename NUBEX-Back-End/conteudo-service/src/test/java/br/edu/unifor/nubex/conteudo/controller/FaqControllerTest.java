package br.edu.unifor.nubex.conteudo.controller;

import br.edu.unifor.nubex.conteudo.dto.FaqCategoriaResponse;
import br.edu.unifor.nubex.conteudo.dto.FaqResponse;
import br.edu.unifor.nubex.conteudo.service.FaqService;
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

@WebMvcTest(FaqController.class)
public class FaqControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private FaqService service;

    @Test
    void listarRetornaAsPerguntasAgrupadasPorCategoria() throws Exception{
        when(service.listar()).thenReturn(List.of(
                new FaqCategoriaResponse("Acesso Multiusuário", List.of(
                        new FaqResponse(2L, "Quem pode agendar?", "Solicitantes internos e externos aprovados."))),
                new FaqCategoriaResponse("Biossegurança", List.of(
                        new FaqResponse(1L, "Preciso de treinamento?", "Sim, o credenciamento é obrigatório.")))));
        mockMvc.perform(get("/api/faq"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].categoria").value("Acesso Multiusuário"))
                .andExpect(jsonPath("$[0].perguntas.length()").value(1))
                .andExpect(jsonPath("$[0].perguntas[0].pergunta").value("Quem pode agendar?"))
                .andExpect(jsonPath("$[0].perguntas[0].resposta").value("Solicitantes internos e externos aprovados."))
                .andExpect(jsonPath("$[1].categoria").value("Biossegurança"))
                .andExpect(jsonPath("$[1].perguntas.length()").value(1))
                .andExpect(jsonPath("$[1].perguntas[0].pergunta").value("Preciso de treinamento?"))
                .andExpect(jsonPath("$[1].perguntas[0].resposta").value("Sim, o credenciamento é obrigatório."));
    }

    @Test
    void listarSemPerguntasRetornaListaVazia() throws Exception{
        when(service.listar()).thenReturn(List.of());
        mockMvc.perform(get("/api/faq"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
