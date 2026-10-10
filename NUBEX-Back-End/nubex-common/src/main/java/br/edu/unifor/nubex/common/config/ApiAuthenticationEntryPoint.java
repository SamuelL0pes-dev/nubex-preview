package br.edu.unifor.nubex.common.config;

import br.edu.unifor.nubex.common.exception.ApiError;
import br.edu.unifor.nubex.common.util.RespostaJson;
import tools.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

/** Chamado quando alguém sem autenticação acessa uma rota protegida: responde 401 em JSON. */
public class ApiAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public ApiAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        // Mensagem genérica: não revela se o token expirou, não existe ou é inválido.
        ApiError erro = ApiError.of(HttpStatus.UNAUTHORIZED,
                "Autenticação necessária ou inválida", request.getRequestURI());
        RespostaJson.escrever(objectMapper, response, erro);
    }
}
