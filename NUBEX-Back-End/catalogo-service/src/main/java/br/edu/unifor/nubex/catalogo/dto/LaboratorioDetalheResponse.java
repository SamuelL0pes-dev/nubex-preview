package br.edu.unifor.nubex.catalogo.dto;

import java.util.List;

public record LaboratorioDetalheResponse(
        Long id,
        String nome,
        String descricao,
        List<EquipamentoResponse> equipamentos
) {
}
