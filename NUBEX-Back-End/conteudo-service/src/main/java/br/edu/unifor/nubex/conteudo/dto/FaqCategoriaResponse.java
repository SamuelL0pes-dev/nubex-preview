package br.edu.unifor.nubex.conteudo.dto;

import java.util.List;

public record FaqCategoriaResponse(
        String categoria,
        List<FaqResponse> perguntas
) {
}
