package br.edu.unifor.nubex.conteudo.dto;

import br.edu.unifor.nubex.conteudo.model.SecaoPesquisador;

import java.util.List;

public record PesquisadorSecaoResponse(
        SecaoPesquisador secao,
        List<PesquisadorResponse> pesquisadores
) {
}
