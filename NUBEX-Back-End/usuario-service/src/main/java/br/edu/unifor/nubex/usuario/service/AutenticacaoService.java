package br.edu.unifor.nubex.usuario.service;

import br.edu.unifor.nubex.common.exception.NegocioException;
import br.edu.unifor.nubex.usuario.dto.LoginRequisicao;
import br.edu.unifor.nubex.usuario.dto.LoginResposta;
import br.edu.unifor.nubex.usuario.model.StatusUsuario;
import br.edu.unifor.nubex.usuario.model.Usuario;
import br.edu.unifor.nubex.usuario.repository.UsuarioRepository;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Login: confere a senha e, se estiver tudo certo, devolve o token.
 *
 * <p>Atende o RF073 e o RNF013 pela raiz: é aqui que o usuário ganha a
 * credencial que todas as rotas protegidas dos cinco serviços vão conferir.
 */
@Service
public class AutenticacaoService {

    private static final Logger log = LoggerFactory.getLogger(AutenticacaoService.class);

    /**
     * A mesma mensagem para conta que não existe e para senha errada.
     *
     * <p>Se a resposta fosse "não existe conta com este e-mail", qualquer pessoa
     * poderia descobrir quem está cadastrado no portal testando endereços. É o
     * mesmo cuidado que o RF086 exige da recuperação de senha, onde a resposta é
     * igual exista ou não a conta.
     */
    private static final String CREDENCIAIS_INVALIDAS = "E-mail, matrícula ou senha incorretos";

    /**
     * O resultado de um login que deu certo, com as duas partes separadas: o
     * {@code token}, que o controller põe no cookie, e o {@code corpo}, que vai
     * no JSON da resposta.
     *
     * <p>Existe por causa da mudança para cookie. Antes o token era um campo do
     * LoginResposta e saía junto com o resto; agora ele sai por outro caminho, e
     * misturar os dois num só objeto deixaria fácil alguém devolver o token no
     * corpo sem perceber -- que é exatamente o que a mudança veio desfazer.
     *
     * @param token o JWT assinado. Credencial: não vai para log nem para o corpo
     *     da resposta
     * @param validadeEmSegundos quanto tempo o token vale, para o Max-Age do
     *     cookie
     * @param corpo o que o front recebe em JSON
     */
    public record Acesso(String token, long validadeEmSegundos, LoginResposta corpo) {

        /**
         * O toString() que o record geraria sozinho imprimiria o token inteiro.
         * Quem tem uma cópia dele entra como a pessoa até ele vencer, então ele
         * não aparece em log em lugar nenhum.
         */
        @Override
        public String toString() {
            return "Acesso[token=omitido, validadeEmSegundos=" + validadeEmSegundos
                    + ", corpo=" + corpo + "]";
        }
    }

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmissorDeToken emissorDeToken;

    /**
     * Hash descartável, usado só para gastar tempo. Veja {@link #entrar}.
     *
     * <p>É gerado na subida, e não escrito no código, por dois motivos: fica com
     * o mesmo custo de BCrypt dos hashes reais, qualquer que seja o custo
     * configurado, e não existe hash de senha dentro do repositório.
     */
    private final String hashDeDescarte;

    public AutenticacaoService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            EmissorDeToken emissorDeToken) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.emissorDeToken = emissorDeToken;
        this.hashDeDescarte = passwordEncoder.encode("conta-inexistente-" + UUID.randomUUID());
    }

    /**
     * Faz o login.
     *
     * <p>A ordem dos passos é deliberada:
     *
     * <ol>
     *   <li><b>procura a conta.</b> Com arroba, por e-mail; sem arroba, por
     *       matrícula;
     *   <li><b>conta não existe: confere a senha contra um hash descartável e
     *       recusa.</b> Essa conferência inútil é uma defesa, não um descuido --
     *       veja abaixo;
     *   <li><b>senha errada: recusa com 401</b> e a mesma mensagem do passo 2;
     *   <li><b>senha certa mas conta não liberada: recusa com 403</b>, agora
     *       dizendo o motivo. Aqui já é seguro ser específico, porque quem
     *       acertou a senha é a própria pessoa, e ela precisa saber que o
     *       cadastro está aguardando aprovação em vez de achar que errou a senha;
     *   <li><b>tudo certo: emite o token.</b>
     * </ol>
     *
     * <h4>Por que gastar BCrypt numa conta que não existe</h4>
     *
     * <p>Conferir senha com BCrypt é lento de propósito: na máquina onde isto foi
     * medido, cerca de 61 ms com o custo padrão. Se a resposta para "conta não
     * existe" saísse sem passar pelo BCrypt, ela voltaria em poucos
     * milissegundos, e a de senha errada em 61 ms. Essa diferença de tempo é
     * mensurável de fora, e com ela dá para descobrir quais e-mails estão
     * cadastrados -- mesmo com a mensagem sendo idêntica nos dois casos. Chamar o
     * BCrypt contra um hash descartável iguala os tempos e fecha essa porta.
     *
     * <p>Nada do que é digitado aqui vai para o log: nem a senha, obviamente, nem
     * o identificador, que é dado pessoal (RNF010). Nos registros aparece só o id
     * da conta, quando ela existe.
     */
    @Transactional(readOnly = true)
    public Acesso entrar(LoginRequisicao requisicao) {
        Optional<Usuario> encontrado = procurar(requisicao.identificador());

        if (encontrado.isEmpty()) {
            passwordEncoder.matches(requisicao.senha(), hashDeDescarte);
            log.debug("Login recusado: identificador não cadastrado");
            throw new NegocioException(HttpStatus.UNAUTHORIZED, CREDENCIAIS_INVALIDAS);
        }

        Usuario usuario = encontrado.get();

        if (!passwordEncoder.matches(requisicao.senha(), usuario.getSenhaHash())) {
            log.debug("Login recusado: senha incorreta para o usuário {}", usuario.getId());
            throw new NegocioException(HttpStatus.UNAUTHORIZED, CREDENCIAIS_INVALIDAS);
        }

        if (!usuario.estaAtivo()) {
            log.debug("Login recusado: usuário {} com status {}", usuario.getId(), usuario.getStatus());
            throw new NegocioException(HttpStatus.FORBIDDEN, motivoDoBloqueio(usuario.getStatus()));
        }

        log.info("Login concluído para o usuário {}", usuario.getId());
        long validade = emissorDeToken.validadeEmSegundos();
        return new Acesso(
                emissorDeToken.emitir(usuario),
                validade,
                LoginResposta.de(validade, usuario));
    }

    /**
     * Arroba decide: é o único caractere que um e-mail tem e uma matrícula não.
     * O IgnoreCase combina com o índice único sobre LOWER(email) da migração.
     */
    private Optional<Usuario> procurar(String identificador) {
        String limpo = identificador.trim();
        return limpo.contains("@")
                ? usuarioRepository.findByEmailIgnoreCase(limpo)
                : usuarioRepository.findByMatricula(limpo);
    }

    /** Mensagens dos status que existem no banco, na ordem do CHECK da migração. */
    private static String motivoDoBloqueio(StatusUsuario status) {
        return switch (status) {
            case PENDENTE_EMAIL -> "Confirme seu e-mail para ativar o acesso";
            case AGUARDANDO_APROVACAO -> "Seu cadastro ainda aguarda aprovação";
            case RECUSADO -> "Seu cadastro foi recusado";
            case INATIVO -> "Seu acesso está inativo. Procure o solicitante responsável";
            // Inalcançável: estaAtivo() já tratou. O switch exaustivo obriga a
            // revisitar este método se um status novo entrar no enum.
            case ATIVO -> CREDENCIAIS_INVALIDAS;
        };
    }
}
