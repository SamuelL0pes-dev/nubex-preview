package br.edu.unifor.nubex.usuario.model;

/**
 * Para que serve o token guardado em TokenVerificacao.
 * Os dois são de uso único e têm prazo de validade.
 */
public enum TipoToken {

    /** Código enviado por e-mail para confirmar o endereço no cadastro (RF057). */
    CODIGO_EMAIL,

    /** Link de uso único para definir nova senha (RF086). */
    RECUPERACAO_SENHA
}
