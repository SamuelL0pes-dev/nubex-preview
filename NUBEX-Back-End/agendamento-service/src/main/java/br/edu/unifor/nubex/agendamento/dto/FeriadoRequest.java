package br.edu.unifor.nubex.agendamento.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record FeriadoRequest(
    @NotNull(message = "A data é obrigatória")
    LocalDate data,
    @NotBlank(message = "A descrição é obrigatória")
    @Size(max = 120, message = "A descrição deve ter no máximo 120 caracteres")
    String descricao
) {
    public FeriadoRequest {
        if (descricao != null){
            descricao = descricao.trim();
        }
    }
}
