package br.edu.unifor.nubex.common.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;

/**
 * Formato único de erro de TODAS as APIs do NUBEX. O front trata um formato só.
 * Campos nulos não vão no JSON.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(
        Instant timestamp,
        int status,
        String erro,
        String mensagem,
        String caminho,
        String codigo,
        List<CampoInvalido> campos) {

    /** Um campo do formulário que não passou na validação. */
    public record CampoInvalido(String campo, String mensagem) {
    }

    public static ApiError of(HttpStatus status, String mensagem, String caminho) {
        return new ApiError(Instant.now(), status.value(), status.getReasonPhrase(),
                mensagem, caminho, null, null);
    }

    public ApiError comCampos(List<CampoInvalido> campos) {
        return new ApiError(timestamp, status, erro, mensagem, caminho, codigo, campos);
    }

    public ApiError comCodigo(String codigo) {
        return new ApiError(timestamp, status, erro, mensagem, caminho, codigo, campos);
    }
}
