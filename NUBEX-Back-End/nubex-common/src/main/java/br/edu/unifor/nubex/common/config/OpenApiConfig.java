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

    private static final String ESQUEMA_COOKIE = "cookieAuth";
    private static final String ESQUEMA_BEARER = "bearerAuth";

    /**
     * Os dois jeitos de autenticar, na ordem em que valem.
     *
     * <p>São declarados como dois itens separados, e não como um item com dois
     * esquemas: separados significam "um <b>ou</b> o outro basta", que é o
     * comportamento real do {@link ResolvedorDeTokenDeAcesso}. Num item só
     * significaria "os dois juntos são obrigatórios", o que seria falso.
     *
     * <ul>
     *   <li><b>cookieAuth</b>: o cookie {@code HttpOnly} que o login devolve. É o
     *       que o portal usa, e a documentação tem que dizer isso -- senão quem
     *       ler conclui que o front deve guardar e reenviar o token, que é
     *       exatamente a prática que a mudança para cookie removeu;
     *   <li><b>bearerAuth</b>: o cabeçalho {@code Authorization}, que continua
     *       valendo como reserva. É o que o botão <i>Authorize</i> desta tela
     *       manda, e o que o {@code curl} e os testes usam.
     * </ul>
     *
     * <p>A {@code description} do cookieAuth não é enfeite. O springdoc desenha
     * esquema de cookie como campo de texto no diálogo <i>Authorize</i>, e
     * digitar ali <b>não tem efeito nenhum</b>: cookie é administrado pelo
     * navegador, e este é {@code HttpOnly}. Sem o aviso, quem fosse testar uma
     * rota protegida colaria o token no primeiro campo da lista, continuaria
     * tomando 401 e concluiria que a API está quebrada.
     */
    @Bean
    OpenAPI openAPI(@Value("${spring.application.name:nubex}") String nomeServico) {
        return new OpenAPI()
                .info(new Info()
                        .title("NUBEX - " + nomeServico)
                        .description("API do Portal NUBEX")
                        .version("0.1.0"))
                .components(new Components()
                        .addSecuritySchemes(ESQUEMA_COOKIE,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.APIKEY)
                                        .in(SecurityScheme.In.COOKIE)
                                        .name(CookieDeSessao.NOME)
                                        .description("Como o portal autentica. O cookie é "
                                                + "HttpOnly: o navegador o manda sozinho depois "
                                                + "do login em POST /api/auth/login, e nenhum "
                                                + "script o lê. **Não use este campo** -- digitar "
                                                + "aqui não tem efeito. Para testar nesta tela, "
                                                + "faça login, copie o token e use o bearerAuth."))
                        .addSecuritySchemes(ESQUEMA_BEARER,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")))
                .addSecurityItem(new SecurityRequirement().addList(ESQUEMA_COOKIE))
                .addSecurityItem(new SecurityRequirement().addList(ESQUEMA_BEARER));
    }
}
