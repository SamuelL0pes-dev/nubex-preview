package br.edu.unifor.nubex.usuario;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import br.edu.unifor.nubex.usuario.config.BootstrapAdministradorInicial;
import br.edu.unifor.nubex.usuario.model.PerfilUsuario;
import br.edu.unifor.nubex.usuario.model.StatusUsuario;
import br.edu.unifor.nubex.usuario.model.TipoUsuario;
import br.edu.unifor.nubex.usuario.model.Usuario;
import br.edu.unifor.nubex.usuario.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Os caminhos em que o seed <b>recusa</b> criar o administrador.
 *
 * <p>São os casos que não quebram nada e por isso passam despercebidos: se um
 * {@code return} virar {@code throw} numa refatoração, o serviço deixa de subir e
 * nenhum teste do caminho feliz reclama.
 *
 * <h4>Por que uma classe só, e não uma por cenário</h4>
 *
 * O caminho natural seria uma classe por cenário, cada uma com
 * {@code @TestPropertySource} diferente, porque o Spring reaproveita contexto por
 * configuração. Só que cada contexto novo instancia o {@code postgres()} do
 * {@link ConfiguracaoPostgresTeste}: seriam dois contêineres PostgreSQL a mais
 * neste módulo, que já sobe dois.
 *
 * <p>Aqui, em vez de variar as propriedades, construímos o
 * {@link BootstrapAdministradorInicial} direto, com os valores de cada cenário.
 * O construtor é público e recebe os três valores, então dá no mesmo para o que
 * está sendo testado -- a decisão do seed -- e cabe tudo em um contexto.
 *
 * <p>Esta classe não declara {@code @TestPropertySource}, de propósito: sem as
 * propriedades {@code nubex.admin.*}, o seed não cria nada na subida e o banco
 * começa vazio. De quebra, isso prova que a aplicação sobe sem as variáveis
 * definidas, que é a situação do CI.
 */
@SpringBootTest
@Import(ConfiguracaoPostgresTeste.class)
class BootstrapAdministradorRecusaTest {

    private static final String SENHA_VALIDA = "senha-de-teste-123";

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void limparBanco() {
        usuarioRepository.deleteAll();
    }

    /**
     * Sem as variáveis, a aplicação sobe e nenhum administrador é criado. O
     * próprio fato de este teste rodar já prova a primeira metade: se a subida
     * falhasse, não haveria contexto para injetar nada.
     */
    @Test
    void semVariavelNaoCriaAdministrador() {
        assertThatCode(() -> seed("", "").run(new DefaultApplicationArguments()))
                .doesNotThrowAnyException();

        assertThat(usuarioRepository.existsByPerfil(PerfilUsuario.ADMINISTRADOR)).isFalse();
        assertThat(usuarioRepository.count()).isZero();
    }

    /** Senha com 7 caracteres: abaixo do mínimo de 8, não cria. */
    @Test
    void senhaCurtaNaoCriaAdministrador() {
        assertThatCode(() -> seed("admin@unifor.br", "1234567").run(new DefaultApplicationArguments()))
                .doesNotThrowAnyException();

        assertThat(usuarioRepository.existsByPerfil(PerfilUsuario.ADMINISTRADOR)).isFalse();
    }

    /**
     * O caso que derrubava o serviço: o e-mail da variável já pertence a outra
     * conta, de outro perfil.
     *
     * <p>A guarda do topo do seed pergunta só pelo perfil, então ela não encontra
     * administrador e deixa seguir. Antes da correção, o {@code save()} violava o
     * índice único {@code uq_usuario_email}, e exceção em {@code ApplicationRunner}
     * aborta a inicialização depois de o contexto estar pronto -- o
     * usuario-service não subia.
     */
    @Test
    void emailDeOutraContaNaoCriaAdministradorNemDerrubaAplicacao() {
        salvarSolicitante("joao@unifor.br");

        assertThatCode(() -> seed("joao@unifor.br", SENHA_VALIDA).run(new DefaultApplicationArguments()))
                .doesNotThrowAnyException();

        assertThat(usuarioRepository.existsByPerfil(PerfilUsuario.ADMINISTRADOR)).isFalse();
        assertThat(usuarioRepository.count()).isEqualTo(1);
    }

    /** A caixa não importa para o índice, que é sobre LOWER(email). */
    @Test
    void emailDeOutraContaEmOutraCaixaTambemNaoCria() {
        salvarSolicitante("joao@unifor.br");

        assertThatCode(() -> seed("JOAO@UNIFOR.BR", SENHA_VALIDA).run(new DefaultApplicationArguments()))
                .doesNotThrowAnyException();

        assertThat(usuarioRepository.existsByPerfil(PerfilUsuario.ADMINISTRADOR)).isFalse();
    }

    /** Erro de digitação na variável não vira conta de acesso total. */
    @Test
    void emailSemFormatoDeEmailNaoCriaAdministrador() {
        assertThatCode(() -> seed("admin-arroba-unifor", SENHA_VALIDA).run(new DefaultApplicationArguments()))
                .doesNotThrowAnyException();

        assertThat(usuarioRepository.existsByPerfil(PerfilUsuario.ADMINISTRADOR)).isFalse();
    }

    /**
     * Espaço nas pontas é cortado antes de gravar.
     *
     * <p>Sem isto, o índice único não protege: ele é sobre LOWER(email), que
     * normaliza caixa e não espaço. Depois seria possível criar uma segunda conta
     * com o mesmo e-mail sem o espaço, e o login por e-mail não acharia a primeira.
     */
    @Test
    void emailComEspacoNasPontasEGravadoSemEspaco() {
        seed("  admin@unifor.br  ", SENHA_VALIDA).run(new DefaultApplicationArguments());

        Usuario administrador = usuarioRepository
                .findByEmailIgnoreCase("admin@unifor.br")
                .orElseThrow();

        assertThat(administrador.getEmail()).isEqualTo("admin@unifor.br");
        assertThat(administrador.getPerfil()).isEqualTo(PerfilUsuario.ADMINISTRADOR);
    }

    /** Variável de nome em branco cai no padrão, em vez de gravar nome vazio. */
    @Test
    void nomeEmBrancoUsaOPadrao() {
        seed("admin@unifor.br", SENHA_VALIDA).run(new DefaultApplicationArguments());

        Usuario administrador = usuarioRepository
                .findByEmailIgnoreCase("admin@unifor.br")
                .orElseThrow();

        assertThat(administrador.getNome()).isEqualTo("Administrador NUBEX");
    }

    // ---- Apoio ----

    /** O nome vai em branco em todos os cenários; só um teste olha para ele. */
    private BootstrapAdministradorInicial seed(String email, String senha) {
        return new BootstrapAdministradorInicial(
                usuarioRepository, passwordEncoder, email, senha, "");
    }

    /**
     * Uma conta que não é administrador, para ocupar o e-mail. SOLICITANTE e
     * INTERNO não exigem matrícula nem cpf: a constraint de matrícula vale só para
     * PARTICIPANTE interno, e a de cpf ou cnpj só para quem é EXTERNO.
     */
    private void salvarSolicitante(String email) {
        Usuario solicitante = new Usuario();
        solicitante.setNome("João da Silva");
        solicitante.setEmail(email);
        solicitante.setSenhaHash(passwordEncoder.encode(SENHA_VALIDA));
        solicitante.setPerfil(PerfilUsuario.SOLICITANTE);
        solicitante.setTipo(TipoUsuario.INTERNO);
        solicitante.setStatus(StatusUsuario.ATIVO);
        usuarioRepository.save(solicitante);
    }
}
