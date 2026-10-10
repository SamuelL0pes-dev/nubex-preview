package br.edu.unifor.nubex.common.exception;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void preparar() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ControllerFalso())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void recursoNaoEncontradoRetorna404() throws Exception {
        mockMvc.perform(get("/falso/nao-encontrado"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.mensagem").value("Usuário não encontrado"));
    }

    @Test
    void conflitoRetorna409() throws Exception {
        mockMvc.perform(get("/falso/conflito"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem").value("E-mail já cadastrado"));
    }

    @Test
    void validacaoRetornaCamposInvalidos() throws Exception {
        mockMvc.perform(post("/falso/validar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos[0].campo").value("nome"))
                .andExpect(jsonPath("$.campos[0].mensagem").value("O nome é obrigatório"));
    }

    @Test
    void erroInesperadoRetorna500SemVazarDetalhes() throws Exception {
        mockMvc.perform(get("/falso/erro"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.codigo").exists())
                .andExpect(content().string(not(containsString("segredo"))));
    }

    @RestController
    @RequestMapping("/falso")
    public static class ControllerFalso {

        @GetMapping("/nao-encontrado")
        public String naoEncontrado() {
            throw new RecursoNaoEncontradoException("Usuário não encontrado");
        }

        @GetMapping("/conflito")
        public String conflito() {
            throw new ConflitoException("E-mail já cadastrado");
        }

        @GetMapping("/erro")
        public String erro() {
            throw new IllegalStateException("segredo interno");
        }

        @PostMapping("/validar")
        public String validar(@Valid @RequestBody Pedido pedido) {
            return "ok";
        }

        public record Pedido(@NotBlank(message = "O nome é obrigatório") String nome) {
        }
    }
}
