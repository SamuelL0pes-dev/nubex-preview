package br.edu.unifor.nubex.conteudo.dto;

import br.edu.unifor.nubex.conteudo.model.Noticia;

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

    public static NoticiaResumo de(Noticia noticia) {
        return new NoticiaResumo(
                noticia.getId(),
                noticia.getTitulo(),
                noticia.getResumo(),
                noticia.getUnidade(),
                noticia.getImagemUrl(),
                noticia.getLink(),
                noticia.getDataPublicacao(),
                noticia.isDestaque());
    }
}
