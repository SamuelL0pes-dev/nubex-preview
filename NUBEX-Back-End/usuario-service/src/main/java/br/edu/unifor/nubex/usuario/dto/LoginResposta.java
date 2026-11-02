package br.edu.unifor.nubex.usuario.dto;

import br.edu.unifor.nubex.usuario.model.PerfilUsuario;
import br.edu.unifor.nubex.usuario.model.TipoUsuario;
import br.edu.unifor.nubex.usuario.model.Usuario;

/**
 * Resposta do login.
 *
 * <p>O {@code usuario} repete o que já está dentro do token. É de propósito: sem
 * ele, o front teria que abrir o token para saber o perfil de quem entrou, e
 * decodificar JWT no navegador é trabalho que não precisa existir. O token segue
 * sendo a única coisa que a API confere -- o que vem neste objeto é só para a
 * tela montar o menu.
 *
 * <p>Só os quatro campos necessários para isso. Nada de e-mail, telefone, cpf ou
 * matrícula: o RNF010 manda tratar apenas o dado necessário à finalidade, e a
 * finalidade aqui é dizer quem entrou e o que ela pode ver.
 */
public record LoginResposta(
        String token,
        String tipoDeToken,
        long expiraEmSegundos,
        UsuarioLogado usuario) {

    /** O prefixo que o front precisa mandar: "Authorization: Bearer <token>". */
    public static final String TIPO_BEARER = "Bearer";

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

    public static LoginResposta de(String token, long expiraEmSegundos, Usuario usuario) {
        return new LoginResposta(
                token, TIPO_BEARER, expiraEmSegundos, UsuarioLogado.de(usuario));
    }

    /**
     * O token é credencial: quem tiver uma cópia entra como a pessoa, até ele
     * vencer. Então ele não aparece em log, nem aqui nem em nenhum lugar.
     */
    @Override
    public String toString() {
        return "LoginResposta[token=omitido, expiraEmSegundos=" + expiraEmSegundos
                + ", usuario=" + (usuario == null ? "null" : usuario.id()) + "]";
    }
}
