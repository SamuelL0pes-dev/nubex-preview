package br.edu.unifor.nubex.common.exception;

import org.springframework.http.HttpStatus;

/** Conflito com o estado atual, como e-mail já cadastrado ou horário já reservado (409). */
public class ConflitoException extends NegocioException {

    public ConflitoException(String mensagem) {
        super(HttpStatus.CONFLICT, mensagem);
    }
}
