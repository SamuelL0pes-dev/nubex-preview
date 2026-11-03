package br.edu.unifor.nubex.agendamento.controller;

import br.edu.unifor.nubex.agendamento.dto.FeriadoRequest;
import br.edu.unifor.nubex.agendamento.dto.FeriadoResponse;
import br.edu.unifor.nubex.agendamento.service.FeriadoService;
import br.edu.unifor.nubex.common.config.NubexCommonAutoConfiguration;
import br.edu.unifor.nubex.common.exception.ConflitoException;
import br.edu.unifor.nubex.common.exception.RecursoNaoEncontradoException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminFeriadoController.class)
@ImportAutoConfiguration(NubexCommonAutoConfiguration.class)
public class AdminFeriadoControllerTest {
    private static final String JSON_VALIDO = """
        {"data": "2026-12-25", "descricao": "Natal"}
        """;

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private FeriadoService service;

    @Test
    void semLoginRetorna401() throws Exception {
        mockMvc.perform(post("/api/admin/feriados")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(JSON_VALIDO))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));

        verifyNoInteractions(service);
    }

    @Test
    @WithMockUser(roles = "SOLICITANTE")
    void perfilSemPermissaoRetorna403() throws Exception {
        mockMvc.perform(post("/api/admin/feriados")
                    .with(user("solicitante").authorities(new SimpleGrantedAuthority("ROLE_SOLICITANTE")))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(JSON_VALIDO))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));

        verifyNoInteractions(service);
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void criarRetorna201ComLocation() throws Exception {
        when(service.criar(any(FeriadoRequest.class))).thenReturn(new FeriadoResponse(7L, LocalDate.of(2026, 12, 25), "Natal"));
        mockMvc.perform(post("/api/admin/feriados")
                    .with(user("admin").authorities(new SimpleGrantedAuthority("ROLE_ADMINISTRADOR")))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(JSON_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/admin/feriados/7"))
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.data").value("2026-12-25"));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void criarSemDescricaoRetorna400() throws Exception{
        mockMvc.perform(post("/api/admin/feriados")
                    .with(user("admin").authorities(new SimpleGrantedAuthority("ROLE_ADMINISTRADOR")))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {"data": "2026-12-25", "descricao": "  "}
                            """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verifyNoInteractions(service);
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void criarDataRepetidaRetorna409() throws Exception {
        when(service.criar(any(FeriadoRequest.class))).thenThrow(new ConflitoException("Já existe um feriado em 2026-12-25"));

        mockMvc.perform(post("/api/admin/feriados")
                    .with(user("admin").authorities(new SimpleGrantedAuthority("ROLE_ADMINISTRADOR")))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(JSON_VALIDO)).andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.mensagem").value("Já existe um feriado em 2026-12-25"));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void excluirRetorna204() throws Exception {
        mockMvc.perform(delete("/api/admin/feriados/7")
                    .with(user("admin").authorities(new SimpleGrantedAuthority("ROLE_ADMINISTRADOR"))))
                .andExpect(status().isNoContent());

        verify(service).excluir(7L);
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void atualizarRetorna200() throws Exception{
        when(service.atualizar(eq(7L), any(FeriadoRequest.class))).thenReturn(new FeriadoResponse(7L, LocalDate.of(2026, 12, 25), "Natal de Jesus"));
        mockMvc.perform(put("/api/admin/feriados/7")
                    .with(user("admin").authorities(new SimpleGrantedAuthority("ROLE_ADMINISTRADOR")))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {"data": "2026-12-25", "descricao": "Natal de Jesus"}
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descricao").value("Natal de Jesus"));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void atualizarParaDataDeOutroFeriadoRetorna409() throws Exception {
        when(service.atualizar(eq(7L), any(FeriadoRequest.class))).thenThrow(new ConflitoException("Já existe um feriado em 2026-12-25"));

        mockMvc.perform(put("/api/admin/feriados/7")
                    .with(user("admin").authorities(new SimpleGrantedAuthority("ROLE_ADMINISTRADOR")))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(JSON_VALIDO))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    void excluirIdInexistenteRetorna404() throws Exception {
        doThrow(new RecursoNaoEncontradoException("Feriado não encontrado: 99")).when(service).excluir(99L);

        mockMvc.perform(delete("/api/admin/feriados/99")
                    .with(user("admin").authorities(new SimpleGrantedAuthority("ROLE_ADMINISTRADOR"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.mensagem").value("Feriado não encontrado: 99"));
    }

    @Test
    void atualizarIdInexistenteRetorna404() throws Exception {
        when(service.atualizar(eq(99L), any(FeriadoRequest.class))).thenThrow(new RecursoNaoEncontradoException("Feriado não encontrado: 99"));

        mockMvc.perform(put("/api/admin/feriados/99")
                    .with(user("admin").authorities(new SimpleGrantedAuthority("ROLE_ADMINISTRADOR")))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(JSON_VALIDO))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.mensagem").value("Feriado não encontrado: 99"));
    }

    @Test
    void criarDescricaoCom121CaracteresRetorna400() throws Exception {
        String json = """
                {"data": "2026-12-25", "descricao": "%s"}
                """.formatted("a".repeat(121));

        mockMvc.perform(post("/api/admin/feriados")
                    .with(user("admin").authorities(new SimpleGrantedAuthority("ROLE_ADMINISTRADOR")))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}
