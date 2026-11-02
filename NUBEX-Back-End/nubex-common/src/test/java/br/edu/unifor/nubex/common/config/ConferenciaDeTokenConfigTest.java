package br.edu.unifor.nubex.common.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.JwtDecoder;

/**
 * Os dois comportamentos de borda da conferência do token, que são diferentes
 * entre si e fáceis de confundir:
 *
 * <ul>
 *   <li><b>chave ausente</b>: a aplicação sobe e recusa todo token. É a falha
 *       fechada, e é a situação do CI, que não define chave nenhuma;
 *   <li><b>chave malformada</b>: a aplicação <b>não</b> sobe. Chave torta é erro
 *       de digitação em configuração, não estado de ambiente -- falhar alto, com
 *       a propriedade nomeada, poupa horas de caça a 401.
 * </ul>
 *
 * <p>Usa {@link ApplicationContextRunner} em vez de {@code @SpringBootTest}
 * porque aqui não há banco nem web: sobe só esta configuração, sem Docker, em
 * milissegundos. Importa, porque este código roda nos cinco serviços.
 */
class ConferenciaDeTokenConfigTest {

    /** Base64 válido, mas que não é uma chave: passa do decode e falha no KeyFactory. */
    private static final String BASE64_QUE_NAO_E_CHAVE = "bm9wZQ==";

    @Test
    void semChaveAplicacaoSobeERecusaQualquerToken() {
        runner("").run(contexto -> {
            assertThat(contexto).hasNotFailed();

            JwtDecoder conferidor = contexto.getBean(JwtDecoder.class);
            assertThatThrownBy(() -> conferidor.decode("qualquer.coisa.aqui"))
                    .isInstanceOf(BadJwtException.class);
        });
    }

    @Test
    void chaveQueNaoEBase64DerrubaASubidaComMensagemUtil() {
        runner("isto-nao-e-uma-chave").run(contexto ->
                assertThat(contexto).hasFailed()
                        .getFailure()
                        .hasMessageContaining("nubex.jwt.chave-publica inválida"));
    }

    @Test
    void base64QueNaoEChaveDerrubaASubidaComMensagemUtil() {
        runner(BASE64_QUE_NAO_E_CHAVE).run(contexto ->
                assertThat(contexto).hasFailed()
                        .getFailure()
                        .hasMessageContaining("nubex.jwt.chave-publica inválida"));
    }

    /**
     * As propriedades entram como bean pronto, e não por {@code withPropertyValues},
     * para o teste não depender da ligação do @ConfigurationProperties -- o que
     * está sendo testado é a decisão do conferidor, não o binding do Spring.
     */
    private static ApplicationContextRunner runner(String chavePublica) {
        return new ApplicationContextRunner()
                .withBean(NubexJwtProperties.class,
                        () -> new NubexJwtProperties(chavePublica, "", null, null))
                .withUserConfiguration(ConferenciaDeTokenConfig.class);
    }
}
