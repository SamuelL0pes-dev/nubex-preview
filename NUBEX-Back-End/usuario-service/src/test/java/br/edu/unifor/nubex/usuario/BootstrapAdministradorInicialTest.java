package br.edu.unifor.nubex.usuario;

import static org.assertj.core.api.Assertions.assertThat;

import br.edu.unifor.nubex.usuario.config.BootstrapAdministradorInicial;
import br.edu.unifor.nubex.usuario.model.PerfilUsuario;
import br.edu.unifor.nubex.usuario.model.StatusUsuario;
import br.edu.unifor.nubex.usuario.model.TipoUsuario;
import br.edu.unifor.nubex.usuario.model.Usuario;
import br.edu.unifor.nubex.usuario.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;

/**
 * Testa o seed do administrador inicial contra um PostgreSQL de verdade, subido
 * em container pelo Testcontainers.
 *
 * As propriedades são definidas aqui, e não lidas do ambiente, para o teste não
 * depender da máquina de quem roda nem do CI.
 */
@SpringBootTest
@Import(ConfiguracaoPostgresTeste.class)
@TestPropertySource(properties = {
        "nubex.admin.email=admin@unifor.br",
        "nubex.admin.senha=senha-de-teste-123",
        "nubex.admin.nome=Administrador de Teste"
})
class BootstrapAdministradorInicialTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private BootstrapAdministradorInicial bootstrap;

    /**
     * O seed roda na subida do contexto, então o administrador já deve existir
     * sem o teste chamar nada.
     */
    @Test
    void criaOAdministradorNaSubidaDaAplicacao() {
        assertThat(usuarioRepository.existsByPerfil(PerfilUsuario.ADMINISTRADOR)).isTrue();

        Usuario administrador = usuarioRepository
                .findByEmailIgnoreCase("admin@unifor.br")
                .orElseThrow();

        assertThat(administrador.getPerfil()).isEqualTo(PerfilUsuario.ADMINISTRADOR);
        assertThat(administrador.getTipo()).isEqualTo(TipoUsuario.INTERNO);
        assertThat(administrador.getStatus()).isEqualTo(StatusUsuario.ATIVO);
        assertThat(administrador.getNome()).isEqualTo("Administrador de Teste");
    }

    /** A senha é gravada como hash BCrypt, nunca em texto. */
    @Test
    void gravaApenasOHashDaSenha() {
        Usuario administrador = usuarioRepository
                .findByEmailIgnoreCase("admin@unifor.br")
                .orElseThrow();

        assertThat(administrador.getSenhaHash()).isNotEqualTo("senha-de-teste-123");
        assertThat(administrador.getSenhaHash()).startsWith("$2");
        assertThat(passwordEncoder.matches("senha-de-teste-123", administrador.getSenhaHash()))
                .isTrue();
    }

    /**
     * O comportamento mais importante: rodar o seed de novo não cria um segundo
     * administrador. Sem essa checagem, cada reinício da aplicação bateria no
     * índice único do e-mail e a subida falharia.
     */
    @Test
    void naoCriaSegundoAdministradorQuandoJaExiste() {
        long antes = usuarioRepository.count();

        bootstrap.run(new DefaultApplicationArguments());
        bootstrap.run(new DefaultApplicationArguments());

        assertThat(usuarioRepository.count()).isEqualTo(antes);
    }
}
