package br.edu.unifor.nubex.conteudo.dto;

public record PesquisadorResponse(
        Long id,
        String nome,
        String cargo,
        String linhaPesquisa,
        String bio,
        String fotoUrl,
        String lattesUrl,
        String orcidUrl,
        String googleAcademicoUrl
) {
}
