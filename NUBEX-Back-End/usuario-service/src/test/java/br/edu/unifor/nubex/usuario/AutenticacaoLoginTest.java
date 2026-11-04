package br.edu.unifor.nubex.usuario;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.edu.unifor.nubex.common.config.CookieDeSessao;
import br.edu.unifor.nubex.usuario.model.PerfilUsuario;
import br.edu.unifor.nubex.usuario.model.StatusUsuario;
import br.edu.unifor.nubex.usuario.model.TipoUsuario;
import br.edu.unifor.nubex.usuario.model.Usuario;
import br.edu.unifor.nubex.usuario.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
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

    /**
     * O corpo leva só o que a tela precisa, e o token <b>não está nele</b>. É o
     * ponto da mudança para cookie: se o token voltasse aqui, o front teria que
     * guardá-lo em algum lugar legível por JavaScript, e um XSS no portal o
     * levaria junto.
     */
    @Test
    void loginDevolveOsDadosDaTelaSemOToken() throws Exception {
        String corpoDaResposta = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo("rodrigo@edu.unifor.br", SENHA_CORRETA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.expiraEmSegundos").value(7200))
                .andExpect(jsonPath("$.usuario.nome").value("Rodrigo Temporal"))
                .andExpect(jsonPath("$.usuario.perfil").value("PARTICIPANTE"))
                .andExpect(jsonPath("$.usuario.tipo").value("INTERNO"))
                .andExpect(jsonPath("$.token").doesNotExist())
                .andExpect(jsonPath("$.tipoDeToken").doesNotExist())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        /*
         * As duas asserções acima cobrem os nomes que o campo tinha. Esta cobre
         * qualquer nome: todo JWT começa por "eyJ", que é o base64 de '{"'. Se um
         * dia alguém devolver o token num campo novo, este teste pega.
         */
        assertThat(corpoDaResposta).doesNotContain("eyJ");
    }

    /** Quem tem matrícula pode entrar por ela, sem digitar o e-mail. */
    @Test
    void loginComMatriculaTambemPoeOTokenNoCookie() throws Exception {
        MockHttpServletResponse resposta = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo("2024001", SENHA_CORRETA)))
                .andExpect(status().isOk())
                .andReturn().getResponse();

        assertThat(resposta.getCookie(CookieDeSessao.NOME)).isNotNull();
        assertThat(resposta.getCookie(CookieDeSessao.NOME).getValue()).isNotBlank();
    }

    // ---- O cookie de acesso ----

    /**
     * Cada atributo aqui está testado porque cada um, faltando, produz uma falha
     * diferente e silenciosa:
     *
     * <ul>
     *   <li>sem {@code HttpOnly}, a proteção que motivou toda a mudança não
     *       existe, e nada no comportamento denuncia;
     *   <li>sem {@code SameSite=Lax}, a API fica aberta a CSRF enquanto o token
     *       de CSRF não existir;
     *   <li>com {@code Path} diferente de "/", o logout não acha o cookie para
     *       apagar;
     *   <li>o {@code Max-Age} tem que acompanhar a validade do token, senão o
     *       navegador guarda um cookie que já só serve para tomar 401.
     * </ul>
     *
     * <p>O {@code Secure} entra na lista por um motivo a mais: ele é o único que
     * depende do ambiente, e por isso é o único que alguém poderia afrouxar sem
     * perceber. Ele vem de {@code COOKIE_SEGURO}, cujo padrão é ligado em
     * <b>qualquer</b> perfil -- de propósito, porque o perfil dev é o padrão dos
     * serviços e afrouxar dentro dele valeria também num deploy que esquecesse de
     * definir o perfil. Esta asserção é o que faz um afrouxamento acidental
     * aparecer aqui, em vez de passar calado até produção.
     */
    @Test
    void cookieDeAcessoVemComOsAtributosQueProtegem() throws Exception {
        MockHttpServletResponse resposta = respostaDoLogin();

        Cookie cookie = resposta.getCookie(CookieDeSessao.NOME);
        assertThat(cookie).isNotNull();
        assertThat(cookie.getValue()).isNotBlank();
        assertThat(cookie.isHttpOnly()).isTrue();
        assertThat(cookie.getSecure()).isTrue();
        assertThat(cookie.getPath()).isEqualTo("/");
        assertThat(cookie.getMaxAge()).isEqualTo(7200);

        /*
         * SameSite não existe na API de Cookie do servlet, só no cabeçalho --
         * então é no cabeçalho que se confere, que é também o que o navegador lê.
         */
        assertThat(resposta.getHeader(HttpHeaders.SET_COOKIE)).contains("SameSite=Lax");
    }

    /**
     * O que prova que o resolvedor novo está ligado: a requisição chega só com o
     * cookie, sem cabeçalho Authorization nenhum, e passa pela autenticação.
     *
     * <p>Mesmo raciocínio do teste por cabeçalho mais abaixo: 404 em vez de 401
     * significa que a requisição atravessou a autenticação e chegou ao
     * roteamento, onde a rota de fato não existe.
     */
    @Test
    void tokenNoCookieAutenticaSemCabecalho() throws Exception {
        mockMvc.perform(get("/api/qualquer-coisa").cookie(cookieDoLogin()))
                .andExpect(status().isNotFound());
    }

    // ---- CORS com credenciais ----

    /**
     * O teste que guarda a linha que mais importa desta mudança:
     * {@code setAllowCredentials(true)} no SecurityConfig.
     *
     * <p>Sem ela, o navegador recebe a resposta do login com status 200 e com o
     * Set-Cookie dentro, não encontra o cabeçalho
     * {@code Access-Control-Allow-Credentials}, e <b>descarta a resposta
     * inteira</b>. O fetch rejeita, o cookie não é guardado, e não aparece uma
     * linha no log do servidor -- o erro existe só no console do navegador. Foi
     * exatamente a causa do login ter parado de funcionar.
     *
     * <p>Em desenvolvimento toda chamada é de outra origem: o front está na porta
     * 5173 e a API na 8081.
     */
    @Test
    void respostaDoLoginAutorizaOEnvioDeCookie() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .header(HttpHeaders.ORIGIN, "http://localhost:5173")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo("rodrigo@edu.unifor.br", SENHA_CORRETA)))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"))
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
    }

    /**
     * A consulta que o navegador faz <b>antes</b> do POST do login, por ser
     * requisição de outra origem com cabeçalho próprio. Se ela não autorizar
     * credenciais, o POST nem chega a sair.
     */
    @Test
    void consultaPreviaDoNavegadorAutorizaCredenciais() throws Exception {
        mockMvc.perform(options("/api/auth/login")
                        .header(HttpHeaders.ORIGIN, "http://localhost:5173")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
    }

    /**
     * O outro lado: liberar credenciais não liberou origem. Uma origem fora da
     * lista continua recusada -- se passasse, qualquer site poderia chamar a API
     * com o cookie da pessoa, que é o ataque que o CORS existe para impedir.
     */
    @Test
    void origemDeFacaRecusadaMesmoComCredenciaisLiberadas() throws Exception {
        mockMvc.perform(options("/api/auth/login")
                        .header(HttpHeaders.ORIGIN, "http://site-malicioso.example")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isForbidden());
    }

    // ---- A documentação (RNF018) ----

    /**
     * O Swagger tem que descrever os <b>dois</b> jeitos de autenticar.
     *
     * <p>Antes desta mudança ele declarava só o cabeçalho {@code Authorization}.
     * Deixar assim seria pior que documentação faltando: quem abrisse a tela para
     * entender como a API autentica leria que precisa guardar e reenviar o token
     * -- exatamente a prática que a mudança para cookie removeu.
     *
     * <p>Os dois esquemas são declarados como itens separados em {@code security},
     * o que em OpenAPI significa "um <b>ou</b> o outro basta". É o comportamento
     * real do resolvedor. Num item só significaria que os dois são obrigatórios
     * juntos, o que seria falso -- e é o erro fácil de cometer aqui.
     */
    @Test
    void swaggerDocumentaOCookieEOCabecalho() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.components.securitySchemes.cookieAuth.type").value("apiKey"))
                .andExpect(jsonPath("$.components.securitySchemes.cookieAuth.in").value("cookie"))
                .andExpect(jsonPath("$.components.securitySchemes.cookieAuth.name")
                        .value(CookieDeSessao.NOME))
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme").value("bearer"))
                // Dois itens, não um com dois esquemas: "um ou o outro".
                .andExpect(jsonPath("$.security.length()").value(2))
                /*
                 * O aviso de que o campo do cookie no diálogo Authorize não
                 * funciona. Sem ele, quem for testar uma rota protegida cola o
                 * token no primeiro campo da lista e não entende o 401.
                 */
                .andExpect(jsonPath("$.components.securitySchemes.cookieAuth.description")
                        .value(org.hamcrest.Matchers.containsString("bearerAuth")));
    }

    // ---- Logout ----

    /**
     * Apagar cookie é mandar um substituto vencido. Os outros atributos têm que
     * ser idênticos aos do login: se o Path ou o SameSite divergirem, o navegador
     * entende que são dois cookies diferentes, guarda o vencido e <b>mantém o
     * válido</b> -- o logout responde 204, parece ter funcionado, e a pessoa
     * segue autenticada.
     */
    @Test
    void logoutApagaOCookieComOsMesmosAtributos() throws Exception {
        MockHttpServletResponse resposta = mockMvc.perform(post("/api/auth/logout")
                        .cookie(cookieDoLogin()))
                .andExpect(status().isNoContent())
                .andReturn().getResponse();

        Cookie cookie = resposta.getCookie(CookieDeSessao.NOME);
        assertThat(cookie).isNotNull();
        assertThat(cookie.getValue()).isEmpty();
        assertThat(cookie.getMaxAge()).isZero();
        assertThat(cookie.getPath()).isEqualTo("/");
        assertThat(cookie.isHttpOnly()).isTrue();
        assertThat(resposta.getHeader(HttpHeaders.SET_COOKIE)).contains("SameSite=Lax");
    }

    /**
     * O motivo de o logout ser rota pública: quem volta depois do token vencer
     * precisa conseguir sair. Se exigisse token válido, tomaria 401 e ficaria com
     * um cookie preso no navegador, sem como removê-lo pela interface -- o cookie
     * é HttpOnly, o front não apaga.
     */
    @Test
    void logoutSemEstarLogadoTambemResponde204() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isNoContent());
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

    private MockHttpServletResponse respostaDoLogin() throws Exception {
        return mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo("rodrigo@edu.unifor.br", SENHA_CORRETA)))
                .andExpect(status().isOk())
                .andReturn().getResponse();
    }

    /** O cookie que o login devolveu, para remandar numa requisição seguinte. */
    private Cookie cookieDoLogin() throws Exception {
        Cookie cookie = respostaDoLogin().getCookie(CookieDeSessao.NOME);
        assertThat(cookie).as("o login tem que devolver o cookie de acesso").isNotNull();
        return cookie;
    }

    /**
     * O token em si, para os testes que abrem o JWT. Vem do cookie agora, porque
     * é o único lugar onde ele sai.
     */
    private String tokenDoLogin() throws Exception {
        return cookieDoLogin().getValue();
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
