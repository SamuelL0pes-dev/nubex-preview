package br.edu.unifor.nubex.common.config;

import br.edu.unifor.nubex.common.exception.ApiError;
import br.edu.unifor.nubex.common.util.RespostaJson;
import tools.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

/** Chamado quando o usuário está autenticado mas o perfil dele não permite a rota: responde 403 em JSON. */
public class ApiAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public ApiAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        ApiError erro = ApiError.of(HttpStatus.FORBIDDEN,
                "Você não tem permissão para acessar este recurso", request.getRequestURI());
        RespostaJson.escrever(objectMapper, response, erro);
    }
}
