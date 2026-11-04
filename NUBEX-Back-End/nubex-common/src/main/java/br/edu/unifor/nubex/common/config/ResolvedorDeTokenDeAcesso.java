package br.edu.unifor.nubex.common.config;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;

/**
 * Diz ao Spring Security onde achar o token em cada requisição: primeiro no
 * cookie {@link CookieDeSessao#NOME}, e se não houver, no cabeçalho
 * {@code Authorization: Bearer <token>}.
 *
 * <p>Sem esta classe, o Spring olha só o cabeçalho. O front passou a mandar o
 * token por cookie, então sem ela toda requisição depois do login chegaria sem
 * credencial e tomaria 401 -- com o cookie correto viajando na requisição, só não
 * lido por ninguém.
 *
 * <h4>Por que o cabeçalho continua valendo</h4>
 *
 * <p>Não é compatibilidade com o front: ele não usa mais. É ferramenta de
 * trabalho. O botão <i>Authorize</i> do Swagger manda cabeçalho, o {@code curl}
 * manda cabeçalho, e os testes que já existem mandam cabeçalho. Tirar a reserva
 * não fecharia porta nenhuma -- quem rouba um token pode mandá-lo nos dois
 * formatos -- e custaria todas essas três coisas.
 *
 * <h4>Cookie na frente, e só um dos dois</h4>
 *
 * <p>Quando os dois chegam juntos, vale o cookie e o cabeçalho é ignorado. É uma
 * escolha de clareza: a alternativa seria recusar a requisição por ambiguidade,
 * o que transformaria um cookie esquecido no navegador em erro para quem está
 * testando pelo Swagger na mesma aba.
 *
 * <p>O token continua proibido na URL. O {@link DefaultBearerTokenResolver} já
 * vem com isso desligado, e fica assim: endereço vai para log de servidor, para
 * histórico do navegador e para o cabeçalho {@code Referer} de qualquer link
 * clicado -- três cópias da credencial, duas delas fora do nosso alcance, e o
 * RNF010 não admite nenhuma.
 */
public class ResolvedorDeTokenDeAcesso implements BearerTokenResolver {

    private final BearerTokenResolver reserva = new DefaultBearerTokenResolver();

    @Override
    public String resolve(HttpServletRequest requisicao) {
        String doCookie = tokenNoCookie(requisicao);
        return (doCookie != null) ? doCookie : reserva.resolve(requisicao);
    }

    /**
     * Devolve null -- e não string vazia -- quando não acha, para o resolve()
     * cair na reserva. Cookie com valor vazio é o que o logout deixa no
     * navegador até ele expirar o registro, e tem que contar como ausente.
     *
     * <h4>Por que o laço segue depois de achar um vazio</h4>
     *
     * <p>Porque <b>pode haver mais de um cookie com o mesmo nome</b> na mesma
     * requisição, e a ordem em que eles chegam não é definida por nenhuma
     * especificação.
     *
     * <p>Isso não é hipótese distante: é o que acontece no dia em que o cookie
     * ganhar o atributo {@code Domain} para atravessar os hosts dos cinco
     * serviços, caso descrito no {@link CookieDeSessao}. Quem já tinha o cookie
     * antigo, amarrado a um host só, passa a ter os dois registrados -- mesmo
     * nome, escopos diferentes. Parar no primeiro faria a pessoa ser tratada como
     * anônima em toda requisição, com o cookie válido chegando ao servidor e
     * nenhuma pista no log.
     *
     * <p>Então vale o primeiro que tiver conteúdo, não o primeiro que aparecer.
     */
    private static String tokenNoCookie(HttpServletRequest requisicao) {
        Cookie[] cookies = requisicao.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (!CookieDeSessao.NOME.equals(cookie.getName())) {
                continue;
            }
            String valor = cookie.getValue();
            if (valor != null && !valor.isBlank()) {
                return valor;
            }
        }
        return null;
    }
}
