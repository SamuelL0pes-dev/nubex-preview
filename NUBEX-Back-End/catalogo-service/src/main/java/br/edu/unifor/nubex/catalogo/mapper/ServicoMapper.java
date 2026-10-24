package br.edu.unifor.nubex.catalogo.mapper;

import br.edu.unifor.nubex.catalogo.dto.ServicoResponse;
import br.edu.unifor.nubex.catalogo.model.Servico;
import org.springframework.stereotype.Component;

@Component
public class ServicoMapper {

    public ServicoResponse toResponse(Servico servico){
        return new ServicoResponse(
                servico.getId(),
                servico.getUnidade().getId(),
                servico.getUnidade().getNome(),
                servico.getUnidade().getSlug(),
                servico.getCategoria(),
                servico.getTecnicas(),
                servico.getAplicacoes()
        );
    }
}
