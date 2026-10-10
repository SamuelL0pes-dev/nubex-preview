package br.edu.unifor.nubex.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Violação de regra de negócio (ex.: "horário já reservado"). O service lança,
 * o GlobalExceptionHandler transforma em resposta HTTP. A mensagem é mostrada
 * ao usuário, então nunca coloque dado sensível nela.
 */
public class NegocioException extends RuntimeException {

    private final HttpStatus status;

    public NegocioException(String mensagem) {
        this(HttpStatus.BAD_REQUEST, mensagem);
    }

    public NegocioException(HttpStatus status, String mensagem) {
        super(mensagem);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
