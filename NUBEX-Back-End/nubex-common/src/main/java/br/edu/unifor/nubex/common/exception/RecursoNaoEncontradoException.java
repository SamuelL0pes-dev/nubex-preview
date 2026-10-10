package br.edu.unifor.nubex.common.exception;

import org.springframework.http.HttpStatus;

/** Registro não existe (404). */
public class RecursoNaoEncontradoException extends NegocioException {

    public RecursoNaoEncontradoException(String mensagem) {
        super(HttpStatus.NOT_FOUND, mensagem);
    }
}
