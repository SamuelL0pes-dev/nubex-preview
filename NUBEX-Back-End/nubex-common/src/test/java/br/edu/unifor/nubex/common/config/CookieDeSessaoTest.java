package br.edu.unifor.nubex.common.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseCookie;

/**
 * O cookie de acesso e as duas recusas de configuração que o acompanham.
 *
 * <p>Todo erro testado aqui é silencioso em produção: o navegador recebe o
 * Set-Cookie, descarta sem reclamar, e o que se vê é "o login não entra" sem uma
 * linha de log em lugar nenhum.
 */
class CookieDeSessaoTest {

    private static final String TOKEN = "um.token.qualquer";
    private static final long DUAS_HORAS = 7200L;

    private static CookieDeSessao com(Boolean seguro, String sameSite) {
        return new CookieDeSessao(new NubexSessaoProperties(seguro, sameSite));
    }

    // ---- O cookie da entrada ----

    @Test
    void cookieDeEntradaProtegeONavegadorPorPadrao() {
        ResponseCookie cookie = com(null, null).criar(TOKEN, DUAS_HORAS);

        assertThat(cookie.getName()).isEqualTo("nubex_acesso");
        assertThat(cookie.getValue()).isEqualTo(TOKEN);
        assertThat(cookie.isHttpOnly()).isTrue();
        assertThat(cookie.isSecure()).isTrue();
        assertThat(cookie.getSameSite()).isEqualTo("Lax");
        assertThat(cookie.getPath()).isEqualTo("/");
        assertThat(cookie.getMaxAge().toSeconds()).isEqualTo(DUAS_HORAS);
    }

    // ---- O cookie da saída ----

    /**
     * Não existe "remover cookie": existe mandar um substituto já vencido. Valor
     * vazio e Max-Age=0 são o que faz o navegador descartar.
     */
    @Test
    void cookieDeSaidaVemVazioEVencido() {
        ResponseCookie cookie = com(null, null).limpar();

        assertThat(cookie.getValue()).isEmpty();
        assertThat(cookie.getMaxAge()).isZero();
    }

    /**
     * O teste que importa do logout. O navegador identifica um cookie pelo nome
     * mais o path (e o domínio): se o cookie de saída divergir do de entrada em
     * qualquer um desses, ele é entendido como <b>outro</b> cookie. O vencido é
     * guardado, o válido fica, o logout responde 204 e a pessoa segue
     * autenticada.
     */
    @Test
    void saidaUsaOsMesmosAtributosDaEntrada() {
        CookieDeSessao cookieDeSessao = com(false, "Strict");

        ResponseCookie entrada = cookieDeSessao.criar(TOKEN, DUAS_HORAS);
        ResponseCookie saida = cookieDeSessao.limpar();

        assertThat(saida.getName()).isEqualTo(entrada.getName());
        assertThat(saida.getPath()).isEqualTo(entrada.getPath());
        assertThat(saida.getSameSite()).isEqualTo(entrada.getSameSite());
        assertThat(saida.isHttpOnly()).isEqualTo(entrada.isHttpOnly());
        assertThat(saida.isSecure()).isEqualTo(entrada.isSecure());
    }

    // ---- As propriedades ----

    @Test
    void aceitaSameSiteEmQualquerCaixa() {
        assertThat(com(null, "lax").criar(TOKEN, DUAS_HORAS).getSameSite()).isEqualTo("Lax");
        assertThat(com(null, "NONE").criar(TOKEN, DUAS_HORAS).getSameSite()).isEqualTo("None");
        assertThat(com(null, " strict ").criar(TOKEN, DUAS_HORAS).getSameSite()).isEqualTo("Strict");
    }

    /** Valor de SameSite que o navegador não conhece faz ele ignorar o atributo. */
    @Test
    void recusaSubirComSameSiteInvalido() {
        assertThatThrownBy(() -> com(null, "Nenhum"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nubex.sessao.cookie-same-site");
    }

    /**
     * A especificação exige Secure junto de SameSite=None, e o navegador descarta
     * o cookie se faltar. Quem configura None é justamente quem está pondo o
     * portal e a API em domínios diferentes -- a hora de errar com menos pistas.
     */
    @Test
    void recusaSubirComSameSiteNoneSemSecure() {
        assertThatThrownBy(() -> com(false, "None"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cookie-seguro");
    }
}
