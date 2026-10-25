package br.edu.unifor.nubex.usuario.model;

/**
 * Vínculo ACADÊMICO do usuário interno. Não confundir com PerfilUsuario:
 * vínculo é o que a pessoa é na universidade, perfil é o que ela pode fazer na API.
 */
public enum VinculoUsuario {
    DOCENTE,
    PESQUISADOR,
    MESTRANDO,
    DOUTORANDO,
    GRADUANDO,
    TECNICO
}
