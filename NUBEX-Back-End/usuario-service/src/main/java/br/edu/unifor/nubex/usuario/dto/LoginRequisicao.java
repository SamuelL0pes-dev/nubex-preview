package br.edu.unifor.nubex.usuario.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Corpo do POST /api/auth/login.
 *
 * <p>Um campo só para as duas formas de entrar: quem tem matrícula (discente)
 * pode usar o número; os demais usam o e-mail. O serviço decide pelo que foi
 * digitado -- com arroba, procura por e-mail; sem arroba, por matrícula. Dois
 * campos obrigariam o front a perguntar antes "você vai entrar com o quê?".
 *
 * <p>As mensagens de validação são escritas aqui porque vão direto para o
 * usuário, no formato de erro do ApiError.
 */
public record LoginRequisicao(

        @NotBlank(message = "Informe o e-mail ou a matrícula")
        @Size(max = 150, message = "E-mail ou matrícula muito longo")
        String identificador,

        /*
         * 72 é o limite do BCrypt: ele ignora o que passa de 72 bytes. Recusar
         * aqui é mais honesto do que aceitar e truncar em silêncio. O limite é em
         * bytes, e não em caracteres, então uma senha com acentos chega ao limite
         * um pouco antes -- o cadastro usa a mesma regra, de 8 a 36 caracteres,
         * bem abaixo disso.
         */
        @NotBlank(message = "Informe a senha")
        @Size(max = 72, message = "Senha muito longa")
        String senha) {

    /**
     * Sem isto, o toString() que o record gera sozinho imprimiria a senha em
     * texto. Basta um log de erro de validação citando o objeto para a senha ir
     * para o arquivo de log.
     */
    @Override
    public String toString() {
        return "LoginRequisicao[identificador=informado, senha=omitida]";
    }
}
