package br.edu.unifor.nubex.conteudo.mapper;

import br.edu.unifor.nubex.conteudo.dto.NoticiaResumo;
import br.edu.unifor.nubex.conteudo.model.Noticia;
import org.springframework.stereotype.Component;

@Component
public class NoticiaMapper {

    public NoticiaResumo toResponse(Noticia noticia){
        return new NoticiaResumo(
                noticia.getId(),
                noticia.getTitulo(),
                noticia.getResumo(),
                noticia.getUnidade(),
                noticia.getImagemUrl(),
                noticia.getLink(),
                noticia.getDataPublicacao(),
                noticia.isDestaque()
        );
    }
}
