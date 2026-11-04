package br.edu.unifor.nubex.common.config;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 * Onde o token é procurado em cada requisição.
 *
 * <p>Vale um teste próprio porque esta classe é o ponto único de falha da
 * mudança para cookie: se ela errar, nenhuma requisição autentica em nenhum dos
 * cinco serviços, e o sintoma é 401 em tudo -- com a credencial correta chegando
 * ao servidor, só não lida.
 */
class ResolvedorDeTokenDeAcessoTest {

    private static final String TOKEN_DO_COOKIE = "token-vindo-do-cookie";
    private static final String TOKEN_DO_CABECALHO = "token-vindo-do-cabecalho";

    private final ResolvedorDeTokenDeAcesso resolvedor = new ResolvedorDeTokenDeAcesso();

    @Test
    void achaOTokenNoCookie() {
        MockHttpServletRequest requisicao = new MockHttpServletRequest();
        requisicao.setCookies(new Cookie(CookieDeSessao.NOME, TOKEN_DO_COOKIE));

        assertThat(resolvedor.resolve(requisicao)).isEqualTo(TOKEN_DO_COOKIE);
    }

    /** A reserva que mantém o Swagger, o curl e os testes por cabeçalho funcionando. */
    @Test
    void semCookieCaiNoCabecalhoAuthorization() {
        MockHttpServletRequest requisicao = new MockHttpServletRequest();
        requisicao.addHeader("Authorization", "Bearer " + TOKEN_DO_CABECALHO);

        assertThat(resolvedor.resolve(requisicao)).isEqualTo(TOKEN_DO_CABECALHO);
    }

    /** Com os dois presentes, vale o cookie. */
    @Test
    void cookieTemPrecedenciaSobreOCabecalho() {
        MockHttpServletRequest requisicao = new MockHttpServletRequest();
        requisicao.setCookies(new Cookie(CookieDeSessao.NOME, TOKEN_DO_COOKIE));
        requisicao.addHeader("Authorization", "Bearer " + TOKEN_DO_CABECALHO);

        assertThat(resolvedor.resolve(requisicao)).isEqualTo(TOKEN_DO_COOKIE);
    }

    /**
     * É o estado em que o logout deixa o navegador: o cookie continua registrado,
     * com valor vazio, até o prazo zerado fazer efeito. Tem que contar como
     * ausente -- se contasse como presente, viraria uma string vazia entregue ao
     * conferidor de token, e quem tivesse mandado cabeçalho junto não seria
     * autenticado.
     */
    @Test
    void cookieVazioContaComoAusenteECaiNaReserva() {
        MockHttpServletRequest requisicao = new MockHttpServletRequest();
        requisicao.setCookies(new Cookie(CookieDeSessao.NOME, ""));
        requisicao.addHeader("Authorization", "Bearer " + TOKEN_DO_CABECALHO);

        assertThat(resolvedor.resolve(requisicao)).isEqualTo(TOKEN_DO_CABECALHO);
    }

    /**
     * O caso que pega o erro de parar no primeiro cookie encontrado.
     *
     * <p>Dois cookies com o mesmo nome chegam juntos quando o escopo deles muda
     * -- é o que vai acontecer no dia em que o cookie ganhar o atributo
     * {@code Domain} para atravessar os hosts dos cinco serviços: quem tinha o
     * antigo, amarrado a um host só, fica com os dois registrados. A ordem de
     * chegada não é definida por especificação nenhuma.
     *
     * <p>Se o resolvedor parasse no primeiro, a pessoa seria tratada como anônima
     * em toda requisição, com o cookie válido chegando ao servidor e nada no log.
     */
    @Test
    void comDoisCookiesDeMesmoNomeValeOQueTemConteudo() {
        MockHttpServletRequest vazioPrimeiro = new MockHttpServletRequest();
        vazioPrimeiro.setCookies(
                new Cookie(CookieDeSessao.NOME, ""),
                new Cookie(CookieDeSessao.NOME, TOKEN_DO_COOKIE));

        assertThat(resolvedor.resolve(vazioPrimeiro)).isEqualTo(TOKEN_DO_COOKIE);

        // E na ordem inversa, para não passar por acidente.
        MockHttpServletRequest vazioDepois = new MockHttpServletRequest();
        vazioDepois.setCookies(
                new Cookie(CookieDeSessao.NOME, TOKEN_DO_COOKIE),
                new Cookie(CookieDeSessao.NOME, ""));

        assertThat(resolvedor.resolve(vazioDepois)).isEqualTo(TOKEN_DO_COOKIE);
    }

    /**
     * Mesmo caso, mas com todos vazios: aí não há token nenhum, e tem que cair na
     * reserva em vez de devolver string vazia ao conferidor.
     */
    @Test
    void comVariosCookiesTodosVaziosCaiNaReserva() {
        MockHttpServletRequest requisicao = new MockHttpServletRequest();
        requisicao.setCookies(
                new Cookie(CookieDeSessao.NOME, ""),
                new Cookie(CookieDeSessao.NOME, "  "));
        requisicao.addHeader("Authorization", "Bearer " + TOKEN_DO_CABECALHO);

        assertThat(resolvedor.resolve(requisicao)).isEqualTo(TOKEN_DO_CABECALHO);
    }

    @Test
    void semCookieESemCabecalhoNaoAchaNada() {
        assertThat(resolvedor.resolve(new MockHttpServletRequest())).isNull();
    }

    /** Cookie de outro nome não serve, mesmo parecendo um token. */
    @Test
    void ignoraCookieDeOutroNome() {
        MockHttpServletRequest requisicao = new MockHttpServletRequest();
        requisicao.setCookies(new Cookie("outro_cookie", TOKEN_DO_COOKIE));

        assertThat(resolvedor.resolve(requisicao)).isNull();
    }

    /**
     * O token na URL continua recusado. Endereço vai para log de servidor,
     * histórico do navegador e cabeçalho Referer de qualquer link clicado: três
     * cópias da credencial, duas delas fora do nosso alcance (RNF010).
     */
    @Test
    void naoAceitaTokenNaUrl() {
        MockHttpServletRequest requisicao = new MockHttpServletRequest();
        requisicao.setParameter("access_token", TOKEN_DO_CABECALHO);

        assertThat(resolvedor.resolve(requisicao)).isNull();
    }
}
