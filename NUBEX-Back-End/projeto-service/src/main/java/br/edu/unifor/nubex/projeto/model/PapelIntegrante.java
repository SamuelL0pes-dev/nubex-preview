package br.edu.unifor.nubex.projeto.model;

/**
 * Função da pessoa DENTRO de um projeto de pesquisa (RF068: "o papel de cada
 * um, como coordenador ou pesquisador").
 *
 * Não confundir com duas coisas parecidas que vivem no usuario-service:
 *  - perfil  -> o que a pessoa pode fazer na API;
 *  - vinculo -> o que a pessoa é na universidade.
 *
 * O papel fica aqui, na tabela de integrantes, porque muda de projeto para
 * projeto: a mesma pessoa coordena um e pesquisa em outro.
 *
 * Os valores seguem a organização do RF018 e o padrão já usado em
 * conteudo.pesquisador.secao.
 */
public enum PapelIntegrante {

    /** Lidera o projeto. */
    COORDENADOR,

    /** Executa a pesquisa. */
    PESQUISADOR,

    /** Discente de iniciação científica (RF018, RF069). */
    INICIACAO_CIENTIFICA,

    /**
     * Qualquer função não prevista. Existe para o caso imprevisto não exigir
     * migração: promover um papel a valor próprio é decisão consciente, não
     * acidente de cadastro.
     */
    OUTROS
}
