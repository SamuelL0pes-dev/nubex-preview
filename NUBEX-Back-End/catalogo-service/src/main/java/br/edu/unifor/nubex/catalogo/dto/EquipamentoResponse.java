package br.edu.unifor.nubex.catalogo.dto;

import br.edu.unifor.nubex.catalogo.model.StatusEquipamento;

public record EquipamentoResponse(
        Long id,
        Long laboratorioId,
        String nome,
        String descricao,
        StatusEquipamento status
) {
}
