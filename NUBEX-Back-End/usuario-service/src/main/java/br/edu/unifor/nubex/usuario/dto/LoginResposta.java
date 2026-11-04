package br.edu.unifor.nubex.usuario.dto;

import br.edu.unifor.nubex.usuario.model.PerfilUsuario;
import br.edu.unifor.nubex.usuario.model.TipoUsuario;
import br.edu.unifor.nubex.usuario.model.Usuario;

/**
 * Resposta do login.
 *
 * <p><b>O token não vem aqui.</b> Ele sai no cabeçalho {@code Set-Cookie}, como
 * cookie {@code HttpOnly}, montado pelo {@code CookieDeSessao} -- o
 * {@link br.edu.unifor.nubex.usuario.controller.AutenticacaoController} explica
 * por quê. Este objeto carrega só o que a tela precisa para se montar.
 *
 * <p>O {@code usuario} repete o que já está dentro do token. É de propósito: sem
 * ele, o front teria que abrir o token para saber o perfil de quem entrou -- e
 * agora não teria nem como, porque não enxerga mais o token. O token segue sendo
 * a única coisa que a API confere; o que vem neste objeto é só para a tela montar
 * o menu.
 *
 * <p>Só os quatro campos necessários para isso. Nada de e-mail, telefone, cpf ou
 * matrícula: o RNF010 manda tratar apenas o dado necessário à finalidade, e a
 * finalidade aqui é dizer quem entrou e o que ela pode ver.
 *
 * <p>O {@code expiraEmSegundos} continua porque o front usa para avisar que a
 * sessão vai cair. Ele é informativo: quem decide se o token vale é o campo
 * {@code exp} de dentro dele, conferido em cada serviço.
 */
public record LoginResposta(
        long expiraEmSegundos,
        UsuarioLogado usuario) {

    public record UsuarioLogado(
            Long id,
            String nome,
            PerfilUsuario perfil,
            TipoUsuario tipo) {

        static UsuarioLogado de(Usuario usuario) {
            return new UsuarioLogado(
                    usuario.getId(), usuario.getNome(), usuario.getPerfil(), usuario.getTipo());
        }
    }

    public static LoginResposta de(long expiraEmSegundos, Usuario usuario) {
        return new LoginResposta(expiraEmSegundos, UsuarioLogado.de(usuario));
    }

    /**
     * Continua sobrescrito depois de o token sair daqui, agora por outro motivo:
     * o {@code nome} é dado pessoal, e o toString() que o record gera sozinho o
     * escreveria em qualquer log que imprimisse este objeto. Fica só o id, que é
     * o que o RNF010 permite registrar.
     */
    @Override
    public String toString() {
        return "LoginResposta[expiraEmSegundos=" + expiraEmSegundos
                + ", usuario=" + (usuario == null ? "null" : usuario.id()) + "]";
    }
}
