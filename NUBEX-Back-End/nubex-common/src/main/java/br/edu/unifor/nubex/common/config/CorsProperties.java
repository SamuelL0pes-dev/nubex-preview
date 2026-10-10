package br.edu.unifor.nubex.common.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Propriedades "nubex.cors.*". As origens vêm de configuração (nubex-common.yml
 * ou variável de ambiente CORS_ORIGINS), nunca fixas no código.
 */
@ConfigurationProperties(prefix = "nubex.cors")
public record CorsProperties(
        List<String> allowedOrigins,
        List<String> allowedMethods,
        List<String> allowedHeaders,
        Long maxAge) {

    public CorsProperties {
        allowedOrigins = (allowedOrigins == null) ? List.of() : List.copyOf(allowedOrigins);
        allowedMethods = (allowedMethods == null || allowedMethods.isEmpty())
                ? List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                : List.copyOf(allowedMethods);
        allowedHeaders = (allowedHeaders == null || allowedHeaders.isEmpty())
                ? List.of("*")
                : List.copyOf(allowedHeaders);
        maxAge = (maxAge == null) ? 3600L : maxAge;
    }
}
