package br.edu.unifor.nubex.usuario.service;

import br.edu.unifor.nubex.common.exception.ConflitoException;
import br.edu.unifor.nubex.common.exception.NegocioException;
import br.edu.unifor.nubex.usuario.dto.SolicitacaoAcessoRequisicao;
import br.edu.unifor.nubex.usuario.dto.SolicitacaoAcessoResposta;
import br.edu.unifor.nubex.usuario.model.PedidoAprovacao;
import br.edu.unifor.nubex.usuario.model.PerfilUsuario;
import br.edu.unifor.nubex.usuario.model.StatusUsuario;
import br.edu.unifor.nubex.usuario.model.TipoUsuario;
import br.edu.unifor.nubex.usuario.model.Usuario;
import br.edu.unifor.nubex.usuario.model.VinculoUsuario;
import br.edu.unifor.nubex.usuario.repository.PedidoAprovacaoRepository;
import br.edu.unifor.nubex.usuario.repository.UsuarioRepository;
import java.util.EnumSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Pedido de acesso como solicitante interno (RF052).
 *
 * <p>Cria a conta já com o pedido de aprovação aberto, numa transação só. O
 * administrador decide depois, no painel: enquanto isso a pessoa existe no banco
 * mas não entra, porque o login só aceita quem está ATIVO.
 */
@Service
public class SolicitacaoAcessoService {

    private static final Logger log = LoggerFactory.getLogger(SolicitacaoAcessoService.class);

    /**
     * O RF052 é explícito: "um docente ou pesquisador da Unifor". Os outros
     * vínculos do enum existem para o cadastro de participante, que é outra rota
     * e outro requisito (RF054).
     */
    private static final Set<VinculoUsuario> VINCULOS_PERMITIDOS =
            EnumSet.of(VinculoUsuario.DOCENTE, VinculoUsuario.PESQUISADOR);

    private static final String EMAIL_EM_USO = "Já existe uma conta com este e-mail";

    private final UsuarioRepository usuarioRepository;
    private final PedidoAprovacaoRepository pedidoRepository;
    private final PasswordEncoder passwordEncoder;

    public SolicitacaoAcessoService(
            UsuarioRepository usuarioRepository,
            PedidoAprovacaoRepository pedidoRepository,
            PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.pedidoRepository = pedidoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registra o pedido.
     *
     * <h4>O perfil e o tipo são decididos aqui, não recebidos</h4>
     *
     * A requisição não tem campo para eles, e é de propósito: esta é a rota de
     * pedido de acesso como solicitante interno, então o resultado é SOLICITANTE
     * e INTERNO, sempre. Se viessem do cliente, um curl com
     * {@code "perfil": "ADMINISTRADOR"} criaria uma conta de acesso total.
     *
     * <h4>Por que a conta nasce em AGUARDANDO_APROVACAO, e não em PENDENTE_EMAIL</h4>
     *
     * O RF052 diz que o pedido "deve permanecer com status pendente até ser
     * avaliado e aprovado por um administrador", e não exige confirmação de
     * e-mail -- essa é do cadastro de participante (RF054).
     *
     * <p>E, ainda que exigisse, não haveria como: o serviço de envio de e-mail
     * (RF083) não existe nem tem dono em cartão nenhum. Nascer em PENDENTE_EMAIL
     * deixaria todo pedido parado para sempre, esperando um e-mail que ninguém
     * manda.
     *
     * <h4>O pedido nasce sem solicitante</h4>
     *
     * O campo solicitante_id fica nulo, que é o combinado do Apêndice A: "nulo
     * quando a decisão cabe a qualquer administrador". No RF052 quem decide é o
     * administrador, não um solicitante específico.
     */
    @Transactional
    public SolicitacaoAcessoResposta registrar(SolicitacaoAcessoRequisicao requisicao) {
        if (!VINCULOS_PERMITIDOS.contains(requisicao.vinculo())) {
            throw new NegocioException(
                    "O pedido de acesso como solicitante é para docente ou pesquisador da Unifor");
        }

        String email = requisicao.email().trim();

        /*
         * Checagem amigável antes de gravar, para o usuário receber 409 com
         * mensagem em vez do erro cru do índice único. O índice continua sendo a
         * garantia final -- veja o catch lá embaixo.
         */
        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflitoException(EMAIL_EM_USO);
        }

        Usuario solicitante = new Usuario();
        solicitante.setNome(requisicao.nome().trim());
        solicitante.setEmail(email);
        solicitante.setSenhaHash(passwordEncoder.encode(requisicao.senha()));
        solicitante.setTelefone(requisicao.telefone());
        solicitante.setVinculo(requisicao.vinculo());
        solicitante.setLattesUrl(requisicao.lattesUrl().trim());
        solicitante.setOrcidUrl(vazioViraNulo(requisicao.orcidUrl()));

        // Decididos pelo servidor, nunca recebidos.
        solicitante.setPerfil(PerfilUsuario.SOLICITANTE);
        solicitante.setTipo(TipoUsuario.INTERNO);
        solicitante.setStatus(StatusUsuario.AGUARDANDO_APROVACAO);

        PedidoAprovacao pedido = new PedidoAprovacao();
        pedido.setUsuario(solicitante);

        /*
         * O try envolve só o save do usuário, e não os dois.
         *
         * A única violação de integridade possível neste ponto é o índice único
         * do e-mail: o usuário é novo, então não há pedido anterior para colidir
         * com o uq_pedido_aprovacao_aberto. Envolver o save do pedido faria uma
         * violação inesperada dele sair como "e-mail já existe", que seria
         * mentira -- e mensagem errada custa mais tempo que erro sem mensagem.
         *
         * Duas requisições com o mesmo e-mail no mesmo instante passam as duas
         * pela checagem acima e as duas gravam: uma ganha, a outra bate no
         * índice. Sem este catch seria 500 com código de rastreio, em vez do 409
         * que o front sabe tratar. Mesma rede de segurança que o seed usa.
         *
         * O INSERT sai aqui, e não no commit, porque o id é GenerationType.IDENTITY:
         * o Hibernate precisa executar para saber o id. Se fosse SEQUENCE, a
         * violação estouraria depois deste bloco e o catch não serviria para nada.
         */
        try {
            usuarioRepository.save(solicitante);
        } catch (DataIntegrityViolationException colisao) {
            log.debug("Colisão no índice único do e-mail ao registrar pedido de acesso");
            throw new ConflitoException(EMAIL_EM_USO);
        }

        pedidoRepository.save(pedido);

        // Só identificadores: e-mail, nome e telefone são dados pessoais (RNF010).
        log.info("Pedido de acesso registrado: usuário {}, pedido {}",
                solicitante.getId(), pedido.getId());

        return SolicitacaoAcessoResposta.de(pedido.getId(), solicitante.getStatus());
    }

    /** O formulário manda string vazia tão facilmente quanto nulo; a coluna prefere nulo. */
    private static String vazioViraNulo(String valor) {
        if (valor == null) {
            return null;
        }
        String limpo = valor.trim();
        return limpo.isEmpty() ? null : limpo;
    }
}
