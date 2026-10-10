package br.edu.unifor.nubex.common.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Propriedades "nubex.security.*". Cada serviço declara no próprio application.yml
 * as rotas públicas dele (as que não exigem login), por exemplo:
 *
 * <pre>
 * nubex:
 *   security:
 *     public-routes:
 *       - GET /api/unidades/**
 *       - POST /api/contato
 * </pre>
 *
 * Formato de cada item: "MÉTODO /caminho" (recomendado) ou só "/caminho", que
 * libera todos os métodos. Prefira sempre informar o método, para não abrir
 * um POST ou DELETE sem querer.
 */
@ConfigurationProperties(prefix = "nubex.security")
public record NubexSecurityProperties(List<String> publicRoutes) {

    public NubexSecurityProperties {
        publicRoutes = (publicRoutes == null) ? List.of() : List.copyOf(publicRoutes);
    }
}
