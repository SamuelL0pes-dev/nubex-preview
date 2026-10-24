package br.edu.unifor.nubex.conteudo.mapper;

import br.edu.unifor.nubex.conteudo.dto.PesquisadorResponse;
import br.edu.unifor.nubex.conteudo.model.Pesquisador;
import org.springframework.stereotype.Component;

@Component
public class PesquisadorMapper {

    public PesquisadorResponse toResponse(Pesquisador pesquisador){
        return new PesquisadorResponse(
                pesquisador.getId(),
                pesquisador.getNome(),
                pesquisador.getCargo(),
                pesquisador.getLinhaPesquisa(),
                pesquisador.getBio(),
                pesquisador.getFotoUrl(),
                pesquisador.getLattesUrl(),
                pesquisador.getOrcidUrl(),
                pesquisador.getGoogleAcademicoUrl()
        );
    }
}
