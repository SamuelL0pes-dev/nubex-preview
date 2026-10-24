package br.edu.unifor.nubex.catalogo.dto;

import br.edu.unifor.nubex.catalogo.model.StatusEquipamento;

public record EquipamentoStatusResponse(
        Long id,
        StatusEquipamento status
) {
}
