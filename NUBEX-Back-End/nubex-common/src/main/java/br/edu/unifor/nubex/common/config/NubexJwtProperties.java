package br.edu.unifor.nubex.common.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Propriedades "nubex.jwt.*": as chaves que assinam e conferem o token, quem o
 * emite e quanto tempo ele vale.
 *
 * <p>Quem recebe cada chave:
 *
 * <ul>
 *   <li><b>chave-publica</b>: os cinco serviços. Ela só <em>confere</em> assinatura.
 *       Quem tem apenas a pública não consegue produzir um token válido.
 *   <li><b>chave-privada</b>: somente o usuario-service, que é o único que faz
 *       login e portanto o único que precisa <em>assinar</em>. Nos outros quatro
 *       serviços esta propriedade fica vazia, de propósito.
 * </ul>
 *
 * <p>É por isso que a assinatura é assimétrica (RS256) em vez de um segredo único
 * compartilhado (HS256): com um segredo único, a mesma chave que confere também
 * assina, e qualquer um dos cinco serviços poderia fabricar um token de
 * ADMINISTRADOR. Aqui, vazar a configuração do catalogo-service não permite
 * forjar nada.
 *
 * <p>As duas chaves entram como o conteúdo base64 do DER, numa única linha, para
 * caber em arquivo .properties e em variável de ambiente sem quebra de linha. O
 * {@link ChavesRsa} também aceita o PEM inteiro colado, se alguém preferir.
 */
@ConfigurationProperties(prefix = "nubex.jwt")
public record NubexJwtProperties(
        String chavePublica,
        String chavePrivada,
        String emissor,
        Duration validade) {

    /** Vai no campo "iss" do token e é conferido na entrada de todo serviço. */
    public static final String EMISSOR_PADRAO = "nubex-usuario-service";

    /**
     * Duas horas. Curto de propósito: o RF087 pede encerrar a sessão em "Sair do
     * Sistema", mas um token sem estado no servidor não tem como ser cancelado --
     * sair apaga o token no navegador e pronto, quem já tivesse uma copia
     * continuaria entrando. A validade curta é o que limita esse prazo.
     */
    public static final Duration VALIDADE_PADRAO = Duration.ofHours(2);

    public NubexJwtProperties {
        chavePublica = (chavePublica == null) ? "" : chavePublica.trim();
        chavePrivada = (chavePrivada == null) ? "" : chavePrivada.trim();
        emissor = (emissor == null || emissor.isBlank()) ? EMISSOR_PADRAO : emissor.trim();
        validade = (validade == null) ? VALIDADE_PADRAO : validade;
    }

    public boolean temChavePublica() {
        return !chavePublica.isBlank();
    }

    public boolean temChavePrivada() {
        return !chavePrivada.isBlank();
    }

    /**
     * Sem isto, o toString() que o record gera sozinho imprimiria as duas chaves
     * inteiras -- e bastaria um log de erro do Spring citando o bean para a chave
     * privada ir para o arquivo de log. Aqui só dizemos se cada uma está definida.
     *
     * <p>Pelo mesmo motivo, o /actuator/env nunca deve ser habilitado: o Spring
     * mascara automaticamente propriedades cujo nome contém password, secret, key
     * ou token, e "chave-privada", em português, não está nessa lista. Hoje não há
     * risco, porque o nubex-common.yml expõe apenas o /actuator/health.
     */
    @Override
    public String toString() {
        return "NubexJwtProperties[emissor=" + emissor
                + ", validade=" + validade
                + ", chavePublica=" + (temChavePublica() ? "definida" : "ausente")
                + ", chavePrivada=" + (temChavePrivada() ? "definida" : "ausente")
                + "]";
    }
}
