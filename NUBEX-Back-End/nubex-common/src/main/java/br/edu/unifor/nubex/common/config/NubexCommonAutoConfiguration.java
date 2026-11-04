package br.edu.unifor.nubex.common.config;

import br.edu.unifor.nubex.common.exception.GlobalExceptionHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

/**
 * Ponto de entrada da configuração compartilhada. Basta o serviço ter o
 * nubex-common como dependência: o Spring Boot encontra esta classe pelo arquivo
 * META-INF/spring/...AutoConfiguration.imports e registra tudo sozinho.
 *
 * O "before" garante que a nossa segurança seja registrada antes da segurança
 * padrão do Spring Boot (que, senão, criaria um usuário com senha gerada no log).
 */
@AutoConfiguration(beforeName = {
        "org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration",
        "org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration",
        "org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration"
})
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@EnableConfigurationProperties({
        CorsProperties.class,
        NubexSecurityProperties.class,
        NubexJwtProperties.class,
        NubexSessaoProperties.class
})
@Import({
        SecurityConfig.class,
        ConferenciaDeTokenConfig.class,
        ApiAuthenticationEntryPoint.class,
        ApiAccessDeniedHandler.class,
        OpenApiConfig.class,
        GlobalExceptionHandler.class
})
public class NubexCommonAutoConfiguration {
}
