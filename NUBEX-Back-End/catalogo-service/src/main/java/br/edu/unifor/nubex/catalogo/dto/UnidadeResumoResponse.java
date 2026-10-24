package br.edu.unifor.nubex.catalogo.dto;

public record UnidadeResumoResponse(
        Long id,
        String nome,
        String slug,
        String descricao,
        String imagemUrl
){
}
