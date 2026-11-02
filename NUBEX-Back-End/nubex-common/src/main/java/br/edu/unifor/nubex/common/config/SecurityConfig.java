package br.edu.unifor.nubex.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Segurança compartilhada pelos cinco serviços (RNF013, RF073).
 *
 * Regras:
 *  - API sem estado (sem sessão): cada requisição precisa trazer o próprio token;
 *  - CSRF desligado, porque não usamos cookie de sessão (o ataque de CSRF
 *    depende de cookie enviado automaticamente pelo navegador);
 *  - tudo exige autenticação, exceto /actuator/health, o Swagger (quando ligado) e as
 *    rotas listadas em nubex.security.public-routes por cada serviço;
 *  - o token é conferido aqui mesmo, em cada serviço, pelo JwtDecoder que o
 *    ConferenciaDeTokenConfig monta a partir da chave pública;
 *  - 401 e 403 saem em JSON, no mesmo formato dos outros erros.
 *
 * Todos os beans abaixo usam @ConditionalOnMissingBean: se um serviço definir o próprio,
 * o do módulo comum sai de cena em vez de dar conflito.
 *
 * A permissão por perfil em cada rota (RNF013) se escreve com @PreAuthorize, que
 * está habilitado por causa do @EnableMethodSecurity. As autoridades que ele
 * compara vêm dos campos do token, traduzidas no ConferenciaDeTokenConfig.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    @ConditionalOnMissingBean(SecurityFilterChain.class)
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            CorsConfigurationSource corsConfigurationSource,
            ApiAuthenticationEntryPoint entryPoint,
            ApiAccessDeniedHandler accessDeniedHandler,
            NubexSecurityProperties securityProperties,
            JwtAuthenticationConverter conversorDeToken,
            @Value("${springdoc.swagger-ui.enabled:false}") boolean swaggerLigado) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable())
                /*
                 * Liga o filtro que lê o cabeçalho "Authorization: Bearer <token>",
                 * confere a assinatura com a chave pública e põe o usuário no
                 * contexto. É o que faz o @PreAuthorize ter com quem comparar.
                 */
                .oauth2ResourceServer(recurso -> recurso
                        /*
                         * Estes dois repetem o que o exceptionHandling acima já
                         * define, e precisam ser repetidos: sem eles, o Spring usa
                         * o tratamento próprio dele quando o token é inválido, e
                         * não o nosso. O resultado era 401 com corpo VAZIO e um
                         * cabeçalho WWW-Authenticate descrevendo o erro interno
                         * ("An error occurred while attempting to decode the Jwt:
                         * Malformed token") -- o que quebra o formato único do
                         * ApiError, que o front espera em toda resposta, e
                         * contraria o include-message: never do nubex-common.yml.
                         */
                        .authenticationEntryPoint(entryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                        .jwt(token -> token.jwtAuthenticationConverter(conversorDeToken)))
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(entryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers("/actuator/health", "/actuator/health/**").permitAll();
                    if (swaggerLigado) {
                        auth.requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll();
                    }
                    for (String rota : securityProperties.publicRoutes()) {
                        liberar(auth, rota);
                    }
                    auth.anyRequest().authenticated();
                });

        return http.build();
    }

    /**
     * CORS: só as origens configuradas podem chamar a API pelo navegador.
     * "*" é proibido de propósito: liberar tudo anula a proteção.
     */
    @Bean
    @ConditionalOnMissingBean(CorsConfigurationSource.class)
    CorsConfigurationSource corsConfigurationSource(CorsProperties props) {
        if (props.allowedOrigins().contains("*")) {
            throw new IllegalStateException("nubex.cors.allowed-origins não pode conter '*'. Liste as origens.");
        }
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(props.allowedOrigins());
        config.setAllowedMethods(props.allowedMethods());
        config.setAllowedHeaders(props.allowedHeaders());
        config.setMaxAge(props.maxAge());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    /** Senhas só como hash BCrypt: usado para gravar no cadastro e conferir no login. */
    @Bean
    @ConditionalOnMissingBean(PasswordEncoder.class)
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Não participa do login por token, e continua aqui de propósito.
     *
     * O login confere a senha chamando o PasswordEncoder direto, sem passar por
     * UserDetailsService. Mas, se este bean não existisse, o Spring Boot criaria
     * um usuário "user" com senha aleatória e escreveria essa senha no log --
     * exatamente o que queremos evitar. Ele existe para ocupar o lugar e recusar.
     */
    @Bean
    @ConditionalOnMissingBean(UserDetailsService.class)
    UserDetailsService userDetailsService() {
        return username -> {
            throw new UsernameNotFoundException("Autenticação ainda não implementada");
        };
    }

    /** Interpreta um item de nubex.security.public-routes: "GET /api/x/**" ou só "/api/x/**". */
    private static void liberar(
            AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth,
            String rota) {
        String[] partes = rota.trim().split("\\s+", 2);
        if (partes.length == 2) {
            auth.requestMatchers(HttpMethod.valueOf(partes[0].toUpperCase()), partes[1]).permitAll();
        } else {
            auth.requestMatchers(partes[0]).permitAll();
        }
    }
}
