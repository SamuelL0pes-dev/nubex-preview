package br.edu.unifor.nubex.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Documentação Swagger/OpenAPI (RNF018). O título usa o nome do serviço
 * (spring.application.name), então cada um dos 5 serviços mostra o seu.
 * A interface fica em /swagger-ui.html e só liga quando springdoc.swagger-ui.enabled=true.
 */
@Configuration
public class OpenApiConfig {

    private static final String ESQUEMA_BEARER = "bearerAuth";

    @Bean
    OpenAPI openAPI(@Value("${spring.application.name:nubex}") String nomeServico) {
        return new OpenAPI()
                .info(new Info()
                        .title("NUBEX - " + nomeServico)
                        .description("API do Portal NUBEX")
                        .version("0.1.0"))
                .components(new Components().addSecuritySchemes(ESQUEMA_BEARER,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")))
                .addSecurityItem(new SecurityRequirement().addList(ESQUEMA_BEARER));
    }
}
