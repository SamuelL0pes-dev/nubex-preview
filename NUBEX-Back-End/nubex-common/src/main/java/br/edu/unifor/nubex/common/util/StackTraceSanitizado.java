package br.edu.unifor.nubex.common.util;

/**
 * Monta o "stack trace" de uma exceção SEM as mensagens. Mostra a classe de cada
 * exceção e as linhas de código por onde ela passou, que é o que a equipe precisa
 * para achar o erro. A mensagem fica de fora porque exceções de banco, por exemplo,
 * trazem o valor que causou o problema (e-mail, CPF) dentro do texto.
 */
public final class StackTraceSanitizado {

    private static final int MAX_CAUSAS = 5;
    private static final int MAX_LINHAS = 25;

    private StackTraceSanitizado() {
    }

    public static String de(Throwable erro) {
        StringBuilder texto = new StringBuilder();
        Throwable atual = erro;
        int nivel = 0;
        while (atual != null && nivel < MAX_CAUSAS) {
            texto.append(nivel == 0 ? "" : "Causado por: ").append(atual.getClass().getName()).append('\n');
            StackTraceElement[] linhas = atual.getStackTrace();
            int limite = Math.min(linhas.length, MAX_LINHAS);
            for (int i = 0; i < limite; i++) {
                texto.append("\tat ").append(linhas[i]).append('\n');
            }
            atual = (atual.getCause() == atual) ? null : atual.getCause();
            nivel++;
        }
        return texto.toString();
    }
}
