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
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Esqueleto do Spring Security (RNF013). Nesta semana (S1) ele só define as
 * regras gerais; o login e a verificação de token entram na S2.
 *
 * Regras:
 *  - API sem estado (sem sessão): cada requisição precisa trazer o próprio token;
 *  - CSRF desligado, porque não usamos cookie de sessão (o ataque de CSRF
 *    depende de cookie enviado automaticamente pelo navegador);
 *  - tudo exige autenticação, exceto /actuator/health, o Swagger (quando ligado) e as
 *    rotas listadas em nubex.security.public-routes por cada serviço;
 *  - 401 e 403 saem em JSON, no mesmo formato dos outros erros.
 *
 * Todos os beans abaixo usam @ConditionalOnMissingBean: se um serviço definir o próprio
 * (por exemplo, o usuario-service com o login real na S2), o do módulo comum sai de cena
 * em vez de dar conflito.
 *
 * A permissão por perfil em cada rota (RNF013) virá com @PreAuthorize, que já
 * está habilitado por causa do @EnableMethodSecurity.
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
            @Value("${springdoc.swagger-ui.enabled:false}") boolean swaggerLigado) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable())
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

    /** Senhas só como hash BCrypt (usado no cadastro e no login, a partir da S2). */
    @Bean
    @ConditionalOnMissingBean(PasswordEncoder.class)
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Provisório. Sem este bean, o Spring Boot cria um usuário "user" com senha
     * aleatória e escreve essa senha no log, o que é exatamente o que queremos
     * evitar. Este serviço sempre recusa, até o login real ser implementado (S2).
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
