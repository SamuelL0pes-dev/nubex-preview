package br.edu.unifor.nubex.agendamento.dto;

import java.time.LocalDate;

public record FeriadoResponse (
    Long id,
    LocalDate data,
    String descricao
){
}
