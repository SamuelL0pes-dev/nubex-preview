package br.edu.unifor.nubex.conteudo.dto;

import br.edu.unifor.nubex.conteudo.model.Edital;

import java.time.LocalDate;

public record EditalResumo(
        Long id,
        String titulo,
        String resumo,
        String unidade,
        String imagemUrl,
        String link,
        LocalDate dataPublicacao,
        LocalDate dataEncerramento,
        boolean destaque) {

    public static EditalResumo de(Edital edital) {
        return new EditalResumo(
                edital.getId(),
                edital.getTitulo(),
                edital.getResumo(),
                edital.getUnidade(),
                edital.getImagemUrl(),
                edital.getLink(),
                edital.getDataPublicacao(),
                edital.getDataEncerramento(),
                edital.isDestaque());
    }
}
