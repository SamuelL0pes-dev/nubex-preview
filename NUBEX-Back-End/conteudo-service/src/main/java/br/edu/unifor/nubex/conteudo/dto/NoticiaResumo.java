package br.edu.unifor.nubex.conteudo.dto;

import java.time.LocalDate;

public record NoticiaResumo(
        Long id,
        String titulo,
        String resumo,
        String unidade,
        String imagemUrl,
        String link,
        LocalDate dataPublicacao,
        boolean destaque) {
}
