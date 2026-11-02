package br.edu.unifor.nubex.usuario;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.edu.unifor.nubex.usuario.model.PedidoAprovacao;
import br.edu.unifor.nubex.usuario.model.PerfilUsuario;
import br.edu.unifor.nubex.usuario.model.StatusPedido;
import br.edu.unifor.nubex.usuario.model.StatusUsuario;
import br.edu.unifor.nubex.usuario.model.TipoUsuario;
import br.edu.unifor.nubex.usuario.model.Usuario;
import br.edu.unifor.nubex.usuario.model.VinculoUsuario;
import br.edu.unifor.nubex.usuario.repository.PedidoAprovacaoRepository;
import br.edu.unifor.nubex.usuario.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Pedido de acesso como solicitante interno (RF052), de ponta a ponta: HTTP de
 * verdade contra um PostgreSQL de verdade, subido pelo Testcontainers. Nada é
 * mockado, então o que passa aqui é o comportamento real da rota.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(ConfiguracaoPostgresTeste.class)
class SolicitacaoAcessoTest {

    private static final String LATTES = "http://lattes.cnpq.br/4928172910492817";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PedidoAprovacaoRepository pedidoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void limparBanco() {
        pedidoRepository.deleteAll();
        usuarioRepository.deleteAll();
    }

    // ---- O caminho que dá certo ----

    @Test
    void registraOPedidoEDevolveOProtocolo() throws Exception {
        mockMvc.perform(post("/api/cadastro/solicitante")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo("ana.ribeiro@edu.unifor.br", "DOCENTE", "senha-de-teste-123")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("AGUARDANDO_APROVACAO"))
                .andExpect(jsonPath("$.protocolo").value(
                        org.hamcrest.Matchers.matchesPattern("^SOL-\\d{4}-\\d{6}$")));

        Usuario criado = usuarioRepository
                .findByEmailIgnoreCase("ana.ribeiro@edu.unifor.br")
                .orElseThrow();

        assertThat(criado.getPerfil()).isEqualTo(PerfilUsuario.SOLICITANTE);
        assertThat(criado.getTipo()).isEqualTo(TipoUsuario.INTERNO);
        assertThat(criado.getStatus()).isEqualTo(StatusUsuario.AGUARDANDO_APROVACAO);
        assertThat(criado.getVinculo()).isEqualTo(VinculoUsuario.DOCENTE);
        assertThat(criado.getLattesUrl()).isEqualTo(LATTES);
    }

    /**
     * O pedido tem de nascer aberto e sem solicitante: pelo Apêndice A, nulo ali
     * significa "a decisão cabe a qualquer administrador", que é o RF052.
     */
    @Test
    void abreOPedidoDeAprovacaoSemSolicitante() throws Exception {
        registrar("bruno.alves@edu.unifor.br");

        Usuario criado = usuarioRepository
                .findByEmailIgnoreCase("bruno.alves@edu.unifor.br")
                .orElseThrow();
        PedidoAprovacao pedido = pedidoRepository
                .findByUsuarioIdAndStatus(criado.getId(), StatusPedido.AGUARDANDO)
                .orElseThrow();

        assertThat(pedido.getStatus()).isEqualTo(StatusPedido.AGUARDANDO);
        assertThat(pedido.getSolicitante()).isNull();
        assertThat(pedido.getDecididoPor()).isNull();
        assertThat(pedido.getCriadoEm()).isNotNull();
    }

    /** A senha nunca vai para o banco em texto. */
    @Test
    void gravaApenasOHashDaSenha() throws Exception {
        registrar("carla.dias@edu.unifor.br");

        Usuario criado = usuarioRepository
                .findByEmailIgnoreCase("carla.dias@edu.unifor.br")
                .orElseThrow();

        assertThat(criado.getSenhaHash()).isNotEqualTo("senha-de-teste-123");
        assertThat(criado.getSenhaHash()).startsWith("$2");
        assertThat(passwordEncoder.matches("senha-de-teste-123", criado.getSenhaHash())).isTrue();
    }

    // ---- O teste mais importante do arquivo ----

    /**
     * O cliente NÃO escolhe o perfil.
     *
     * <p>A tela do portal envia hoje {@code "perfil": "SOLICITANTE"} e
     * {@code "tipo": "INTERNO"} no corpo. Se o servidor obedecesse, bastaria um
     * curl com {@code "perfil": "ADMINISTRADOR"} para criar uma conta de acesso
     * total -- sem login, sem aprovação, sem nada.
     *
     * <p>Aqui mandamos exatamente esse ataque: ADMINISTRADOR e EXTERNO. A conta
     * tem de sair SOLICITANTE e INTERNO de todo jeito.
     */
    @Test
    void ignoraOPerfilEOTipoEnviadosPeloCliente() throws Exception {
        String ataque = """
                {"nome":"Invasor",
                 "email":"invasor@edu.unifor.br",
                 "senha":"senha-de-teste-123",
                 "telefone":"85999990000",
                 "vinculo":"DOCENTE",
                 "lattesUrl":"%s",
                 "perfil":"ADMINISTRADOR",
                 "tipo":"EXTERNO",
                 "status":"ATIVO"}
                """.formatted(LATTES);

        mockMvc.perform(post("/api/cadastro/solicitante")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ataque))
                .andExpect(status().isCreated());

        Usuario criado = usuarioRepository
                .findByEmailIgnoreCase("invasor@edu.unifor.br")
                .orElseThrow();

        assertThat(criado.getPerfil()).isEqualTo(PerfilUsuario.SOLICITANTE);
        assertThat(criado.getTipo()).isEqualTo(TipoUsuario.INTERNO);
        assertThat(criado.getStatus()).isEqualTo(StatusUsuario.AGUARDANDO_APROVACAO);
    }

    // ---- Recusas ----

    @Test
    void emailJaCadastradoRetorna409() throws Exception {
        registrar("duplicada@edu.unifor.br");

        mockMvc.perform(post("/api/cadastro/solicitante")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo("duplicada@edu.unifor.br", "PESQUISADOR", "senha-de-teste-123")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem").value("Já existe uma conta com este e-mail"));

        assertThat(usuarioRepository.count()).isEqualTo(1);
    }

    /** A caixa não importa: o índice único é sobre LOWER(email). */
    @Test
    void emailJaCadastradoEmOutraCaixaTambemRetorna409() throws Exception {
        registrar("elena@edu.unifor.br");

        mockMvc.perform(post("/api/cadastro/solicitante")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo("ELENA@EDU.UNIFOR.BR", "DOCENTE", "senha-de-teste-123")))
                .andExpect(status().isConflict());

        assertThat(usuarioRepository.count()).isEqualTo(1);
    }

    /** O RF052 é para docente ou pesquisador; os outros vínculos são do RF054. */
    @Test
    void vinculoNaoPermitidoRetorna400() throws Exception {
        mockMvc.perform(post("/api/cadastro/solicitante")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo("fabio@edu.unifor.br", "MESTRANDO", "senha-de-teste-123")))
                .andExpect(status().isBadRequest());

        assertThat(usuarioRepository.count()).isZero();
    }

    @Test
    void senhaCurtaRetorna400ComOCampo() throws Exception {
        mockMvc.perform(post("/api/cadastro/solicitante")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo("gina@edu.unifor.br", "DOCENTE", "1234567")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos[0].campo").value("senha"));

        assertThat(usuarioRepository.count()).isZero();
    }

    @Test
    void lattesComFormatoErradoRetorna400() throws Exception {
        String corpo = """
                {"nome":"Hugo","email":"hugo@edu.unifor.br","senha":"senha-de-teste-123",
                 "telefone":"85999990000","vinculo":"DOCENTE",
                 "lattesUrl":"http://exemplo.com/nao-e-lattes"}
                """;

        mockMvc.perform(post("/api/cadastro/solicitante")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos[0].campo").value("lattesUrl"));

        assertThat(usuarioRepository.count()).isZero();
    }

    @Test
    void telefoneComLetraRetorna400() throws Exception {
        String corpo = """
                {"nome":"Iris","email":"iris@edu.unifor.br","senha":"senha-de-teste-123",
                 "telefone":"(85) 99999-0000","vinculo":"DOCENTE","lattesUrl":"%s"}
                """.formatted(LATTES);

        mockMvc.perform(post("/api/cadastro/solicitante")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos[0].campo").value("telefone"));
    }

    /** Formulário que não preenche o ORCID manda vazio; a coluna fica nula. */
    @Test
    void orcidVazioEGravadoComoNulo() throws Exception {
        String corpo = """
                {"nome":"Joana","email":"joana@edu.unifor.br","senha":"senha-de-teste-123",
                 "telefone":"85999990000","vinculo":"PESQUISADOR","lattesUrl":"%s",
                 "orcidUrl":""}
                """.formatted(LATTES);

        mockMvc.perform(post("/api/cadastro/solicitante")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo))
                .andExpect(status().isCreated());

        assertThat(usuarioRepository.findByEmailIgnoreCase("joana@edu.unifor.br")
                .orElseThrow().getOrcidUrl()).isNull();
    }

    // ---- Apoio ----

    private void registrar(String email) throws Exception {
        mockMvc.perform(post("/api/cadastro/solicitante")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo(email, "DOCENTE", "senha-de-teste-123")))
                .andExpect(status().isCreated());
    }

    private static String corpo(String email, String vinculo, String senha) {
        return """
                {"nome":"Ana Ribeiro","email":"%s","senha":"%s",
                 "telefone":"85999990000","vinculo":"%s","lattesUrl":"%s"}
                """.formatted(email, senha, vinculo, LATTES);
    }
}
