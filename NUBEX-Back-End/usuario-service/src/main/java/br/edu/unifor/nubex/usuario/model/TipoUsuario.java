package br.edu.unifor.nubex.usuario.model;

/**
 * Origem do usuário: INTERNO é da Unifor, EXTERNO vem de empresa ou instituição
 * parceira. Diz de onde a pessoa vem, não como ela faz login.
 *
 * <p>Os dois entram por e-mail. Quem tem matrícula pode entrar por ela também,
 * mas matrícula é uma forma adicional e não a única: o administrador é INTERNO,
 * não tem matrícula nenhuma e entra por e-mail. A constraint
 * ck_usuario_participante_interno_tem_matricula da migração só exige matrícula de
 * PARTICIPANTE interno, que é o discente.
 *
 * <p>Combinado com o perfil, este campo é o que produz os seis perfis do RF051
 * sem duplicar valores de perfil: solicitante e participante existem nas duas
 * origens.
 */
public enum TipoUsuario {
    INTERNO,
    EXTERNO
}
