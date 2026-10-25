package br.edu.unifor.nubex.usuario.model;

/**
 * Perfil de acesso: o que a pessoa PODE fazer na API (RF051, RNF013).
 * É lido pelo @PreAuthorize de cada rota.
 *
 * Não confundir com TipoUsuario (de onde a pessoa vem) nem com
 * VinculoUsuario (o que ela é na universidade). As três dimensões são
 * independentes: "solicitante externo", por exemplo, é
 * perfil=SOLICITANTE + tipo=EXTERNO.
 *
 * DIVERGE do Apêndice A: o documento previa PESQUISADOR_EXTERNO como perfil.
 * Ele deixou de existir, porque virava a mesma informação em dois lugares.
 */
public enum PerfilUsuario {

    /** Discente da Unifor, incluído em agendamentos por um solicitante interno. Não cria nem altera agendamento (RF050). */
    PARTICIPANTE,

    /** Faz agendamento, cadastra projeto e pede compra. Se for INTERNO, também aprova cadastros (RF052, RF055). */
    SOLICITANTE,

    /** Diretoria. Acesso total ao painel administrativo (RF073, RF074). */
    ADMINISTRADOR,

    /** Despacha compra já aprovada e acompanha agendamentos e contatos. Não cadastra projeto nem aprova solicitante (RF075). */
    SUPERVISAO_ADMINISTRATIVA,

    /** Vê tudo o que o administrador vê e não altera nada (RF075). */
    ADMINISTRADOR_CONSULTA
}
