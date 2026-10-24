package br.edu.unifor.nubex.conteudo.dto;

public record FaqResponse(
        Long id,
        String pergunta,
        String resposta
) {
}
