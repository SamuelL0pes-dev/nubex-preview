package br.edu.unifor.nubex.conteudo.mapper;

import br.edu.unifor.nubex.conteudo.dto.FaqResponse;
import br.edu.unifor.nubex.conteudo.model.Faq;
import org.springframework.stereotype.Component;

@Component
public class FaqMapper {

    public FaqResponse toResponse(Faq faq){
        return new FaqResponse(
                faq.getId(),
                faq.getPergunta(),
                faq.getResposta()
        );
    }
}
