package br.edu.unifor.nubex.conteudo.controller;

import br.edu.unifor.nubex.conteudo.dto.NoticiaResumo;
import br.edu.unifor.nubex.conteudo.service.NoticiaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NoticiaController.class)
public class NoticiaControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private NoticiaService service;

    private static final NoticiaResumo EXEMPLO = new NoticiaResumo(
            1L, "Mutirao de biosseguranca", "Resumo da noticia.", "Unidade de Bioensaios",
            "/imagens/noticia-1.png", "/noticias/1", LocalDate.of(2026, 10, 20), false);

    @Test
    void listarDevolveOFormatoEsperadoDoJson() throws Exception {
        when(service.listar(3)).thenReturn(List.of(EXEMPLO));

        mockMvc.perform(get("/api/noticias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].titulo").value("Mutirao de biosseguranca"))
                .andExpect(jsonPath("$[0].resumo").value("Resumo da noticia."))
                .andExpect(jsonPath("$[0].unidade").value("Unidade de Bioensaios"))
                .andExpect(jsonPath("$[0].imagemUrl").value("/imagens/noticia-1.png"))
                .andExpect(jsonPath("$[0].link").value("/noticias/1"))
                .andExpect(jsonPath("$[0].dataPublicacao").value("2026-10-20"))
                .andExpect(jsonPath("$[0].destaque").value(false));
    }

    @Test
    void semLimiteNaUrlOPadraoEhTres() throws Exception {
        when(service.listar(anyInt())).thenReturn(List.of());

        mockMvc.perform(get("/api/noticias"))
                .andExpect(status().isOk());

        verify(service).listar(3);
    }

    @Test
    void limiteQueNaoEhNumeroRetorna400() throws Exception {
        mockMvc.perform(get("/api/noticias").param("limite", "abc"))
                .andExpect(status().isBadRequest());
    }
}
