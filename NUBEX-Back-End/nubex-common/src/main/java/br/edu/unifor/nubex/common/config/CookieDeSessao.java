package br.edu.unifor.nubex.common.config;

import java.time.Duration;
import org.springframework.http.ResponseCookie;

/**
 * Monta o cookie que carrega o token de acesso (RNF013).
 *
 * <h4>Por que o token virou cookie</h4>
 *
 * <p>Antes o login devolvia o token no corpo da resposta, e o front tinha que
 * guardá-lo em algum lugar que o JavaScript lê. Qualquer falha de XSS no portal
 * -- um comentário, um nome de laboratório, qualquer texto nosso que chegue à
 * tela sem escapar -- levaria a credencial inteira junto. Com {@code HttpOnly},
 * o navegador manda o cookie sozinho em cada requisição e <b>nenhum JavaScript
 * consegue lê-lo</b>, nem o nosso nem o injetado.
 *
 * <h4>Por que esta classe mora no nubex-common</h4>
 *
 * <p>O usuario-service <b>escreve</b> o cookie (é o único que faz login) e os
 * cinco serviços <b>leem</b> (pelo {@link ResolvedorDeTokenDeAcesso}). O nome do
 * cookie é o acordo entre os dois lados: escrito em dois lugares, um dia um é
 * corrigido e o outro não, e o sintoma é 401 em tudo sem erro nenhum no log. Como
 * constante num lugar só, isso não acontece.
 *
 * <h4>Um cookie para cinco serviços</h4>
 *
 * <p>Isto funciona em desenvolvimento por um detalhe que vale saber: <b>cookie
 * ignora porta</b>. O login responde em {@code localhost:8081}, o cookie fica
 * registrado para o host {@code localhost}, e o navegador o manda também para
 * {@code localhost:8082}, {@code :8083}, {@code :8084} e {@code :8085}. Os cinco
 * serviços recebem a credencial sem nenhuma configuração a mais.
 *
 * <p><b>Em produção isso deixa de valer se os serviços ficarem em hosts
 * diferentes.</b> Sem o atributo {@code Domain}, o cookie é amarrado ao host
 * exato que o criou: um cookie nascido em {@code usuarios.nubex.br} não é mandado
 * para {@code catalogo.nubex.br}, e o login passaria a funcionar só no serviço de
 * usuários -- os outros quatro responderiam 401 sem nada no log explicando. Nesse
 * cenário o cookie precisa de {@code Domain=.nubex.br}, e os cinco têm que estar
 * sob esse domínio. Com todos atrás de um mesmo host (um proxy na frente,
 * separando por caminho), nada disso é necessário.
 */
public class CookieDeSessao {

    /**
     * O nome do cookie, combinado com o front.
     *
     * <p>Sem o prefixo {@code __Host-}, que seria mais seguro, porque ele exige
     * que o cookie venha do mesmo host da página, e em desenvolvimento o front
     * está em {@code localhost:5173} e a API em {@code localhost:8081}. Vale
     * revisitar quando portal e API estiverem atrás do mesmo domínio.
     */
    public static final String NOME = "nubex_acesso";

    /**
     * {@code Path=/} e não {@code /api}: o logout tem que apagar o mesmo cookie
     * que o login criou, e o navegador só considera o par (nome, path). Com
     * paths diferentes, o apagar não acha o que apagar e a pessoa continua
     * logada.
     */
    private static final String CAMINHO = "/";

    private final NubexSessaoProperties propriedades;

    public CookieDeSessao(NubexSessaoProperties propriedades) {
        this.propriedades = propriedades;
    }

    /**
     * O cookie da entrada.
     *
     * <p>A validade acompanha a do token de propósito, mas não manda nela: quem
     * decide se o token vale ainda é o {@code exp} de dentro dele, conferido pelo
     * {@code JwtTimestampValidator} em cada serviço. O {@code Max-Age} só evita
     * que o navegador guarde um cookie que já não serve para nada -- um cookie
     * com prazo maior que o do token não dá acesso nenhum, só produz 401.
     *
     * <p>O valor não precisa de codificação: um JWT é formado por base64url e
     * pontos, e todos esses caracteres são válidos num valor de cookie.
     */
    public ResponseCookie criar(String token, long validadeEmSegundos) {
        return comum()
                .value(token)
                .maxAge(Duration.ofSeconds(validadeEmSegundos))
                .build();
    }

    /**
     * O cookie da saída: valor vazio e {@code Max-Age=0}, que é como se apaga um
     * cookie -- não existe "remover", existe mandar um substituto já vencido.
     *
     * <p>Os outros atributos têm que ser <b>idênticos</b> aos do {@link #criar}.
     * É a parte que costuma dar errado: se o {@code Path} ou o {@code SameSite}
     * divergirem, o navegador entende que são dois cookies diferentes, guarda o
     * vencido e mantém o válido. O logout responde 204, parece ter funcionado, e
     * a pessoa segue autenticada.
     */
    public ResponseCookie limpar() {
        return comum()
                .value("")
                .maxAge(Duration.ZERO)
                .build();
    }

    /** Os atributos que os dois cookies compartilham, para não divergirem. */
    private ResponseCookie.ResponseCookieBuilder comum() {
        return ResponseCookie.from(NOME)
                .httpOnly(true)
                .secure(propriedades.cookieSeguro())
                .sameSite(propriedades.cookieSameSite())
                .path(CAMINHO);
    }
}
