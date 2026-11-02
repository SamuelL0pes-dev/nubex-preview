package br.edu.unifor.nubex.usuario.controller;

import br.edu.unifor.nubex.usuario.dto.LoginRequisicao;
import br.edu.unifor.nubex.usuario.dto.LoginResposta;
import br.edu.unifor.nubex.usuario.service.AutenticacaoService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Entrada no sistema.
 *
 * <p>É a única rota do usuario-service que fica aberta, e tem que ser: quem vai
 * fazer login ainda não tem token. A liberação não está escrita aqui -- ela é
 * declarada em {@code nubex.security.public-routes} no application.properties,
 * que é o mecanismo que o nubex-common já usa. Assim todas as rotas públicas do
 * serviço ficam listadas num lugar só, em vez de espalhadas pelos controllers.
 *
 * <p>Não leva {@code @PreAuthorize} pelo mesmo motivo. Todas as outras rotas que
 * vierem depois levam.
 */
@RestController
@RequestMapping("/api/auth")
public class AutenticacaoController {

    private final AutenticacaoService autenticacaoService;

    public AutenticacaoController(AutenticacaoService autenticacaoService) {
        this.autenticacaoService = autenticacaoService;
    }

    /**
     * POST /api/auth/login
     *
     * <p>O {@code @Valid} aplica as regras do LoginRequisicao antes de o método
     * rodar: campo vazio ou longo demais já volta 400 com a lista de campos, no
     * formato do ApiError, sem chegar ao banco. É o que o RNF013 pede ao mandar
     * validar na API todos os dados recebidos, independentemente do que a
     * interface já validou.
     *
     * <p>Respostas: 200 com o token; 400 se faltar campo; 401 se o identificador
     * ou a senha estiverem errados; 403 se a senha estiver certa mas a conta não
     * estiver liberada; 503 se o serviço estiver sem chave de assinatura.
     */
    @PostMapping("/login")
    public LoginResposta entrar(@Valid @RequestBody LoginRequisicao requisicao) {
        return autenticacaoService.entrar(requisicao);
    }
}
