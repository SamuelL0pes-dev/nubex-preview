package br.edu.unifor.nubex.common.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

/**
 * Conferência do token em todo serviço (RNF013, RF073).
 *
 * <p>A decisão de arquitetura por trás desta classe: cada um dos cinco serviços
 * confere o token por conta própria, em vez de existir um ponto único de entrada
 * que confere e avisa os outros por um cabeçalho. O motivo é que as cinco portas
 * dos serviços continuam publicadas no docker-compose.yml -- com um ponto único,
 * bastaria chamar a porta de um serviço direto, sem token, mandando o cabeçalho
 * já "aprovado", e ele acreditaria. Conferindo cada um, não existe cabeçalho em
 * que confiar.
 *
 * <p>Como esta classe mora no nubex-common, os cinco serviços passam a conferir
 * de uma vez, sem mudança em nenhum deles.
 */
@Configuration(proxyBeanMethods = false)
class ConferenciaDeTokenConfig {

    private static final Logger log = LoggerFactory.getLogger(ConferenciaDeTokenConfig.class);

    /**
     * Monta o conferidor do token a partir da chave pública.
     *
     * <p>Chave <b>ausente</b> e chave <b>malformada</b> não se comportam igual, e a
     * diferença é deliberada:
     *
     * <ul>
     *   <li><b>ausente</b>: o serviço sobe mesmo assim, com um conferidor que
     *       recusa qualquer token. É a falha fechada -- o /actuator/health
     *       continua respondendo, de que o job do CI depende, e toda rota
     *       protegida responde 401 em vez de deixar passar sem conferir. Não
     *       subir aqui deixaria o CI vermelho sem necessidade, porque ele não
     *       define chave nenhuma;
     *   <li><b>malformada</b>: o serviço <b>não sobe</b>, e a exceção nomeia a
     *       propriedade e o formato esperado. Chave torta é erro de digitação em
     *       configuração, não estado de ambiente: falhar alto poupa horas de caça
     *       a 401 que nunca vão virar 200. Como este bean mora no nubex-common,
     *       vale para os cinco serviços.
     * </ul>
     *
     * <p>Os dois casos têm teste no ConferenciaDeTokenConfigTest.
     *
     * <p>O algoritmo é fixado em RS256 de propósito. Sem fixar, um atacante pode
     * tentar a troca de algoritmo: reassinar o token com outro esquema que a
     * chave pública valide.
     */
    @Bean
    @ConditionalOnMissingBean(JwtDecoder.class)
    JwtDecoder jwtDecoder(NubexJwtProperties propriedades) {
        if (!propriedades.temChavePublica()) {
            log.error("nubex.jwt.chave-publica não definida: nenhum token será aceito e toda "
                    + "rota protegida responderá 401. Defina JWT_CHAVE_PUBLICA no ambiente.");
            return token -> {
                throw new BadJwtException("Serviço sem chave pública configurada");
            };
        }

        NimbusJwtDecoder conferidor = NimbusJwtDecoder
                .withPublicKey(ChavesRsa.publica(propriedades.chavePublica()))
                .signatureAlgorithm(SignatureAlgorithm.RS256)
                .build();

        /*
         * Assinatura válida não basta. O JwtTimestampValidator recusa token
         * vencido (o campo exp) e o JwtIssuerValidator recusa token que não tenha
         * saído do nosso usuario-service (o campo iss) -- sem ele, um token
         * assinado pela mesma chave para outra finalidade seria aceito aqui.
         */
        conferidor.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                new JwtTimestampValidator(),
                new JwtIssuerValidator(propriedades.emissor())));

        return conferidor;
    }

    /**
     * Traduz os campos do token para o que o {@code @PreAuthorize} entende.
     *
     * <p>O RF051 descreve seis perfis, que no nosso modelo de dados são a
     * combinação de perfil (5 valores) com tipo (INTERNO ou EXTERNO). Por isso
     * geramos duas autoridades, e não uma:
     *
     * <ul>
     *   <li>{@code ROLE_<perfil>}, que o {@code hasRole('SOLICITANTE')} encontra
     *       -- o prefixo ROLE_ é a convenção que o hasRole acrescenta sozinho;
     *   <li>{@code TIPO_<tipo>}, para o {@code hasAuthority('TIPO_INTERNO')}.
     * </ul>
     *
     * <p>Assim "solicitante interno" se escreve
     * {@code hasRole('SOLICITANTE') and hasAuthority('TIPO_INTERNO')}, sem
     * precisar de cinco valores de perfil novos só para separar origem.
     *
     * <p>O principal passa a ser o campo sub, que carrega o id do usuário: dentro
     * de um controller, {@code authentication.getName()} devolve esse id.
     */
    @Bean
    @ConditionalOnMissingBean(JwtAuthenticationConverter.class)
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter conversor = new JwtAuthenticationConverter();
        conversor.setJwtGrantedAuthoritiesConverter(autoridadesDoToken());
        conversor.setPrincipalClaimName("sub");
        return conversor;
    }

    private static Converter<Jwt, Collection<GrantedAuthority>> autoridadesDoToken() {
        return token -> {
            List<GrantedAuthority> autoridades = new ArrayList<>(2);
            acrescentar(autoridades, "ROLE_", token.getClaimAsString("perfil"));
            acrescentar(autoridades, "TIPO_", token.getClaimAsString("tipo"));
            return autoridades;
        };
    }

    private static void acrescentar(List<GrantedAuthority> destino, String prefixo, String valor) {
        if (valor != null && !valor.isBlank()) {
            destino.add(new SimpleGrantedAuthority(prefixo + valor.trim()));
        }
    }
}
