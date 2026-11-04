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
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Segurança compartilhada pelos cinco serviços (RNF013, RF073).
 *
 * Regras:
 *  - API sem estado (sem sessão do servidor): cada requisição precisa trazer o
 *    próprio token. O token viaja em cookie HttpOnly, não em sessão -- nada do
 *    estado de quem entrou fica guardado aqui;
 *  - CSRF desligado, e a proteção contra ele hoje é o SameSite=Lax do cookie:
 *    o navegador não manda o cookie em requisição que muda estado vinda de
 *    outro site, e nossa API não tem GET que mude estado. Isso cobre o ataque;
 *    o token de CSRF (CookieCsrfTokenRepository mais o front mandando o
 *    cabeçalho X-XSRF-TOKEN) é reforço em cima, e exige mudança no front --
 *    ligar só deste lado quebraria todo POST dele, login incluído;
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
            BearerTokenResolver resolvedorDeToken,
            @Value("${springdoc.swagger-ui.enabled:false}") boolean swaggerLigado) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable())
                /*
                 * Liga o filtro que acha o token na requisição, confere a
                 * assinatura com a chave pública e põe o usuário no contexto. É o
                 * que faz o @PreAuthorize ter com quem comparar.
                 */
                .oauth2ResourceServer(recurso -> recurso
                        /*
                         * Onde procurar o token. Sem isto, o Spring olharia só o
                         * cabeçalho Authorization, e o cookie HttpOnly que o front
                         * manda em toda requisição chegaria aqui sem ser lido: 401
                         * em tudo depois do login, com a credencial certa viajando
                         * na requisição. Veja o ResolvedorDeTokenDeAcesso.
                         */
                        .bearerTokenResolver(resolvedorDeToken)
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
     *
     * <p>O setAllowCredentials(true) é o que permite cookie em chamada de outra
     * origem, e em desenvolvimento toda chamada é de outra origem: o front está
     * em localhost:5173 e a API em localhost:8081. Sem ele, o navegador recebe a
     * resposta do login, não encontra o cabeçalho
     * Access-Control-Allow-Credentials e <b>descarta a resposta inteira</b> --
     * mesmo com status 200 e com o Set-Cookie dentro dela. O fetch rejeita, e o
     * erro aparece só no console do navegador, não no log do servidor. É o tipo
     * de falha que se procura no lugar errado por horas.
     *
     * <p>Combinar credenciais com origem "*" é proibido pela especificação, e o
     * próprio Spring recusa subir nesse caso -- a verificação abaixo já
     * impedia antes, e agora tem um segundo motivo para existir.
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
        config.setAllowCredentials(true);
        config.setMaxAge(props.maxAge());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    /**
     * Onde o token é procurado em cada requisição: cookie primeiro, cabeçalho
     * como reserva. Fica nos cinco serviços de uma vez, porque mora aqui.
     */
    @Bean
    @ConditionalOnMissingBean(BearerTokenResolver.class)
    BearerTokenResolver bearerTokenResolver() {
        return new ResolvedorDeTokenDeAcesso();
    }

    /**
     * Monta o cookie de acesso. Só o usuario-service usa, porque só ele faz
     * login, mas o bean nasce aqui porque o nome do cookie é o mesmo que o
     * resolvedor acima procura -- os dois lados do acordo no mesmo lugar.
     */
    @Bean
    @ConditionalOnMissingBean(CookieDeSessao.class)
    CookieDeSessao cookieDeSessao(NubexSessaoProperties propriedades) {
        return new CookieDeSessao(propriedades);
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
