package br.edu.unifor.nubex.conteudo.mapper;

import br.edu.unifor.nubex.conteudo.dto.EditalResumo;
import br.edu.unifor.nubex.conteudo.model.Edital;
import org.springframework.stereotype.Component;

@Component
public class EditalMapper {

    public EditalResumo toResponse(Edital edital){
        return new EditalResumo(
                edital.getId(),
                edital.getTitulo(),
                edital.getResumo(),
                edital.getUnidade(),
                edital.getImagemUrl(),
                edital.getLink(),
                edital.getDataPublicacao(),
                edital.getDataEncerramento(),
                edital.isDestaque()
        );
    }
}
