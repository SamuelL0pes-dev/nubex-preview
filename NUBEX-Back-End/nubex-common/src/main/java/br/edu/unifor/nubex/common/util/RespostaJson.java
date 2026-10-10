package br.edu.unifor.nubex.common.util;

import br.edu.unifor.nubex.common.exception.ApiError;
import tools.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.MediaType;

/**
 * Escreve um ApiError direto na resposta. Os filtros de segurança rodam antes do
 * Spring MVC, então o GlobalExceptionHandler não os alcança e precisam escrever
 * o JSON "na mão".
 */
public final class RespostaJson {

    private RespostaJson() {
    }

    public static void escrever(ObjectMapper mapper, HttpServletResponse response, ApiError erro) throws IOException {
        response.setStatus(erro.status());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        mapper.writeValue(response.getOutputStream(), erro);
    }
}
