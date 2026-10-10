package br.edu.unifor.nubex.common.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class StackTraceSanitizadoTest {

    @Test
    void mostraOndeFalhouSemVazarAMensagem() {
        Exception erro = new IllegalStateException("cpf 123.456.789-00",
                new RuntimeException("email maria@exemplo.com"));

        String texto = StackTraceSanitizado.de(erro);

        assertFalse(texto.contains("123.456.789-00"));
        assertFalse(texto.contains("maria@exemplo.com"));
        assertTrue(texto.contains("java.lang.IllegalStateException"));
        assertTrue(texto.contains("Causado por: java.lang.RuntimeException"));
        assertTrue(texto.contains("StackTraceSanitizadoTest"));
    }
}
