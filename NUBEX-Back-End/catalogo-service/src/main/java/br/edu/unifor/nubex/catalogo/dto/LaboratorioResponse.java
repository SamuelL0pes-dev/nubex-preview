package br.edu.unifor.nubex.catalogo.dto;

public record LaboratorioResponse(
        Long id,
        Long unidadeId,
        String nome,
        String descricao
) {
}
