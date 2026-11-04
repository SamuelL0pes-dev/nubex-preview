package br.edu.unifor.nubex.usuario.controller;

import br.edu.unifor.nubex.common.config.CookieDeSessao;
import br.edu.unifor.nubex.usuario.dto.LoginRequisicao;
import br.edu.unifor.nubex.usuario.dto.LoginResposta;
import br.edu.unifor.nubex.usuario.service.AutenticacaoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Entrada e saída do sistema.
 *
 * <p>As duas rotas ficam abertas, e têm que ficar: quem vai fazer login ainda não
 * tem token, e quem vai sair pode estar com o token já vencido -- veja o
 * {@link #sair()}. A liberação não está escrita aqui; ela é declarada em
 * {@code nubex.security.public-routes} no application.properties, que é o
 * mecanismo que o nubex-common já usa. Assim todas as rotas públicas do serviço
 * ficam listadas num lugar só, em vez de espalhadas pelos controllers.
 *
 * <p>Nenhuma das duas leva {@code @PreAuthorize} pelo mesmo motivo. Todas as
 * outras rotas que vierem depois levam.
 *
 * <h4>O token sai em cookie, não no corpo</h4>
 *
 * <p>Era no corpo até esta mudança. O problema do corpo é que obriga o front a
 * guardar o token em algum lugar que o JavaScript lê, e aí qualquer XSS no portal
 * leva a credencial. Em cookie {@code HttpOnly}, o navegador manda sozinho em
 * cada requisição e nenhum script consegue ler. Quem monta o cookie é o
 * {@link CookieDeSessao}, no nubex-common, que também é onde o nome dele está
 * definido para os cinco serviços lerem.
 */
@RestController
@RequestMapping("/api/auth")
public class AutenticacaoController {

    private final AutenticacaoService autenticacaoService;
    private final CookieDeSessao cookieDeSessao;

    public AutenticacaoController(
            AutenticacaoService autenticacaoService, CookieDeSessao cookieDeSessao) {
        this.autenticacaoService = autenticacaoService;
        this.cookieDeSessao = cookieDeSessao;
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
     * <p>Respostas: 200 com o cookie de acesso e os dados da tela no corpo; 400
     * se faltar campo; 401 se o identificador ou a senha estiverem errados; 403
     * se a senha estiver certa mas a conta não estiver liberada; 503 se o serviço
     * estiver sem chave de assinatura.
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResposta> entrar(@Valid @RequestBody LoginRequisicao requisicao) {
        AutenticacaoService.Acesso acesso = autenticacaoService.entrar(requisicao);
        return ResponseEntity.ok()
                .header(
                        HttpHeaders.SET_COOKIE,
                        cookieDeSessao.criar(acesso.token(), acesso.validadeEmSegundos()).toString())
                .body(acesso.corpo());
    }

    /**
     * POST /api/auth/logout
     *
     * <p>Manda o navegador descartar o cookie de acesso. É o que faltava para o
     * RF087 ("encerrar a sessão na opção Sair do Sistema") poder ser cumprido:
     * enquanto o token era guardado pelo front, sair era o front esquecer o
     * token; agora que ele é {@code HttpOnly}, <b>o front não consegue
     * apagá-lo</b> -- só o servidor apaga, mandando um cookie substituto já
     * vencido.
     *
     * <h4>Por que é rota pública</h4>
     *
     * <p>Parece errado e não é. Se o logout exigisse token válido, quem voltasse
     * depois do token vencer tomaria 401 ao tentar sair, e ficaria com um cookie
     * preso no navegador sem nenhuma forma de removê-lo pela interface. Fazer a
     * rota pública resolve isso sem abrir nada: ela não lê nem escreve no banco,
     * não consulta quem está chamando, e o único efeito é o navegador de quem
     * chamou apagar o próprio cookie.
     *
     * <p>O que isso permite é um site malicioso forçar o logout de alguém:
     * chateação, não vazamento. Nada é lido, nada é alterado, e a pessoa entra de
     * novo com a senha dela.
     *
     * <p><b>E o {@code SameSite=Lax} não impede isso</b> -- vale registrar por
     * escrito, porque é tentador achar que impede. O {@code SameSite} governa o
     * <b>envio</b> do cookie, e esta rota <b>não precisa de cookie para
     * funcionar</b>: ela só responde com um cookie de limpeza. Um formulário
     * auto-submetido de outro site é requisição simples, então não há consulta
     * prévia de CORS para recusar a origem; a requisição chega, a rota é pública,
     * e o navegador aplica o {@code Set-Cookie} da resposta.
     *
     * <p>Quem fecharia essa porta é o token de CSRF, que o SecurityConfig explica
     * por que ainda não está ligado. Até lá isto é um incômodo aceito de olhos
     * abertos, não um descuido -- e preferível a travar quem volta com o token
     * vencido sem conseguir sair.
     *
     * <p>Responde 204 sem corpo: não há nada a dizer, e sair não falha. Mesmo sem
     * cookie nenhum na requisição, a resposta é a mesma -- dizer "você não estava
     * logado" seria informação sobre o estado de quem chamou, sem utilidade para
     * a tela.
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> sair() {
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, cookieDeSessao.limpar().toString())
                .build();
    }
}
