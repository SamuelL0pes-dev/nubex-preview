package br.edu.unifor.nubex.usuario;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.edu.unifor.nubex.usuario.model.PerfilUsuario;
import br.edu.unifor.nubex.usuario.model.StatusUsuario;
import br.edu.unifor.nubex.usuario.model.TipoUsuario;
import br.edu.unifor.nubex.usuario.model.Usuario;
import br.edu.unifor.nubex.usuario.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Login de ponta a ponta, contra um PostgreSQL de verdade subido pelo
 * Testcontainers.
 *
 * <p>O par de chaves é gerado em memória na subida da classe, e não lido de
 * arquivo nem escrito no código: assim não existe chave dentro do repositório, o
 * teste roda igual no CI e na máquina de qualquer pessoa, e cada execução usa um
 * par diferente.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(ConfiguracaoPostgresTeste.class)
class AutenticacaoLoginTest {

    private static final String SENHA_CORRETA = "senha-de-teste-123";

    private static final String CHAVE_PUBLICA;
    private static final String CHAVE_PRIVADA;

    static {
        try {
            KeyPairGenerator gerador = KeyPairGenerator.getInstance("RSA");
            gerador.initialize(2048);
            KeyPair par = gerador.generateKeyPair();
            // getEncoded() da pública sai em X.509 e da privada em PKCS#8,
            // exatamente os formatos que o ChavesRsa espera.
            CHAVE_PUBLICA = Base64.getEncoder().encodeToString(par.getPublic().getEncoded());
            CHAVE_PRIVADA = Base64.getEncoder().encodeToString(par.getPrivate().getEncoded());
        } catch (Exception causa) {
            throw new ExceptionInInitializerError(causa);
        }
    }

    @DynamicPropertySource
    static void chavesDeTeste(DynamicPropertyRegistry registro) {
        registro.add("nubex.jwt.chave-publica", () -> CHAVE_PUBLICA);
        registro.add("nubex.jwt.chave-privada", () -> CHAVE_PRIVADA);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtDecoder jwtDecoder;

    private final ObjectMapper json = new ObjectMapper();

    @BeforeEach
    void prepararBanco() {
        usuarioRepository.deleteAll();
        salvar("rodrigo@edu.unifor.br", "2024001", StatusUsuario.ATIVO);
    }

    // ---- Caminho que dá certo ----

    @Test
    void loginComEmailDevolveToken() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo("rodrigo@edu.unifor.br", SENHA_CORRETA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.tipoDeToken").value("Bearer"))
                .andExpect(jsonPath("$.expiraEmSegundos").value(7200))
                .andExpect(jsonPath("$.usuario.nome").value("Rodrigo Temporal"))
                .andExpect(jsonPath("$.usuario.perfil").value("PARTICIPANTE"))
                .andExpect(jsonPath("$.usuario.tipo").value("INTERNO"));
    }

    /** Quem tem matrícula pode entrar por ela, sem digitar o e-mail. */
    @Test
    void loginComMatriculaDevolveToken() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo("2024001", SENHA_CORRETA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    // ---- Recusas ----

    @Test
    void senhaErradaRetorna401() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo("rodrigo@edu.unifor.br", "senha-errada-123")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    /**
     * O teste que importa para a privacidade: a resposta de conta que não existe
     * tem que ser indistinguível da de senha errada. Se fossem diferentes,
     * qualquer pessoa descobriria quem está cadastrado no portal testando
     * endereços de e-mail.
     */
    @Test
    void contaInexistenteRespondeIgualASenhaErrada() throws Exception {
        String comSenhaErrada = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo("rodrigo@edu.unifor.br", "senha-errada-123")))
                .andExpect(status().isUnauthorized())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        String contaInexistente = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo("ninguem@edu.unifor.br", "senha-errada-123")))
                .andExpect(status().isUnauthorized())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        assertThat(mensagem(contaInexistente)).isEqualTo(mensagem(comSenhaErrada));
    }

    /**
     * Com a senha certa já é seguro dizer o motivo: quem acertou a senha é a
     * própria pessoa, e ela precisa saber que o cadastro aguarda aprovação em vez
     * de ficar achando que errou a senha.
     */
    @Test
    void contaAguardandoAprovacaoRetorna403ComOMotivo() throws Exception {
        salvar("novo@edu.unifor.br", "2024002", StatusUsuario.AGUARDANDO_APROVACAO);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo("novo@edu.unifor.br", SENHA_CORRETA)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.mensagem").value("Seu cadastro ainda aguarda aprovação"));
    }

    /** O @Valid barra antes de chegar ao banco (RNF013). */
    @Test
    void corpoSemSenhaRetorna400() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"identificador\":\"rodrigo@edu.unifor.br\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    // ---- O conteúdo do token ----

    @Test
    void tokenCarregaOsCamposQueAPermissaoUsa() throws Exception {
        Jwt token = jwtDecoder.decode(tokenDoLogin());

        assertThat(token.getClaimAsString("iss")).isEqualTo("nubex-usuario-service");
        assertThat(token.getSubject()).isNotBlank();
        assertThat(token.getClaimAsString("perfil")).isEqualTo("PARTICIPANTE");
        assertThat(token.getClaimAsString("tipo")).isEqualTo("INTERNO");
        assertThat(token.getClaimAsString("nome")).isEqualTo("Rodrigo Temporal");
        assertThat(token.getExpiresAt()).isNotNull();
    }

    /**
     * O token fica guardado no navegador e viaja em toda requisição. Só o nome
     * entra, porque a tela precisa dele; nada de e-mail, cpf, matrícula ou
     * telefone (RNF010).
     */
    @Test
    void tokenNaoLevaDadoPessoalAlemDoNome() throws Exception {
        Jwt token = jwtDecoder.decode(tokenDoLogin());

        assertThat(token.getClaims())
                .doesNotContainKeys("email", "cpf", "cnpj", "matricula", "telefone", "senha");
    }

    // ---- O token funcionando nas rotas protegidas ----

    /**
     * Prova que a conferência do token está ligada de verdade na cadeia de
     * filtros: sem token, a mesma rota devolve 401 (é o que o
     * UsuarioApplicationTests já verifica). Com um token válido, deixa de ser 401
     * e vira 404, porque a rota realmente não existe -- ou seja, a requisição
     * passou pela autenticação e chegou ao roteamento.
     */
    @Test
    void tokenValidoPassaPelaAutenticacao() throws Exception {
        mockMvc.perform(get("/api/qualquer-coisa")
                        .header("Authorization", "Bearer " + tokenDoLogin()))
                .andExpect(status().isNotFound());
    }

    /**
     * O 401 de token inválido tem que sair no formato único de erro do projeto,
     * igual a todos os outros.
     *
     * <p>Este teste existe por causa de um erro encontrado na verificação manual:
     * sem o authenticationEntryPoint declarado no oauth2ResourceServer, o Spring
     * usa o tratamento dele, que responde 401 com <b>corpo vazio</b> e descreve o
     * erro interno no cabeçalho WWW-Authenticate ("Malformed token"). Os dois
     * problemas de uma vez: o front recebe um 401 sem nada para mostrar, e detalhe
     * de implementação vaza para fora.
     */
    @Test
    void tokenInventadoDa401NoFormatoDeErroDoProjeto() throws Exception {
        mockMvc.perform(get("/api/qualquer-coisa")
                        .header("Authorization", "Bearer isto.nao.e-um-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.mensagem").value("Autenticação necessária ou inválida"))
                .andExpect(jsonPath("$.caminho").value("/api/qualquer-coisa"));
    }

    // ---- Apoio ----

    private String tokenDoLogin() throws Exception {
        String resposta = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo("rodrigo@edu.unifor.br", SENHA_CORRETA)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        return json.readTree(resposta).get("token").asText();
    }

    private String mensagem(String respostaJson) throws Exception {
        return json.readTree(respostaJson).get("mensagem").asText();
    }

    private static String corpo(String identificador, String senha) {
        return "{\"identificador\":\"" + identificador + "\",\"senha\":\"" + senha + "\"}";
    }

    private Usuario salvar(String email, String matricula, StatusUsuario status) {
        Usuario usuario = new Usuario();
        usuario.setNome("Rodrigo Temporal");
        usuario.setEmail(email);
        usuario.setMatricula(matricula);
        usuario.setSenhaHash(passwordEncoder.encode(SENHA_CORRETA));
        usuario.setPerfil(PerfilUsuario.PARTICIPANTE);
        usuario.setTipo(TipoUsuario.INTERNO);
        usuario.setStatus(status);
        return usuarioRepository.save(usuario);
    }
}
