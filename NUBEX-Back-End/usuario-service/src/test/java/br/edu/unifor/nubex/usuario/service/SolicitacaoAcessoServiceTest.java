package br.edu.unifor.nubex.usuario.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import br.edu.unifor.nubex.common.exception.ConflitoException;
import br.edu.unifor.nubex.common.exception.NegocioException;
import br.edu.unifor.nubex.usuario.dto.SolicitacaoAcessoRequisicao;
import br.edu.unifor.nubex.usuario.model.Usuario;
import br.edu.unifor.nubex.usuario.model.VinculoUsuario;
import br.edu.unifor.nubex.usuario.repository.PedidoAprovacaoRepository;
import br.edu.unifor.nubex.usuario.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Os caminhos do service que o teste de ponta a ponta não alcança.
 *
 * <p>O {@link br.edu.unifor.nubex.usuario.SolicitacaoAcessoTest} cobre a rota
 * inteira contra um PostgreSQL real, e é o teste que importa para o comportamento.
 * Mas dois caminhos daqui ele não consegue exercitar:
 *
 * <ul>
 *   <li>a <b>corrida</b> no índice único, que exigiria duas requisições no mesmo
 *       milissegundo -- não se reproduz de forma determinística;
 *   <li>a prova de que a recusa por vínculo acontece <b>antes</b> de qualquer
 *       acesso ao banco, e não depois.
 * </ul>
 *
 * <p>Aqui o repository é mockado, e é o limite certo: o que está sendo testado é a
 * decisão do service quando o banco reage de um jeito, não o banco. Sem Spring e
 * sem Docker.
 */
class SolicitacaoAcessoServiceTest {

    private final UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
    private final PedidoAprovacaoRepository pedidoRepository = mock(PedidoAprovacaoRepository.class);
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private final SolicitacaoAcessoService service =
            new SolicitacaoAcessoService(usuarioRepository, pedidoRepository, passwordEncoder);

    /**
     * A corrida: a guarda não viu o e-mail porque a outra requisição ainda não
     * tinha gravado, e o índice único do banco é quem barra.
     *
     * <p>Sem o catch no service, isso sairia 500 com código de rastreio. Tem de
     * sair o mesmo 409 do caminho normal, que é o que o front sabe tratar.
     */
    @Test
    void colisaoNoIndiceUnicoViraConflitoENao500() {
        when(usuarioRepository.existsByEmailIgnoreCase(anyString())).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class)))
                .thenThrow(new DataIntegrityViolationException(
                        "ERROR: duplicate key value violates unique constraint \"uq_usuario_email\""));

        assertThatThrownBy(() -> service.registrar(requisicao(VinculoUsuario.DOCENTE)))
                .isInstanceOf(ConflitoException.class)
                .hasMessage("Já existe uma conta com este e-mail");

        // O pedido não pode ter sido criado se a conta não entrou.
        verifyNoInteractions(pedidoRepository);
    }

    /** O caminho amigável: a guarda vê o e-mail e nem tenta gravar. */
    @Test
    void emailJaCadastradoNemChegaNoSave() {
        when(usuarioRepository.existsByEmailIgnoreCase(anyString())).thenReturn(true);

        assertThatThrownBy(() -> service.registrar(requisicao(VinculoUsuario.DOCENTE)))
                .isInstanceOf(ConflitoException.class);

        verify(usuarioRepository, never()).save(any(Usuario.class));
        verifyNoInteractions(pedidoRepository);
    }

    /**
     * O RF052 é para docente ou pesquisador. A recusa tem de acontecer antes de
     * tocar no banco: nem a consulta de e-mail deve rodar para um vínculo que a
     * rota não aceita.
     */
    @Test
    void vinculoNaoPermitidoRecusaAntesDeTocarNoBanco() {
        assertThatThrownBy(() -> service.registrar(requisicao(VinculoUsuario.MESTRANDO)))
                .isInstanceOf(NegocioException.class);

        verifyNoInteractions(usuarioRepository);
        verifyNoInteractions(pedidoRepository);
    }

    private static SolicitacaoAcessoRequisicao requisicao(VinculoUsuario vinculo) {
        return new SolicitacaoAcessoRequisicao(
                "Ana Ribeiro",
                "ana.ribeiro@edu.unifor.br",
                "senha-de-teste-123",
                "85999990000",
                vinculo,
                "http://lattes.cnpq.br/4928172910492817",
                null);
    }
}
