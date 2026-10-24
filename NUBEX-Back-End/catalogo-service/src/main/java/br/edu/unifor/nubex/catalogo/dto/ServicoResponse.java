package br.edu.unifor.nubex.catalogo.dto;

public record ServicoResponse(
        Long id,
        Long unidadeId,
        String unidadeNome,
        String unidadeSlug,
        String categoria,
        String tecnicas,
        String aplicacoes

) {
}
