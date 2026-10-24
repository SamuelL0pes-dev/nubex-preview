package br.edu.unifor.nubex.catalogo.dto;

import java.util.List;

public record UnidadeDetalheResponse(
        Long id,
        String nome,
        String slug,
        String descricao,
        String imagemUrl,
        List<LaboratorioDetalheResponse> laboratorios
) {
}
