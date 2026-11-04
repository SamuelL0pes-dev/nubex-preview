package br.edu.unifor.nubex.common.config;

import java.util.Set;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Propriedades "nubex.sessao.*": os dois atributos do cookie de acesso que mudam
 * de um ambiente para outro.
 *
 * <p>O que <b>não</b> é configurável está fixo no {@link CookieDeSessao} de
 * propósito: {@code HttpOnly} e {@code Path} não têm motivo legítimo para variar,
 * e deixar o {@code HttpOnly} desligável seria oferecer um jeito de desfazer a
 * própria proteção que motivou a mudança para cookie.
 *
 * <p>Os padrões são os seguros. Quem quiser afrouxar tem que escrever no
 * ambiente, e aparece na revisão.
 *
 * @param cookieSeguro liga o atributo {@code Secure}, que faz o navegador só
 *     mandar o cookie por HTTPS. Padrão {@code true}, e serve em
 *     desenvolvimento: o navegador trata {@code localhost} como origem segura e
 *     aceita cookie {@code Secure} mesmo em {@code http://localhost}. Só precisa
 *     virar {@code false} se o front for aberto por um endereço de rede em HTTP
 *     puro (por exemplo {@code http://192.168.0.10:5173}), onde o navegador
 *     descartaria o cookie em silêncio.
 * @param cookieSameSite {@code Lax}, {@code Strict} ou {@code None}. Padrão
 *     {@code Lax}, que é o que protege contra CSRF enquanto o token de CSRF não
 *     existir: o navegador não manda o cookie em requisição que muda estado vinda
 *     de outro site. {@code None} só é necessário se o portal e a API ficarem em
 *     domínios diferentes -- porta diferente não conta, {@code localhost:5173} e
 *     {@code localhost:8081} são o mesmo site para esta regra.
 */
@ConfigurationProperties(prefix = "nubex.sessao")
public record NubexSessaoProperties(Boolean cookieSeguro, String cookieSameSite) {

    private static final Set<String> VALORES_DE_SAME_SITE = Set.of("Lax", "Strict", "None");

    public NubexSessaoProperties {
        cookieSeguro = (cookieSeguro == null) ? Boolean.TRUE : cookieSeguro;
        cookieSameSite = normalizar(cookieSameSite);

        /*
         * Falha alta, no mesmo espírito da chave malformada do
         * ConferenciaDeTokenConfig: estes dois erros não produzem sintoma
         * nenhum no servidor. O navegador recebe o Set-Cookie, descarta
         * calado, e o que se vê é "o login não entra" sem uma linha de log em
         * lugar nenhum. Barato de recusar agora, caro de achar depois.
         */
        if (!VALORES_DE_SAME_SITE.contains(cookieSameSite)) {
            throw new IllegalStateException(
                    "nubex.sessao.cookie-same-site aceita Lax, Strict ou None. Recebido: " + cookieSameSite);
        }
        if ("None".equals(cookieSameSite) && !cookieSeguro) {
            throw new IllegalStateException(
                    "nubex.sessao.cookie-same-site=None exige nubex.sessao.cookie-seguro=true: "
                            + "o navegador descarta cookie SameSite=None sem Secure.");
        }
    }

    /** Aceita "lax", "LAX" e "Lax" como a mesma coisa; vazio cai no padrão. */
    private static String normalizar(String valor) {
        if (valor == null || valor.isBlank()) {
            return "Lax";
        }
        String limpo = valor.trim();
        return limpo.substring(0, 1).toUpperCase() + limpo.substring(1).toLowerCase();
    }
}
